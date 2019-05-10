package core.Server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import core.Shared.IServer;
import core.Shared.IClient;
import core.Shared.TopicMessage;

public class Server implements IServer {
	private static final long serialVersionUID = 1L;
	private String privateIp;
	private Registry registry;
	private ConcurrentHashMap<String,IClient> connectedClients;
	private ConcurrentHashMap<String,List<IClient>> topics;
  
	
	public Server() {
		try {
			privateIp=InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			System.out.println("PrivateIP could not be found");
			System.exit(0);
		}
		connectedClients = new ConcurrentHashMap<>();
		topics = new ConcurrentHashMap<>();
		setProperty();
		try {
			registry = LocateRegistry.createRegistry(8000);
			System.out.println("Registry created");
			} catch (RemoteException e) {
				if (e.getMessage().contains("Port already in use"))
					try {
						registry = LocateRegistry.getRegistry(8000);
						System.out.println("Registry found");
					} catch (RemoteException e1) {
						System.out.println("Registry could not be found or created");
						System.exit(0);
					}
					}
		}
  
	
	private static void setProperty() {
		System.setProperty("java.security.policy","file:./sec.policy");
		System.setProperty("java.rmi.server.codebase","file:${workspace_loc}/Server/");
		if (System.getSecurityManager()==null)		System.setSecurityManager(new SecurityManager());
		System.setProperty("java.rmi.server.hostname","localhost");
	}

	@Override
	public synchronized void connect(String clientId, IClient stub) throws RemoteException {
		try {
			registry.bind(clientId,stub);
			connectedClients.putIfAbsent(clientId,(IClient)registry.lookup(clientId));
		} catch (AlreadyBoundException | NotBoundException e) {
			System.out.println("Hand-shake failed with client "+clientId+", there already was a client with that id");
			throw new RemoteException("Hand-shake failed, there already was a client with id "+clientId);
		}
		System.out.println("Client "+clientId+" connected");
		stub.notifyClient("Hand-shake ok\n");
	}
	
	
	@Override
	public synchronized void disconnect(String clientId) throws RemoteException {
		try {
			registry.unbind(clientId);
		} catch (NotBoundException e) {
			System.out.println("Disconnection failed with client "+clientId+", there was not a client with that id");
			throw new RemoteException("Disconnection failed, there was not a client with id "+clientId);
		}
		IClient client = connectedClients.get(clientId);
		System.out.println("Client "+clientId+" disconnected");
		client.notifyClient("Disconnecting..");
		for(List<IClient> clientList : topics.values())
			clientList.remove(client);
		if (connectedClients.containsKey(clientId))	
			connectedClients.remove(clientId);
	}
  
  
	@Override
	public void createTopic(String clientId, String topic) throws RemoteException {
		if(topic.isEmpty())
			connectedClients.get(clientId).notifyClient("Topic name cannot be empty");
		else if (!topics.containsKey(topic)) {
	  		topics.put(topic, new LinkedList<IClient>());
	  		subscribe(clientId,topic);
		}
		else
			connectedClients.get(clientId).notifyClient("Topic with name "+topic+" already exists");
	}



	@Override
	public void subscribe(String clientId, String topic) throws RemoteException {
		List<IClient> clients = topics.get(topic);
		IClient client = connectedClients.get(clientId);
		try {
			synchronized(clients) {
				if (!clients.contains(client))	clients.add(client);
				else	client.notifyClient("You already subscribed the topic "+topic);
			}
		}
		catch(NullPointerException e) {
			client.notifyClient("Topic with name "+topic+" does not exists");
		}
	}


	@Override
	public void unsubscribe(String clientId, String topic) throws RemoteException {
		List<IClient> clients = topics.get(topic);
		IClient client = connectedClients.get(clientId);
		try {
			synchronized(clients) {
				if (clients.contains(client))	clients.remove(client);
				else	client.notifyClient("You are not subscribed to the topic "+topic+'\n');
			}
		}
		catch(NullPointerException e) {
			client.notifyClient("Topic with name "+topic+" does not exists");
		}
	}


	@Override
	public void publish(String clientId, TopicMessage message) throws RemoteException {
		if (topics.containsKey(message.getTopic()))
			for(IClient client : topics.get(message.getTopic()))
				client.sendMessage(message);
		else
			connectedClients.get(clientId).notifyClient("Topic with name "+message.getTopic()+" does not exist");
	}


	@Override
	public void showTopicList(String clientId) throws RemoteException {
		connectedClients.get(clientId).getTopicList(topics.keySet());
	}


	@Override
	public void showSubscribersOfOneTopic(String clientId, String topic) throws RemoteException {
		IClient client = connectedClients.get(clientId);
		if(topics.containsKey(topic)) {
			client.notifyClient("\nTopic: "+topic);
			for(IClient subscriber : topics.get(topic))
				for (AbstractMap.Entry<String,IClient> entry : connectedClients.entrySet())
					if (entry.getValue().equals(subscriber))
						client.notifyClient(entry.getKey());
		}
		else	client.notifyClient("Topic "+topic+" does not exists\n");
	}


	@Override
	public void showSubscribersOfAllTopics(String clientId) throws RemoteException {
		for(AbstractMap.Entry<String,List<IClient>> topic : topics.entrySet())
			showSubscribersOfOneTopic(clientId,topic.getKey());
	}

	
	


	public static void main(String args[]) {
		Server server = new Server();
		System.out.println("Private ip: " + server.getId());
		Scanner scanner=new Scanner(System.in);
		System.out.println("Insert the server name you want to create:");
		try {
			server.bindToRegistry(scanner.nextLine());
		} catch(RemoteException e) {
			System.out.println(e.getMessage());
			System.exit(0);
		}
		scanner.close();
	}
	
	
	public void bindToRegistry(String serverName) throws RemoteException {
		try {
			registry.rebind(serverName,(IServer)UnicastRemoteObject.exportObject(this,0));
			System.out.println("It works!\n");
			} catch (RemoteException e) {
			}
		}
	
	
	
	
	public void notifyClient(String message) throws RemoteException {
		System.out.println(message);
		
	}
	
	
	public void sendMessage(TopicMessage message) throws RemoteException {
		System.out.println(message);
		for(String clientId : connectedClients.keySet())
			connectedClients.get(clientId).sendMessage(message);
	}

	
	public void getTopicList(Set<String> topics) throws RemoteException {	
		System.out.println(topics);
	}
	
	
	public String getId() {
	    return privateIp;
	}
  
}
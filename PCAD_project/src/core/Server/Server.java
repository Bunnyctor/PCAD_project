package core.Server;

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
	private Registry registry;
	private AbstractMap<String,IClient> connectedClients;
	private AbstractMap<String,List<IClient>> topics;
  
	public Server() {
		this(8000);
		}
	
	public Server(int port) {
		connectedClients = new ConcurrentHashMap<>();
		topics = new ConcurrentHashMap<>();
		System.setProperty("java.security.policy","file:./sec.policy");
		System.setProperty("java.rmi.server.codebase","file:${workspace_loc}/Server/");
		if(System.getSecurityManager()==null)		System.setSecurityManager(new SecurityManager());
		System.setProperty("java.rmi.server.hostname","localhost");
		try {
			registry = LocateRegistry.createRegistry(port);
			System.out.println("Registry created at port "+port);
			} catch (RemoteException e) {
				if (e.getMessage().contains("Port already in use"))
					System.out.println("Port already in use. Trying to connect to it...");
					try {
						registry = LocateRegistry.getRegistry(port);
						System.out.println("Registry found at port "+port);
					} catch (RemoteException e1) {
						System.out.println("Registry cannot be created at port "+port);
					}
					}
		System.out.println("Server created at registry port "+port);
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
		stub.notifyClient("Hand-shake ok");
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
		if (!topics.containsKey(topic)) {
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
	public void getTopicList(String clientId) throws RemoteException {
		connectedClients.get(clientId).sendTopicList(topics.keySet());
	}


	@Override
	public void seeSubscribersOfOneTopic(String clientId, String topic) throws RemoteException {
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
	public void seeSubscribersOfAllTopics(String clientId) throws RemoteException {
		for(AbstractMap.Entry<String,List<IClient>> topic : topics.entrySet())
			seeSubscribersOfOneTopic(clientId,topic.getKey());
	}




	public static void main(String args[]) {
		Server server;
		System.out.println("Insert port number:");
		Scanner scanner=new Scanner(System.in);
		if(scanner.hasNextInt())
			server = new Server(scanner.nextInt());
		else {
			System.out.println("Port number was not an integer, so it has been randomized");
			server = new Server((int)(Math.random() * 10000));
		}
		scanner.close();
		try {
			server.getRegistry().rebind("REG", (IServer)UnicastRemoteObject.exportObject(server,0));
		} catch (RemoteException e) {
		}
		System.out.println("It works!\n");
	}


	public Registry getRegistry() {
		return registry;
	}
	
	
	
	
	
	
	
	
	
	
	public void notifyClient(String message) throws RemoteException {
		System.out.println(message);
		
	}
	
	
	public void sendMessage(TopicMessage message) throws RemoteException {
		System.out.println(message);
		for(String clientId : connectedClients.keySet())
			connectedClients.get(clientId).sendMessage(message);
	}

	
	public void sendTopicList(Set<String> topics) throws RemoteException {	
		System.out.println(topics);
	}
	
	
	
	
	
	

  
}
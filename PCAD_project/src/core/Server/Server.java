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
import java.util.concurrent.ConcurrentHashMap;

import core.Shared.IServer;
import core.Shared.IClient;
import core.Shared.TopicMessage;

public class Server implements IServer {
	private static final long serialVersionUID = 1L;
	private static Registry registry;
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
		System.setProperty("java.rmi.server.hostname","localhost");
		if(System.getSecurityManager()==null)		System.setSecurityManager(new SecurityManager());
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
						System.out.println("Registry cannot be create at port "+port);
					}
					}
		System.out.println("Server creato");
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
		System.out.println("Hand-shake ok with "+clientId);
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
		IClient cd = connectedClients.get(clientId);
		System.out.println("Client "+clientId+" disconnected");
		cd.notifyClient("Disconnecting..");
		for(List<IClient> clientList : topics.values())
			clientList.remove(cd);
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
		IClient cd = connectedClients.get(clientId);
		try {
			synchronized(clients) {
				if (!clients.contains(cd))	clients.add(cd);
				else	cd.notifyClient("You already subscribed the topic "+topic);
			}
		}
		catch(NullPointerException e) {
			cd.notifyClient("Topic with name "+topic+" does not exists");
		}
	}


	@Override
	public void unsubscribe(String clientId, String topic) throws RemoteException {
		List<IClient> clients = topics.get(topic);
		IClient cd = connectedClients.get(clientId);
		try {
			synchronized(clients) {
				if (clients.contains(cd))	clients.remove(cd);
				else	cd.notifyClient("You are not subscribed to the topic "+topic+'\n');
			}
		}
		catch(NullPointerException e) {
			cd.notifyClient("Topic with name "+topic+" does not exists");
		}
	}


	@Override
	public void publish(String clientId, TopicMessage message) throws RemoteException {
		if (topics.containsKey(message.getTopic()))
			for(IClient cl : topics.get(message.getTopic()))
				cl.sendMessage(message);
		else
			connectedClients.get(clientId).notifyClient("Topic with name "+message.getTopic()+" does not exist");
	}


	@Override
	public void getTopicList(String clientId) throws RemoteException {
		connectedClients.get(clientId).sendTopicList(topics.keySet());
	}


	@Override
	public void seeClientsOfOneTopic(String clientId, String topic) throws RemoteException {
		IClient cd = connectedClients.get(clientId);
		if(topics.containsKey(topic)) {
			cd.notifyClient("\nTopic: "+topic);
			for(IClient subscriber : topics.get(topic))
				for (AbstractMap.Entry<String,IClient> entry : connectedClients.entrySet())
					if (entry.getValue().equals(subscriber))
						cd.notifyClient(entry.getKey());
		}
		else	cd.notifyClient("Topic "+topic+" does not exists\n");
	}


	@Override
	public void seeClientsOfAllTopics(String clientId) throws RemoteException {
		for(AbstractMap.Entry<String,List<IClient>> topic : topics.entrySet())
			seeClientsOfOneTopic(clientId,topic.getKey());
	}




	public static void main(String args[]) {
		Server server;
		System.out.println("Insert port number:");
		Scanner scanner=new Scanner(System.in);
		if(scanner.hasNextInt())
			server = (Server)new Server(scanner.nextInt());
		else {
			System.out.println("Port number was not an integer, so it has been randomized");
			server = (Server)new Server((int)(Math.random() * 10000));
		}
		scanner.close();
		IServer stubRequest;
		try {
			stubRequest = (IServer)UnicastRemoteObject.exportObject(server,0);
			registry.rebind("REG", stubRequest);
		} catch (RemoteException e) {
		}
		System.out.println("It works!\n");
	}



  
}
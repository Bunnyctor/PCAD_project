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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import core.Shared.IServer;
import core.Shared.IClient;
import core.Shared.TopicMessage;

public class Server implements IServer {
	private static final long serialVersionUID = 1L;
	private static Registry r = null;
	private AbstractMap<String,IClient> connectedClients = new ConcurrentHashMap<>();
	private AbstractMap<String,List<IClient>> topics = new ConcurrentHashMap<>();
  
	public Server() {
		try {
			System.setProperty("java.security.policy","file:./sec.policy");
			System.setProperty("java.rmi.server.codebase","file:${workspace_loc}/Server/");
			System.setProperty("java.rmi.server.hostname","localhost");
			if(System.getSecurityManager()==null)		System.setSecurityManager(new SecurityManager());
			try {
				r = LocateRegistry.createRegistry(8000);
				System.out.println("Registro creato");
				} catch (RemoteException e) {
					if (e.getMessage().contains("Port already in use"))
						System.out.println("Port already in use. Trying to connect to it...");
						r = LocateRegistry.getRegistry(8000);
						System.out.println("Registro trovato");
						}
			}
		catch (Exception e) {
		}
		System.out.println("Server creato");
		}
  
  
	public synchronized void request(String clientId, IClient stub) throws RemoteException {
		System.out.println("Request dal client "+clientId);
		try {
			r.bind(clientId,stub);
		} catch (AlreadyBoundException e) {	
		}
		try {
			connectedClients.putIfAbsent(clientId,(IClient)r.lookup(clientId));
			System.out.println(connectedClients.toString());
		} catch (NotBoundException e) {
		}
		stub.notifyClient("hand-shake ok");
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
				else	cd.notifyClient("You already subscribed the topic "+topic+'\n');
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
		if (!topics.containsKey(message.getTopic()))
			connectedClients.get(clientId).notifyClient("Topic with name "+message.getTopic()+" does not exist");
		else
			for(IClient cl : topics.get(message.getTopic()))
				cl.sendMessage(message);
	}


	@Override
	public void getTopicList(String clientId) throws RemoteException {
		connectedClients.get(clientId).sendTopicList(topics.keySet());
	}


	@Override
	public Set<String> getTopicList() throws RemoteException {
		return topics.keySet();
	}



	@Override
	public void seeClientsOfOneTopic(String clientId, String topic) throws RemoteException {
		IClient cd = connectedClients.get(clientId);
		if(topics.get(topic)!=null) {
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
			Server server = (Server)new Server();
			IServer stubRequest;
			try {
				stubRequest = (IServer) UnicastRemoteObject.exportObject(server,0);
				r.rebind("REG", stubRequest);
			} catch (RemoteException e) {
			}
		    System.out.println("It works!\n");
	}



  
}
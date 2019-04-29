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
	  System.out.println("Server creato");
  }
  
  public synchronized void request(String clientId, IClient stub) throws RemoteException {
	  System.out.println("Request dal client "+clientId);
	  try {
		r.bind(clientId, stub);
	} catch (AlreadyBoundException e) {
	}
	  try {
		connectedClients.putIfAbsent(clientId,(IClient)r.lookup(clientId));	//<clientId,remoteObjRefToClientId>
		System.out.println(connectedClients.toString());
	} catch (NotBoundException e) {
	}
	  stub.notifyClient();
  }
  
  public static void main(String args[]) {
		try {
			System.setProperty("java.security.policy","file:./sec.policy");
			System.setProperty("java.rmi.server.codebase","file:${workspace_loc}/Server/");
			if(System.getSecurityManager() == null) System.setSecurityManager(new SecurityManager());
			System.setProperty("java.rmi.server.hostname","localhost");
			try {
				r = LocateRegistry.createRegistry(8000);
				System.out.println("Registro creato");
			} catch (RemoteException e) {
				if (e.getMessage().contains("Port already in use"))
		            System.out.println("Port already in use. Trying to connect to it...");
				r = LocateRegistry.getRegistry(8000);
				System.out.println("Registro trovato");
			}
			Server server = (Server)new Server();
			IServer stubRequest = (IServer) UnicastRemoteObject.exportObject(server,0);
			r.rebind("REG", stubRequest);
		    System.out.println("It works!\n");
		    
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	 }
  
  
  
  
@Override
public void createTopic(String clientId, String topic) throws RemoteException {
		if (!topics.containsKey(topic)) {
	  		topics.put(topic, new LinkedList<IClient>());
	  		subscribe(clientId,topic);
		}
		else {
			try {
				IClient cd = (IClient)r.lookup(clientId);
				cd.sendMessage("Topic with name "+topic+" already exists");
			} catch (NotBoundException e) {
			}
		}
}
  
@Override
public void publish(TopicMessage message) throws RemoteException {
	if (!topics.containsKey(message.getTopic()))
  		topics.put(message.getTopic(), new LinkedList<IClient>());
  	for(IClient cl : topics.get(message.getTopic()))				//se non mi iscrivo al topic, non ricevo messaggio
  		cl.sendMessage(message);
}



@Override
public void subscribe(String clientId, String topic) throws RemoteException {
	try {
		List<IClient> clients = topics.get(topic);
		IClient cd = (IClient)r.lookup(clientId);	
		synchronized(clients) {
			if(clients==null)
				cd.sendMessage("Topic with name "+topic+" does not exists");
			else if (!clients.contains(cd))
				clients.add(cd);
		}
	} catch (NotBoundException e) {
	}
	
}


@Override
public void unsubscribe(String clientId, String topic) throws RemoteException {
	try {
		IClient cd = (IClient)r.lookup(clientId);
		List<IClient> clients = topics.get(topic);
		synchronized(clients) {
			if (clients != null && clients.contains(cd))
				clients.remove(cd);
		}
	} catch (NotBoundException e) {
	}
}

/*
public void notifyClient() throws RemoteException {
	System.out.println("hand-shake ok!");
}

public void sendTopicList(Set<String> topics) throws RemoteException {
	
}*/

@Override
public Set<String> getTopicList() throws RemoteException {
	return topics.keySet();
}

@Override
public void getTopicList(String clientId) throws RemoteException {
	connectedClients.get(clientId).sendTopicList(topics.keySet());
}

public void sendMessage(TopicMessage msg) throws RemoteException {
}



public void printClientList(String clientId) throws RemoteException {
	IClient cd;
	try {
		cd = (IClient)r.lookup(clientId);
	} catch (NotBoundException e) {
		return;
	}
	for(AbstractMap.Entry<String,List<IClient>> topic : topics.entrySet()) {
		cd.sendMessage("\nTopic: "+topic.getKey());
		for(IClient subscriber : topic.getValue())
			for (AbstractMap.Entry<String,IClient> entry : connectedClients.entrySet())
				if (entry.getValue().equals(subscriber))
					cd.sendMessage(entry.getKey());
	}
	System.out.println('\n');
}



  
}
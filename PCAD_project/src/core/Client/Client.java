package core.Client;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import core.Shared.TopicMessage;
import core.Shared.IClient;
import core.Shared.IServer;

public class Client implements IClient {
   
	private static final long serialVersionUID = 1L;
    private IClient stub;
    private IServer server;
    private Scanner sc;
    private String topic, message;
    private List<TopicMessage> messages = new LinkedList<TopicMessage>();
    private ConcurrentHashMap<String,List<TopicMessage>> topicMess = new ConcurrentHashMap<>();
   
	public Client(String clientId) {
		try {
		System.setProperty("java.security.policy","file:./sec.policy");
		System.setProperty("java.rmi.server.codebase","file:${workspace_loc}/Client/");
		if (System.getSecurityManager() == null)	System.setSecurityManager(new SecurityManager());
		System.setProperty("java.rmi.server.hostname","localhost");
		//Registry r = LocateRegistry.getRegistry("localhost",8000);
		Registry r = LocateRegistry.getRegistry(8000);
		server = (IServer)r.lookup("REG");
		stub = (IClient)UnicastRemoteObject.exportObject(this,0);
		server.request(clientId,stub);
		sc=new Scanner(System.in);

		while(true) {
			menu();
			topic=sc.nextLine();
			switch(topic) {
			case("1"):
				server.getTopicList(clientId);
				break;
			case("2"):
				System.out.println("Create topic");
				topic=sc.nextLine();
				server.createTopic(clientId,topic);
				break;
			case("3"):
				System.out.println("Insert topic");
				topic=sc.nextLine();
				System.out.println("Insert post");
				message=sc.nextLine();
				server.publish(clientId,new TopicMessage(topic,message,clientId));
				break;
			case("4"):
				System.out.println("Insert topic to subscribe");
				topic=sc.nextLine();
				server.subscribe(clientId,topic);
				break;
			case("5"):
				System.out.println("Insert topic to unsubscribe");
				topic=sc.nextLine();
				server.unsubscribe(clientId,topic);
				break;
			case("6"):
				System.out.println("Not yet implemented");
				break;
			case("7"):
				System.out.println("Not yet implemented");
				break;
			case("8"):
				System.out.println("Insert topic");
				topic=sc.nextLine();
				server.seeClientsOfOneTopic(clientId,topic);
				break;
			case("9"):
				server.seeClientsOfAllTopics(clientId);
				break;
			case("quit"):
				System.out.println("\nQuitting..");
				return;
			default:
				break;
			}
			System.out.println("Press enter to continue");
			sc.nextLine();
		}
		} catch (RemoteException | NotBoundException e) {
		}
	}
	
	
	
	private void menu() {
		System.out.println("Press:");
		System.out.println("1 \tGet all topics");
		System.out.println("2 \tCreate topic");
		System.out.println("3 \tPublish post into a topic");
		System.out.println("4 \tSubscribe a topic");
		System.out.println("5 \tUnsubscribe from a topic");
		System.out.println("6 \tSee all posts");
		System.out.println("7 \tSee all posts of a topic");
		System.out.println("8 \tSee subscribers of a topic");
		System.out.println("9 \tSee subscribers of all topics");
	}
		
	public void notifyClient(String message) throws RemoteException {
		System.out.println(message);
	}


	@Override
	public void sendMessage(TopicMessage msg) throws RemoteException {
		messages.add(msg);
		/*topicMess.putIfAbsent(msg.getTopic(), new LinkedList<MessageT>());
		List<MessageT> aux=topicMess.get(msg.getTopic());
		synchronized(aux) {
			aux.add(msg);
		}*/
		synchronized(topicMess) {
			topicMess.putIfAbsent(msg.getTopic(), new LinkedList<TopicMessage>());
			System.out.println(msg.toString());
		}
	}

	@Override
	public void sendTopicList(Set<String> topics) throws RemoteException {	
		System.out.println(topics);
	}

	
	
	
	
	
	
	public static void main(String args[]) {
		String clientId = Integer.toString((int)(Math.random() * 1000));
		System.out.println("Client "+clientId);
		new Client(clientId) ;
		}
	
	
	
}
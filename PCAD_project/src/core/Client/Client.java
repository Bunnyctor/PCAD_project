package core.Client;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import core.Shared.TopicMessage;
import core.Shared.IClient;
import core.Shared.IServer;

public class Client implements IClient {
   
	private static final long serialVersionUID = 1L;
    private IClient stub;
    private IServer server;
    private String clientId;
    

   
	public Client() {
		System.setProperty("java.security.policy","file:./sec.policy");
		System.setProperty("java.rmi.server.codebase","file:${workspace_loc}/Client/");
		if (System.getSecurityManager() == null)	System.setSecurityManager(new SecurityManager());
		System.setProperty("java.rmi.server.hostname","localhost");
		clientId = Integer.toString((int)(Math.random() * 1000));
		try {
			//Registry r = LocateRegistry.getRegistry("localhost",8000);
			Registry r = LocateRegistry.getRegistry(8000);
			server = (IServer)r.lookup("REG");
			stub = (IClient)UnicastRemoteObject.exportObject(this,0);
			server.request(clientId,stub);
		} catch (RemoteException | NotBoundException e) {
		}
	}
	
	
	
	private static void menu() {
		System.out.println("Press:");
		System.out.println("1 \tGet all topics");
		System.out.println("2 \tCreate topic");
		System.out.println("3 \tPublish post into a topic");
		System.out.println("4 \tSubscribe a topic");
		System.out.println("5 \tUnsubscribe from a topic");
		System.out.println("6 \tSee subscribers of a topic");
		System.out.println("7 \tSee subscribers of all topics\n");
	}
		
	public void notifyClient(String message) throws RemoteException {
		System.out.println(message);
	}


	@Override
	public void sendMessage(TopicMessage message) throws RemoteException {
		System.out.println(message.toString());
	}

	@Override
	public void sendTopicList(Set<String> topics) throws RemoteException {	
		System.out.println(topics);
	}


	
	
	public static void main(String args[]) {
		Client client = new Client();
		System.out.println("Client "+client.getClientId());
		
		try {
			Scanner scanner=new Scanner(System.in);
			String topic, message;
			String clientId=client.getClientId();
			IServer server=client.getServer();
	
			while(true) {
				menu();
				topic=scanner.nextLine();
				switch(topic) {
				case("1"):
					server.getTopicList(clientId);
					break;
				case("2"):
					System.out.println("Create topic");
					topic=scanner.nextLine();
					server.createTopic(clientId,topic);
					break;
				case("3"):
					System.out.println("Insert topic");
					topic=scanner.nextLine();
					System.out.println("Insert post");
					message=scanner.nextLine();
					server.publish(clientId,new TopicMessage(topic,message,clientId));
					break;
				case("4"):
					System.out.println("Insert topic to subscribe");
					topic=scanner.nextLine();
					server.subscribe(clientId,topic);
					break;
				case("5"):
					System.out.println("Insert topic to unsubscribe");
					topic=scanner.nextLine();
					server.unsubscribe(clientId,topic);
					break;
				case("6"):
					System.out.println("Insert topic");
					topic=scanner.nextLine();
					server.seeClientsOfOneTopic(clientId,topic);
					break;
				case("7"):
					server.seeClientsOfAllTopics(clientId);
					break;
				case("quit"):
					System.out.println("\nQuitting..");
					scanner.close();
					return;
				default:
					System.out.println("Invalid choice");
					break;
				}
				System.out.println("\nPress enter to continue");
				scanner.nextLine();
				}
			} catch (RemoteException e) {	
			}
	}
	
	
	
    public IServer getServer() {
    	return server;
    }
    
    public String getClientId() {
    	return clientId;
    }
	
}
package core.Client;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import core.Shared.TopicMessage;
import core.Shared.IClient;
import core.Shared.IServer;

public class Client implements IClient {
   
	private static final long serialVersionUID = 1L;
    private String clientId;
    private IClient stub;
    private IServer server;
    

   
	public Client() {
		System.setProperty("java.security.policy","file:./sec.policy");
		System.setProperty("java.rmi.server.codebase","file:${workspace_loc}/Client/");
		if (System.getSecurityManager() == null)	System.setSecurityManager(new SecurityManager());
		System.setProperty("java.rmi.server.hostname","localhost");
		clientId = Integer.toString((int)(Math.random() * 1000));
	}
	
	private void connectToServer(String serverName) throws Exception {
		try {
			server = (IServer)LocateRegistry.getRegistry("192.168.1.102",8000).lookup(serverName);
			} catch (NotBoundException e) {
				throw new Exception("Server could not be found");
			}
		stub = (IClient)UnicastRemoteObject.exportObject(this,0);
		try {
			server.connect(this.getClientId(),stub);
			} catch (RemoteException e) {
				throw new Exception("Server could not be connected");
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
		System.out.println("7 \tSee subscribers of all topics");
		System.out.println("quit \tDisconnect from server\n");
	}
	
	@Override
	public void notifyClient(String message) throws RemoteException {
		System.out.println(message);
	}


	@Override
	public void sendMessage(TopicMessage message) throws RemoteException {
		System.out.println(message);
	}

	@Override
	public void getTopicList(Set<String> topics) throws RemoteException {	
		System.out.println(topics);
	}


	
	
	public static void main(String args[]) {
		Client client = new Client();
		String clientId=client.getClientId();
		System.out.println("Client "+clientId);
		Scanner scanner=new Scanner(System.in);
		System.out.println("Insert the server you want to connect to:");
		try {
			client.connectToServer(scanner.nextLine());
		} catch (Exception e) {
			System.out.println(e.getMessage());
			scanner.close();
			System.exit(0);
		}
		
		
		try {
			String choice, topic, message;
			IServer server=client.getServer();
	
			while(true) {
				menu();
				choice=scanner.nextLine();
				switch(choice) {
				case("1"):
					server.sendTopicList(clientId);
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
					server.sendSubscribersOfOneTopic(clientId,topic);
					break;
				case("7"):
					server.sendSubscribersOfAllTopics(clientId);
					break;
				case("quit"):
					try {
						server.disconnect(clientId);
					} catch(RemoteException e) {
						break;
					}
					scanner.close();
					System.exit(0);
				default:
					System.out.println("Invalid choice");
					break;
				}
				System.out.println("\nPress enter to continue");
				scanner.nextLine();
				}
			} catch (RemoteException e) {	
				System.out.println("Client main had a problem\n");
			}
	}
	
	
	
    public String getClientId() {
    	return clientId;
    }
    
    public IClient getStub() {
    	return stub;
    }
	
    public IServer getServer() {
    	return server;
    }
    
	
}
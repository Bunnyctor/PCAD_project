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
    private String id;
    private IServer serverToConnect;
    

   
	public Client() {
		id = Integer.toString((int)(Math.random() * 1000));
		serverToConnect=null;
	}
	
	private static void setProperty(String ip) {
		System.setProperty("java.security.policy","file:./sec.policy");
		System.setProperty("java.rmi.server.codebase","file:${workspace_loc}/Client/");
		if (System.getSecurityManager() == null)	System.setSecurityManager(new SecurityManager());
		System.setProperty("java.rmi.server.hostname",ip);
	}
	
	private void connectToServer(String serverIp, String serverName) throws Exception {
		try {
			serverToConnect = (IServer)LocateRegistry.getRegistry(serverIp,8000).lookup(serverName);
			} catch (NotBoundException e) {
				throw new Exception("Server could not be found");
			}
		try {
			serverToConnect.connect(this.id,(IClient)UnicastRemoteObject.exportObject(this,0));
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
		System.out.println("ClientId "+client.id);
		Scanner scanner=new Scanner(System.in);
		try {
			System.out.println("Insert the server IP you want to connect to:");
			String serverIp = scanner.nextLine();
			setProperty(serverIp);
			System.out.println("Insert the server name you want to connect to:");
			client.connectToServer(serverIp,scanner.nextLine());
		} catch (Exception e) {
			System.out.println(e.getMessage());
			scanner.close();
			System.exit(0);
		}
		
		
		try {
			String choice,topic;
	
			while(true) {
				menu();
				choice=scanner.nextLine();
				switch(choice) {
				case("1"):
					client.serverToConnect.showTopicList(client.id);
					break;
				case("2"):
					System.out.println("Create topic");
					client.serverToConnect.createTopic(client.id,scanner.nextLine());
					break;
				case("3"):
					System.out.println("Insert topic");
					topic=scanner.nextLine();
					System.out.println("Insert post");
					client.serverToConnect.publish(client.id,new TopicMessage(topic,scanner.nextLine(),client.id));
					break;
				case("4"):
					System.out.println("Insert topic to subscribe");
					topic=scanner.nextLine();
					client.serverToConnect.subscribe(client.id,topic);
					break;
				case("5"):
					System.out.println("Insert topic to unsubscribe");
					topic=scanner.nextLine();
					client.serverToConnect.unsubscribe(client.id,topic);
					break;
				case("6"):
					System.out.println("Insert topic");
					topic=scanner.nextLine();
					client.serverToConnect.showSubscribersOfOneTopic(client.id,topic);
					break;
				case("7"):
					client.serverToConnect.showSubscribersOfAllTopics(client.id);
					break;
				case("quit"):
					client.serverToConnect.disconnect(client.id);
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
				System.exit(0);
			}
	}
    
	
}
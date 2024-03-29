package core.Client;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import core.Shared.*;

public class Client implements IClient {
   
	private static final long serialVersionUID = 1L;
    private String id;
    private IServer serverToConnect;
    private Registry registry;
    

   
	public Client() {
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
		finally {
			id=Integer.toString((int)(Math.random()*1000));
			serverToConnect=null;
		}
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
			client.bindInRegistry(client.id);
		} catch(RemoteException e) {
			System.out.println(e.getMessage());
			scanner.close();
			System.exit(0);
		}
		
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
			String topic;
	
			while(client.serverToConnect!=null) {
				menu();
				switch(scanner.nextLine()) {
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
				case("Quit"):
					try {
						client.disconnectFromServer();
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
					finally {
						scanner.close();
						System.exit(0);
					}
				default:
					System.out.println("Invalid choice");
					break;
				}
				System.out.println("\nPress enter to continue");
				scanner.nextLine();
				}
			} catch (RemoteException e) {	
				System.out.println("Server could not be reached");
				scanner.close();
				System.exit(0);
			}
	}
    
	
	
	
	
	private static void menu() {
		System.out.println("Type:");
		System.out.println("1 \tGet all topics");
		System.out.println("2 \tCreate topic");
		System.out.println("3 \tPublish post into a topic");
		System.out.println("4 \tSubscribe a topic");
		System.out.println("5 \tUnsubscribe from a topic");
		System.out.println("6 \tSee subscribers of a topic");
		System.out.println("7 \tSee subscribers of all topics");
		System.out.println("Quit \tDisconnect from server\n");
	}
	
	
	private static void setProperty(String ip) {
		System.setProperty("java.security.policy","file:./sec.policy");
		System.setProperty("java.rmi.server.codebase","file:${workspace_loc}/Client/");
		if (System.getSecurityManager()==null)	System.setSecurityManager(new SecurityManager());
		System.setProperty("java.rmi.server.hostname",ip);
	}
	
	
	private void connectToServer(String serverIp, String serverNameToConnect) throws Exception {
		try {
			serverToConnect = (IServer)LocateRegistry.getRegistry(serverIp,8000).lookup(serverNameToConnect);
			} catch (NotBoundException e) {
				throw new Exception("Server could not be found");
			}
		try {
			serverToConnect.connect(this.id,(IClient)UnicastRemoteObject.toStub(this));
			} catch (RemoteException e) {
				throw new Exception("Server could not be connected");
			}
	}
	
	
	private void disconnectFromServer() throws Exception {
		try {
			this.serverToConnect.disconnect(this.id);
		} catch (RemoteException e) {
			throw new Exception ("Server could not be disconnected");
		} finally {
			this.serverToConnect=null;
		}
	}
	
	
	public void bindInRegistry(String clientId) throws RemoteException {
		try {
			registry.rebind(clientId,(IClient)UnicastRemoteObject.exportObject(this,0));
			} catch (RemoteException e) {
				throw e;
		}
	}
	
}
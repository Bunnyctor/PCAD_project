package core.Shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;
import java.io.Serializable;


public interface IClient extends Remote,Serializable {
	public void notifyClient(String message) throws RemoteException;
	public void sendMessage(TopicMessage message) throws RemoteException;
	public void sendTopicList(Set<String> topics) throws RemoteException;
}
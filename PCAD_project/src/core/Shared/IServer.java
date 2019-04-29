package core.Shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

import core.Shared.IClient;
import core.Shared.TopicMessage;

import java.io.Serializable;

public interface IServer extends Remote,Serializable {
	public void request(String clientId, IClient stub) throws RemoteException;
	public void createTopic(String clientId, String topic) throws RemoteException;
	public void subscribe(String clientId, String topic) throws RemoteException;
	public void unsubscribe(String clientId, String topic) throws RemoteException;
	public void publish(String clientId, TopicMessage message)throws RemoteException;
	public void getTopicList(String client)throws RemoteException;
	public Set<String> getTopicList()throws RemoteException;
	public void printClientList(String clientId) throws RemoteException;
}

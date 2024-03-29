package core.Shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

import core.Shared.IClient;
import core.Shared.TopicMessage;

import java.io.Serializable;

public interface IServer extends Remote,Serializable {
	public void connect(String clientId, IClient stub) throws RemoteException;
	public void disconnect(String clientId) throws RemoteException;
	public void createTopic(String clientId, String topic) throws RemoteException;
	public void subscribe(String clientId, String topic) throws RemoteException;
	public void unsubscribe(String clientId, String topic) throws RemoteException;
	public void publish(String clientId, TopicMessage message)throws RemoteException;
	public void showTopicList(String clientId) throws RemoteException;
	public void showSubscribersOfOneTopic(String clientId, String topic) throws RemoteException;
	public void showSubscribersOfAllTopics(String clientId) throws RemoteException;
}
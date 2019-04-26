package core.Shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

import core.Shared.IClient;
import core.Shared.TopicMessage;

import java.io.Serializable;

public interface IServer extends Remote,Serializable {
	public void request(String x, IClient stub) throws RemoteException;
	public void subscribe(String topic, String name) throws RemoteException;
	public void unsubscribe(String topic, String clientId) throws RemoteException;
	public void publish(TopicMessage msg)throws RemoteException;
	public void getTopicList(String client)throws RemoteException;
	public Set<String> getTopicList()throws RemoteException;
	public void printClientList() throws RemoteException;
}

package core.Server;

import core.Shared.IClient;

public class ClientDetails {
	public String name;
	public IClient client;
	
	public ClientDetails(String name, IClient client) {
		this.name=name;
		this.client=client;		
	}
	
}

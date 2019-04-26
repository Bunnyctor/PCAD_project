package core.Shared;
import java.io.Serializable;
import java.util.Date;

public class TopicMessage implements Serializable{

	private static final long serialVersionUID = 1L;
	private String topic;
	private String from;
	private String message;
	private Date date;
	
	
	public TopicMessage(String topic,String mess,String from) {
		this.topic=topic;
		this.from=from;
		this.message=mess;
		this.date=new Date();
	}

	public String getTopic() {
		return topic;
	}
	
	public String getFrom() {
		return from;
	}
	
	public String getMess() {
		return message;
	}

	public String toString() {
		return "\nTopic: "+topic+"\nFrom: "+from+"\nMessage: "+message+"\nDate: "+date+'\n';	
	}
	
}
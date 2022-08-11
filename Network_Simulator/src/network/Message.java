package network;

public class Message {
	//message for remote agents
	Double[] msg;
	
	public Message(Double[] m) {
		this.msg=m;
	}
	
	public Double[] getmessage(){
		return msg;
	}
}

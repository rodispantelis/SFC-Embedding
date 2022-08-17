package network;

/** generates message objects for communication with the agents */

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

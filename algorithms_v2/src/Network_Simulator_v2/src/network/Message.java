package network;

/** generates message objects for communication with the agents 
 * the use of the messages should be defined in the agent objects. */

public class Message {
	/** message in Double array format */
	Double[] msg;
	
	public Message(Double[] m) {
		this.msg=m;
	}

	/** return message */
	public Double[] getmessage(){
		return msg;
	}
}

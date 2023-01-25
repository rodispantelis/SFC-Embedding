package functions;

public class Functions {
	public Object[] activation;
	
	/** Generates an array of function objects. **/
	public Functions(){
		Bipolarsigm b=new Bipolarsigm();
		Hypertansigm h=new Hypertansigm();
		Linear l=new Linear();
		ReLU r=new ReLU();
		Sigm s=new Sigm();
		Threshold t=new Threshold();
		this.activation= new Object[]{b, h, l, r, s, t};
	}
}

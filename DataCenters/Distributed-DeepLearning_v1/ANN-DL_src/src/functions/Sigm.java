package functions;

public class Sigm {

	public Sigm() {
		
	}
	
	/** Sigmoid function. f(x)=1/(1+exp(-x)) **/
	public double activation(double x) {
		double y=1/(1+Math.exp(-x));
		return y;
	}
}

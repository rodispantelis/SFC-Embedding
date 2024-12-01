package functions;

public class Bipolarsigm {

	public Bipolarsigm() {
		
	}

	/** Bipolar sigmoid function. f(x)=(1-exp(x))/(1+exp(x)) **/
	public double activation(double x) {
		
		double y=(1-Math.exp(x))/(1+Math.exp(x));
		return y;
	}
}

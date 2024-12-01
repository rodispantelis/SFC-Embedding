package functions;

public class Hypertansigm {

	public Hypertansigm() {
		
	}
	
	/** Hyperbolic Tangent Function. f(x)=(1-exp(-2*x))/(1+exp(-2*x)) **/
	public double activation(double x) {

		double y=(1-Math.exp(-2*x))/(1+Math.exp(-2*x));
		return y;
	}
}

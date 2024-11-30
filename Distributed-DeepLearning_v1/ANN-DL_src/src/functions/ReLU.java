package functions;

public class ReLU {

	public ReLU() {
		
	}
	
	/** Rectified Linear Unit Function. f(x)=max(x,0) **/
	public double activation(double x) {
		
		double y=Math.max(x,0);
		return y;
	}
}

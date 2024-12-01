package functions;

public class Threshold {

	public Threshold() {
		
	}
	
	/** Activation threshold. Return 0 for x<1 otherwise return 1. **/
	public double activation(double x) {

		if(x<1) {
			return 0;
		}else {
			return 1;
		}
	}
}

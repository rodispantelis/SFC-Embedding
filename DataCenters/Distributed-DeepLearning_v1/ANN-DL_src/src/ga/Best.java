package ga;

/** stores the best fitness & mapping **/
public class Best {
	/** best mapping so far **/
	private String bestphenotype[];	
	/** fitness for the above mapping **/
	private double bestfitness;		
	
	public Best(int f){
		this.bestphenotype=new String[f];
		for(int i=0;i<f;i++){
			bestphenotype[i]=Integer.toString(0);
		}
	}
	
	/** get best fitness **/
	public double getbestfitness(){
		return bestfitness;
	}
	
	/** set best fitness **/
	public void setbestfitness(double newfit){
		bestfitness=newfit;
	}
	
	/** get best solution (phenotype) **/
	public int[] getbestphenotype(){
		int[] bmap=new int[bestphenotype.length];
		for(int k=0;k<bestphenotype.length;k++){
			bmap[k]=Integer.parseInt(bestphenotype[k]);
		}
		return bmap;
	}

	/** set best solution (phenotype) **/
	public void setbestphenotype(int[] ma){
		bestphenotype=new String[ma.length];
		for(int i=0;i<ma.length;i++){
			bestphenotype[i]=Integer.toString(ma[i]);
		}
	}
}

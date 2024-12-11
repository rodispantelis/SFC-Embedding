package sfc_ft_b;

/** store the best fitness and solution */

public class Best_b {
	/** current best solution */
	private String bestphenotype[];
	/** fitness of current best solution */
	private double bestfitness;
	
	public Best_b(int f){
		this.bestphenotype=new String[f];
		for(int i=0;i<f;i++){
			bestphenotype[i]=Integer.toString(0);
		}
	}
	
	/** get best fitness */
	public double getbestfitness(){
		return bestfitness;
	}
	
	/** set best fitness */
	public void setbestfitness(double newfit){
		bestfitness=newfit;
	}
	
	/** get best solution */
	public int[] getbestphenotype(){
		int[] bmap=new int[bestphenotype.length];
		for(int k=0;k<bestphenotype.length;k++){
			bmap[k]=Integer.parseInt(bestphenotype[k]);
		}
		return bmap;
	}
	
	/** set best solution */
	public void setbestphenotype(int[] ma){
		bestphenotype=new String[ma.length];
		for(int i=0;i<ma.length;i++){
			bestphenotype[i]=Integer.toString(ma[i]);
		}
	}
}

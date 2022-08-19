package sfc_b;

public class Best_b {
	//stores the best fitness & mapping
	private String bestphenotype[];	//best mapping so far
	private double bestfitness;		//fitness for the above mapping
	
	public Best_b(int f){
		this.bestphenotype=new String[f];
		for(int i=0;i<f;i++){
			bestphenotype[i]=Integer.toString(0);
		}
	}
	
	public double getbestfitness(){
		return bestfitness;
	}
	
	public void setbestfitness(double newfit){
		bestfitness=newfit;
	}
	
	public int[] getbestphenotype(){
		int[] bmap=new int[bestphenotype.length];
		for(int k=0;k<bestphenotype.length;k++){
			bmap[k]=Integer.parseInt(bestphenotype[k]);
		}
		return bmap;
	}
	
	public void setbestphenotype(int[] ma){
		bestphenotype=new String[ma.length];
		for(int i=0;i<ma.length;i++){
			bestphenotype[i]=Integer.toString(ma[i]);
		}
	}
}

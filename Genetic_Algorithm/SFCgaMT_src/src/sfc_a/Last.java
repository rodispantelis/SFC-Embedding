package sfc_a;

	/** Store mapping to which the population has last converged */
public class Last {
	/** Best mapping for this generation */
	private String lastmapping[];
	/** Best fitness on the above mapping */
	private double lastfitness=0;
	
	public Last(){
		this.lastfitness=100000;
	}
	
	/** get last best fitness */
	public double getlastfitness(){
		return lastfitness;
	}
	
	/** set last best fitness */
	public void setlastfitness(double newfit){
		lastfitness=newfit;
	}
	
	/** get last best mapping */
	public int[] getlastmapping(){
		int[] bmap=new int[lastmapping.length];
		for(int k=0;k<lastmapping.length;k++){
			bmap[k]=Integer.parseInt(lastmapping[k]);
		}
		return bmap;
	}
	
	/** set last best mapping */
	public void setlastmapping(int[] ma){
		lastmapping=new String[ma.length];
		for(int i=0;i<ma.length;i++){
			lastmapping[i]=Integer.toString(ma[i]);
		}
	}
}

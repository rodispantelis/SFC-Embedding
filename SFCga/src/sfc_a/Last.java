package sfc_a;

public class Last {
	//Store mapping to which the population has last converged
	private String lastmapping[];//Best mapping for this generation
	private double lastfitness=0;//Best fitness on the above mapping
	
	public Last(){
		this.lastfitness=100000;
	}
	
	public double getlastfitness(){
		return lastfitness;
	}
	
	public void setlastfitness(double newfit){
		lastfitness=newfit;
	}
	
	public int[] getlastmapping(){
		int[] bmap=new int[lastmapping.length];
		for(int k=0;k<lastmapping.length;k++){
			bmap[k]=Integer.parseInt(lastmapping[k]);
		}
		return bmap;
	}
	public void setlastmapping(int[] ma){
		lastmapping=new String[ma.length];
		for(int i=0;i<ma.length;i++){
			lastmapping[i]=Integer.toString(ma[i]);
		}
	}
}

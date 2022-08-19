package sfc_c;

public class Last_c {
	//Store mapping to which the population has last converged
	private String lastmapping[];//Best mapping for this generation
	private int lastfitness=0;//Best fitness on the above mapping
	
	public Last_c(){
		this.lastfitness=100000;
	}
	
	public int getlastfitness(){
		return lastfitness;
	}
	
	public void setlastfitness(int newfit){
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

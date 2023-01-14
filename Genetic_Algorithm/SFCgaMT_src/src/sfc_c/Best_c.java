package sfc_c;

/** store the best fitness and mapping */

public class Best_c {
	/** current best mapping */
	private String bestmapping[];
	/** fitness of current best mapping */
	private double bestfitness=0;
	/** available capacity */
	private int[] avcap= new int[2];
	
	public Best_c(int f){
		this.bestfitness=-1;
		this.bestmapping=new String[f];
		for(int i=0;i<f;i++){
			bestmapping[i]=Integer.toString(i);
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
	
	/** set available capacity */
	public void setavcap(int ind, int c) {
		avcap[ind]=c;
	}
	
	/** get available capacity */
	public int[] getavcap() {
		return avcap;
	}
	
	/** statistics */
	public void stats(){
		String temp[]=bestmapping.clone();
		int cnt=0;
		for(int i=0;i<temp.length;i++) {
			boolean c=true;
			for(int j=(i+1);j<temp.length;j++) {
				if(!(temp[j].equals("a")) && temp[i].equals(temp[j])) {			
					cnt++;
					temp[j]="a";
					if(c) {
						cnt++;
						c=false;
					}
				}
			}
		}
		avcap[0]=bestmapping.length;
		avcap[1]=cnt;
	}
	
	/** get best solution mapping */
	public int[] getbestmapping(){
		int[] bmap=new int[bestmapping.length];
		for(int k=0;k<bestmapping.length;k++){
			bmap[k]=Integer.parseInt(bestmapping[k]);
		}
		return bmap;
	}
	
	/** set best solution mapping */
	public void setbestmapping(int[] ma){
		bestmapping=new String[ma.length];
		for(int i=0;i<ma.length;i++){
			bestmapping[i]=Integer.toString(ma[i]);
		}
	}
}

package sfc_a;

public class Best {
	//Store the best fitness & mapping
	private String bestmapping[];//Best mapping so far
	private double bestfitness=0;//The fitness for the above mapping
	private int[] avcap= new int[2];
	
	public Best(int f){
		this.bestfitness=-1;
		this.bestmapping=new String[f];
		for(int i=0;i<f;i++){
			bestmapping[i]=Integer.toString(i);
		}
	}
	
	public double getbestfitness(){
		return bestfitness;
	}
	
	public void setbestfitness(double newfit){
		bestfitness=newfit;
	}
	
	public void setavcap(int ind, int c) {
		avcap[ind]=c;
	}
	
	public int[] getavcap() {
		return avcap;
	}
	
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
	
	public int[] getbestmapping(){
		int[] bmap=new int[bestmapping.length];
		for(int k=0;k<bestmapping.length;k++){
			bmap[k]=Integer.parseInt(bestmapping[k]);
		}
		return bmap;
	}
	public void setbestmapping(int[] ma){
		bestmapping=new String[ma.length];
		for(int i=0;i<ma.length;i++){
			bestmapping[i]=Integer.toString(ma[i]);
		}
	}
}

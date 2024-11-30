package nnetwork;

/** Encodes and decodes graph in the Edge Vector format. **/
public class Codec {
	
	//numbering starts from 0
	public Codec() {
	}
	
	/** On input of two nodes, you get the place of their edge on the vector. **/
	public int coder(int a, int b){
		int x=0;
		if(a==b){
			System.out.println("error"+a+"|"+b);
			}else{
				if(a>b){
					int t=a;
					a=b;
					b=t;
				}
				x=((b-1)*(1+(b-1)))/2;
			}
		return x+a;

	}
	
	/** On input of one place of the vector that denotes one edge of the graph
	you get the two nodes that define the edge **/
	public int[] decoder(int q){

		int x=0;
		int y=0;
		int result[]=new int[2];

		y=(int) Math.sqrt((2*q));
		y++;
		x=q-(((y*y)-y)/2);
		
		if(x<0){
			y--;
			x=q-(((y*y)-y)/2);
		}
		
		if(x<y) {
			result[0]=x;
			result[1]=y;
		}else {
			result[0]=y;
			result[1]=x;
		}

		return result;
	}
}

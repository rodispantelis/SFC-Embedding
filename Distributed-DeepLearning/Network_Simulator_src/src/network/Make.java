package network;

/** generates network */

public class Make {
	/** network object */
	Network net;
	/** network type */
	String type;
	/** network elements */
	int racks=0, switches=0, links=0, servers=0;
	/** servers per rack */
	int servperrack=0;								
	Codec cod=new Codec();							//Edge Vector coder-decoder
	/** path to store log files */
	String path="";
	/** k parameter different usage in different topologies */
	int k;											
	
	public Make() {

	}
	
	/** return generated network */
	public Network getnet() {
		return net;
	}
	
	/** make 3-tier fat-tree network given k parameter and servers per rack*/
	public void makefattree(int tk, int tservperrack) {
		//network parameters
		k=tk;
		servperrack=k/2;
		type="fat-tree";								
		racks=k*(k/2);								//number of racks
		servers=(int)(Math.pow(k,3.0)/4.0);			//number of servers
		switches=(k/2)*(k/2)+(k*k);					//number of switches=core + aggregation + edge
		int nodes=switches+servers;					//network nodes; switches and servers
		links=nodes*(nodes-1)/2;					//maximum number of links between the nodes
		
		//initialize network
		net=new Network(type, k, servperrack, switches, racks, links); 
		
		//define the Top of the Rack (ToR) switches
		//this is one edge switch in every rack
		for(int r=0;r<racks;r++){
			net.rack2switch(r, r);
		}
		
		
		//define pod ID for each ToR switch
		int rc=0;
		for(int po=0;po<k;po++) {
			for(int ra=0;ra<(k/2);ra++) {
				net.rack2pod(rc,po);
				rc++;
			}
		}
		
		//IDs of links between servers and  ToR switches

		int le=0;		
		
		for(int l1=0;l1<racks;l1++) {
			for(int l2=0;l2<servperrack;l2++) {
					net.addserverintlink(le, le, (l1+servers), 0);
					le++;
			}
		}

		System.out.println("-------------------");
		//define aggregation switches
		for(int p=0;p<k;p++) {
			for(int a=(racks+p*(k/2))-(k*k/2);a<(racks+(p+1)*(k/2))-(k*k/2);a++) {
					for(int r1=(racks+p*(k/2));r1<(racks+(p+1)*(k/2));r1++) {
					net.addswitchlink(0,(r1+servers),(a+servers), 0);
					net.switches.get(r1).settype("aggregation");
				}
			}
		}	
		
		//define core switches
		for(int p=0;p<(k/2)*(k/2);p++) {
			net.switches.get(k*k+p).settype("core");
		}
		
		int ad=0,cnt=0;
		for(int i1=0;i1<((k/2)*(k/2));i1++){
			for(int i2=0;i2<k*(k/2);i2+=((k/2))){
				net.addswitchlink(0,(i1+servers+(k*k)),(ad+i2+servers+k*(k/2)), 0);
			}
			cnt++;
			if(cnt>=(k/2)) {
				ad++;
				cnt=0;
			}
		}
	}
}

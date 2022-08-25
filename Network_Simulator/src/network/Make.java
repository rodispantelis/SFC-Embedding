package network;

/** generates network */

public class Make {
	Network net;
	String type;									//type of network
	int racks=0, switches=0, links=0, servers=0;
	int servperrack=0;								//servers per rack
	Codec cod=new Codec();							//Edge Vector coder-decoder
	String path="C:\\Files\\src\\results\\";		//path to store log files
	int k;											//k parameter different usage in different topologies
	
	public Make() {

	}
	
	/** return generated network */
	public Network getnet() {
		return net;
	}
	
	/** make 3-tier fat-tree network given k parameter and servers per rack*/
	public void makefattree(int tk, int tservperrack) {
		//parameters
		k=tk;
		servperrack=tservperrack;
		type="fat-tree";								
		racks=(int)(Math.pow(k,3.0)/4.0);		//number of racks
		servers=racks*servperrack;				//number of servers
		switches=(k/2)*(k/2)+(k*k)+racks;		//number of switches=core + aggregation & edge + ToR
		int nodes=(racks*servperrack)+switches;	//network nodes
		links=nodes*(nodes-1)/2;				//maximum number of links between the nodes
		
		//initialize network
		net=new Network(type, k, servperrack, switches, racks, links); 
		
		//define the Top of the Rack (ToR) switches
		//there is one ToR switch in every rack that connects it to the rest of the network
		for(int r=0;r<racks;r++){
			net.rack2switch(r, r);
		}
		
		//define pod ID for each ToR switch
		int rc=0;
		for(int po=0;po<k;po++) {
			for(int ra=0;ra<((k/2)*(k/2));ra++) {
				net.rack2pod(rc,po);
				rc++;
			}
		}

		//IDs of links between servers and  ToR switches
		net.links.get(0).settype(0);
		int le=0;		
		
		for(int l1=0;l1<(k*(k/2)*(k/2));l1++) {
			for(int l2=0;l2<servperrack;l2++) {
					net.addserverlink(le, (l1+servers), le, 0);
					le++;
			}
		}

		//define intra-rack links
		for(int i1=0;i1<racks;i1++) {
			for(int i2=0;i2<servperrack;i2++) {
				for(int i3=0;i3<servperrack;i3++) {
					if(i2!=i3) {
						int ii2=(i1*servperrack)+i2;
						int ii3=(i1*servperrack)+i3;
						net.links.get(cod.coder(ii2, ii3)).setintrarack();
						net.links.get(cod.coder(ii2, ii3)).settype(0);
						net.links.get(cod.coder(ii2, ii3)).setcapacity(1.0);
					}
				}
			}
		}
		
		
		//IDs of links between switches
		int ls=0;		
		
		//define edge switches
		int t=0;
		for(int p=0;p<k;p++) {		
			for(int s=racks+p*(k/2);s<(racks+(p+1)*(k/2));s++) {
				for(int r1=0;r1<(k/2);r1++) {		
					//net.switch2switch(s,r1+t, "ToR");
					net.switch2switchedge(r1+t,s, "edge");
					net.addlink(ls, r1+t, s, 0);
					ls++;
				}
				t+=k/2;
			}
		}

		//define aggregation switches
		for(int p=0;p<k;p++) {
			for(int a=(racks+(p+k)*(k/2));a<(racks+(p+k+1)*(k/2));a++) {
				for(int r1=(racks+p*(k/2));r1<(racks+(p+1)*(k/2));r1++) {
					net.switch2switch(r1,a, "aggregation");
					net.switch2switch(a,r1, "edge");
					net.addlink(ls, r1, a, 0);
					ls++;
				}
			}
		}
		
		
		//define core switches
		for(int p=0;p<(k/2);p++) {
			for(int c=(p*(k/2))+racks+k*k;c<((p+1)*(k/2)+racks+k*k);c++) {
				for(int a=(racks+k*(k/2)+p);a<(racks+k*(k/2)+p+(k/2)*k);a+=k/2) {
					net.switch2switch(a,c, "core");
					net.switch2switch(c,a, "aggregation");
					net.addlink(ls, a, c, 0);
					ls++;
				}
			}
		}
	}
}

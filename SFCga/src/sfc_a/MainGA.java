package sfc_a;
import java.io.File;

public class MainGA extends Thread{
	
	//GA parameters
	int pnodes=0;
	int genes=0;
	int generations=0;
	int supergen=0;
	int bestfit=0;
	int mupr=1;
	int crpr=1;
	int dd2=0;
	int netstat=0;
	int iteration=0;
	GA ga;
	Kdb kdb= new Kdb();										//database

	//Setup defsetup= new Setup(0, 0, 322, 50, 4, 60, 30);	experimental setup
	
	Setup defsetup= new Setup(0, 0, 322, 50, 2, 60, 30);	//default setup

	sfc_b.Tuning tuning = new sfc_b.Tuning();				//parameter tuning mechanism
	String path="C:\\Files\\src\\EVgraphs\\";				//path to stored SFCs

	int iterations=6000;
	int iter=0;
	int duration=0;
	
	//virtual topology
	int cnodes=0;
	int cedges;
	int celnet[];
	network.VNFgraph cnet;
	
	//substrate network
	int pedges;
	int phynet[];
	network.Network pnet;
	
	boolean rejection=false;
	
	int netclasses=4;		//number of classes of the network based on traffic load
	
	int mapping[]= {1};		//node mapping
	boolean deletedb=true;	//set to true to delete previous knowledge and start from scratch
	boolean paga=true;

	String path2="C:\\Files\\src\\";						//path to log files
	
	File kdbf = new File(path2+"results\\knowledgeDB.csv");
	File adl = new File(path2+"results\\adaptationlog.csv");
	File fitim = new File(path2+"results\\fitnessimprovement.csv");
	
	public MainGA(network.Network net) {
		//initialize network object, default setup, delete old log files
		this.pnet=net;

		if(deletedb) {
			if(kdbf.exists()) {
				kdbf.delete();
			}
		}else {
			if(kdbf.exists()) {
				kdb.loaddb(path2+"results\\knowledgeDB.csv");
			}
		}
		
		if(fitim.exists()) {
			fitim.delete();
		}
		
		if(adl.exists()) {
			adl.delete();
		}
		kdb.addsetup(defsetup);
	}	

	public void setduration(int d) {
		duration=d;
	}
	
	
	public void init() {
		//initialize simulation
		System.out.println("Genetic Algorithm for SFC Embedding");
		pnet.shownetstats();
		for(int d=0;d<iterations;d++) {
			ga=new GA(pnet);
			ga.setduration(duration);
			iter=d;
			int r=(int) (Math.random()*5);
			r+=5;
			String VNFfile=path+"chain"+r+"EV";
			network.VNFgraph vnfgraph=new network.VNFgraph(VNFfile);
			vnfgraph.demands();
			
			System.out.print((d+1)+":");

			initsim1(vnfgraph, r, d);
		
			if(isrejected()) {
				pnet.storerejectstats();
			}
			
			for(int v=0;v<pnet.getnumofembedded();v++) {
				pnet.getembeddedSFCs().get(v).reduceduration();
				if(pnet.getembeddedSFCs().get(v).getduration()<=0) {
					pnet.delembedded(v);
					System.out.println("- removed SFC #"+v);
				}
			}
		}
		
		pnet.shownetstats();
	}
	
	public void initsim1(network.VNFgraph vnfgraph, int r, int d) {
		
		iteration=d;
		dd2=r;
		cnet=vnfgraph;
		initsim2(defsetup);
	}

	public void initsim2(Setup su){//For GA simulation
		
		//start simulation and serve first request on default setup
		
		ga.loadVNFgraph(cnet);
			
			double netcost=pnet.getusedcpu()/pnet.gettotalcpu();
			
			netstat=(int)(netcost*netclasses);

			Setup setup=kdb.find(dd2, netstat);
			
			if(paga) {
			if(setup.vnf>0.001) {
				//if no exact setup for request
				//load closest and initialize parameter tuning
				ga.loadSetup(setup,Double.toString(setup.vnf));

				if(!tuning.shouldwait()) {
					
					//if tuning is available run the procedure in a thread
					System.out.print("\n@ "+(iteration+1)+" ");
					
					(new tuningthread()).start();
				}
			}else {
				//if setup is found load it
				ga.loadSetup(setup,Double.toString(setup.vnf));
			}
			}else {
				ga.loadSetup(su, "-1");
			}
			ga.init();

		rejection=ga.isrejected();
	}
	
	
	public boolean isrejected() {
		return rejection;
	}
	
	class tuningthread implements Runnable {
		   private Thread t;
		   private String threadName="parameter adjustment";
		   network.Network pnet2;
		   network.VNFgraph cnet2;
		   
		   public void run() {
			   int tempdd2=dd2;
			   int tempnetstat=netstat;
				System.out.print("> "+tempdd2+"-"+tempnetstat+"\n");
				
				pnet2=pnet;
				cnet2=cnet;

				tuning.loadgraphs(cnet2, pnet2);
				tuning.initTuning();
				kdb.addsetup(new Setup(tempdd2, tempnetstat, 
					tuning.ga.best.getbestphenotype()[0],
					tuning.ga.best.getbestphenotype()[1],
					tuning.ga.best.getbestphenotype()[2],
					tuning.ga.best.getbestphenotype()[3],
					tuning.ga.best.getbestphenotype()[4]));  
		   }
		   
		   public void start () {
		         t = new Thread (this, threadName);
		         t.start ();
		   }
	}
}

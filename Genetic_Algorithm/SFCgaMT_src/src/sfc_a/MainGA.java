package sfc_a;
import java.io.File;

	/** determine GA parameters and initialize simulation */
public class MainGA extends Thread{
	
	/** Genetic Algorithm parameters */
	int pnodes=0, genes=0, generations=0, supergen=0, mupr=1, crpr=1;
	/** specify the VNFgraph file names */
	int r1,r2;
	/** VNFgraph length */
	int dd2=0;
	int netstat=0;
	int iteration=0;
	/** Genetic Algorithm */
	GA ga;
	/** PAGA setup database */
	Kdb kdb= new Kdb();
	/** set to true to delete previous knowledge of PAGA */
	boolean deletedb=true;						
	/** set true to use PAGA */
	boolean paga=true;							
	/** use population generation heuristic */
	boolean popgenheuristic=true;				
	/** default setup */
	Setup defsetup;								
	/** GA tuning mechanism (PAGA) */
	sfc_b.Tuning tuning = new sfc_b.Tuning();	
	/** number of classes of the network based on traffic load */
	int netclasses=4;							

	/** VNFgraph - cnet */
	network.VNFgraph cnet;
	
	/** substrate network - pnet */
	network.Network pnet;

	/** number of iterations, in each iteration one request is computed */
	int iterations=6000;
	/** embedded VNF maximum life cycle duration */
	int duration=0;
	/** is last request rejected? */
	boolean rejection=false;
	/** path to stored SFCs */
	String EVpath="";							
	
	/** path to log files */
	String path="";
	/** kdb storage file */
	File kdbf = new File(path+"knowledgeDB.csv");
	/** file to store the adaptation of the optimal setup found by PAGA in the functionality of the GA */
	File adl = new File(path+"adaptationlog.csv");

	
	/** initialize network object, setup database, delete old log files */
	public MainGA(network.Network net, Setup setup) {
		
		this.pnet=net;
		this.defsetup=setup;
		
		if(adl.exists()) {
			adl.delete();
		}
		kdb.addsetup(defsetup);
	}	

	// setters
	/** set duration */
	public void setduration(int d) {
		duration=d;
	}
	
	/** set simulation iterations */
	public void setiterations(int i) {
		iterations=i;
	}
	
	/** set path to the stored VNFgraph files */
	public void setEVpath(String s){
		EVpath=s;
	}
	
	/** set number of netclasses */
	public void setnetclasses(int cl){
		netclasses=cl;
	}
	
	/** set parameters that determine VNFgraph filenames */
	public void setr1r2(int ra1, int ra2) {
		r1=ra1;
		r2=ra2;
	}
	
	/** set boolean parameters for using PAGA and population generation heuristic
	 * and for deleting previous stored setups in kdb
	 */
	public void setboolparams(boolean[] boolparams) {
		if(boolparams[0]) {
			paga=true;
		}else {
			paga=false;
		}
		
		if(boolparams[1]) {
			deletedb=true;
		}else {
			deletedb=false;
		}
		
		if(boolparams[2]) {
			popgenheuristic=true;
		}else {
			popgenheuristic=false;
		}
		
		if(deletedb) {
			if(kdbf.exists()) {
				kdbf.delete();
			}
		}else {
			if(kdbf.exists()) {
				kdb.loaddb(path+"knowledgeDB.csv");
			}
		}
	}
	
	/** initialize simulation */
	public void init() {
		
		System.out.println("Genetic Algorithm for SFC Embedding");
		pnet.shownetstats();
		for(int d=0;d<iterations;d++) {
			ga=new GA(pnet);
			ga.setpopgenheuristic(popgenheuristic);
			ga.setduration(duration);
			int r=(int) (Math.random()*r2);
			r+=r1;
			String VNFfile=EVpath+"chain"+r+"EV";
			network.VNFgraph vnfgraph=new network.VNFgraph(VNFfile);
			vnfgraph.demands();
			
			System.out.print((d+1)+":");

			initsim1(vnfgraph, r, d);
		
			//store statistics on rejection
			if(isrejected()) {
				pnet.storerejectstats();
			}
			
			//remove embedded SFC if lifetime expired
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
	
	/** simulation stage 1 */
	public void initsim1(network.VNFgraph vnfgraph, int r, int d) {
		
		iteration=d;
		dd2=r;
		cnet=vnfgraph;
		initsim2(defsetup);
	}

	/** simulation stage 2 */
	public void initsim2(Setup su){

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
	
	/** is request rejected? */
	public boolean isrejected() {
		return rejection;
	}
	
	/** thread that runs tuning procedure (PAGA) */
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

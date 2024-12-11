package nnetwork;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/** Operates the neural network. */
public class ANNMain {
									//neural network elements
	NNetwork net;
	Topology topo;
	String type, function, ofunction;
	double bias;
	int inputs, outputs, hidden, nodes, hnodes, upper, lower;
	
	double[] mod={0.0}; 			//edge weights of current model in Edge Vector indexing format
									
									//filenames and stored parameters
	String trainingset="dataFL";
	String delimiter=",";
	String dataset="sampledata";
	String storedmodel="model.csv";
	File pr=new File("nnprofile");
	
	public ANNMain() {
		readprofile();
	}
	
	/** Initialize neural network type, parameters and procedures. **/
	public static void main(String[] args) {
		ANNMain m=new ANNMain();
		m.readprofile();				//read profile
		m.maketopology();				//generate network topology
		m.makenetwork();				//initialize network
		m.train();						//start training procedure
										//read training dataset
		datasets.TrainingSet trset=new datasets.TrainingSet(m.dataset, m.delimiter);
		trset.readtraining();
		m.runANN(trset.getset(0));		//run network
	}

	/** Train network using Genetic Algorithm. **/
	public Double[][] ga_train(Double[] theset, int vnfsize) {
										//initialize network
		readprofile();
		maketopology();
		makenetwork();

		trainsingle(theset, vnfsize);	//train on a single record

		Double[][] res=runANN(theset);	//run trained network on input record

		return res;
	}

	/** Run models from database file. */
	public Double[][] runmodel(int vnfsize, Double[] theset) {

		Double[][] res= {{-1.0},{-1.0}};
		readprofile();
		maketopology();
		makenetwork();

		ArrayList<double[]> readm=readmodel(vnfsize);
		int mm=-1;
		for(int m=0;m<readm.size();m++) {
			if(readm.get(m).length>2) {
				net.setew(readm.get(m));
				res=runANN(theset);
				if(res[1][0]>(-1) && res[1][0]<1000) {
					mm=m;
					break;
				}
			}
		}

		if(mm>(-1)) {
			mod=readm.get(mm);
			return res;
		}else {
			Double[][] fres= {{-1.0},{-1.0}};
			return fres;
		}
	}

	/** Read models from database file. */
	public ArrayList<double[]> readmodel(int vnfsize) {
		File md=new File(storedmodel);
		String[] params= {};
		ArrayList<double[]> modls=new ArrayList<double[]>();
	
		if(md.exists()) {
			try{
		    	Scanner scanner = new Scanner(md);
   
		    	while(scanner.hasNext()){
		    		String s=scanner.nextLine();
		    		String[] params0=s.split("|");
		    		if(params0[0].equals(Integer.toString(vnfsize))) {
		    			String s2=s.subSequence(2, (s.length())).toString();
		    			params= s2.split(",");
		    			
		    			double[] tmod=new double[params.length];
		    			for(int p=0;p<params.length;p++) {
		    				tmod[p]=Double.parseDouble(params[p]);
		    			}
		    			modls.add(tmod);
		    		}
		    	}
		    	scanner.close();
			}
			catch (IOException e) {
			       e.printStackTrace();
			   }
		}
		return modls;
	}

	/** Run single model from database file. */
	public Double[][] runmodel_single(int vnfsize, Double[] theset) {

		Double[][] res= {{-1.0},{-1.0}};
		readprofile();
		maketopology();
		makenetwork();

		double[] readm=readmodel_single(vnfsize);
		if(readm.length>2) {
			net.setew(readm);
			res=runANN(theset);
		}

		return res;
	}
	
	/** Read single model from database file. */
	public double[] readmodel_single(int vnfsize) {
		File md=new File(storedmodel);
		String[] params= {};
		
		if(md.exists()) {
			try{
		    	Scanner scanner = new Scanner(md);
   
		    	while(scanner.hasNext()){
		    		String s=scanner.nextLine();
		    		String[] params0=s.split("|");
		    		if(params0[0].equals(Integer.toString(vnfsize))) {
		    			String s2=s.subSequence(2, (s.length())).toString();
		    			params= s2.split(",");
		    		}
		    	}
		    	scanner.close();
			}
			catch (IOException e) {
			       e.printStackTrace();
			   }
		}
		
		if(params.length>0) {
			mod=new double[params.length];
			for(int p=0;p<params.length;p++) {
				mod[p]=Double.parseDouble(params[p]);
			}
		}
		
		return mod;
	}
	
	/** Print network description, structure and elements. */
	public void printnet(NNetwork net) {
		System.out.println(net.net[4].id);
		System.out.println("\nNeural Network description");
		Codec c=new Codec();
		int[] t=net.topo.ev;

		for(int n=0;n<t.length;n++) {
			if(t[n]>0) {
				System.out.println(n+": "+Arrays.toString(c.decoder(n))+" "+net.ew[n]);
			}
		}
		
		for(int d=0;d<net.net.length;d++) {
			System.out.println("node: "+net.net[d].id+" bias: "+net.net[d].getbias()
					+" type: "+net.net[d].getnodetype()
						+" Output to: "+Arrays.toString(net.net[d].getoutto())
							+" Input from: "+Arrays.toString(net.net[d].getinputfrom()));
		}
		
		System.out.println("Inputs: "+Arrays.toString(net.topo.inodes));
		System.out.println("Outputs: "+Arrays.toString(net.topo.onodes));
	}
	
	/** Print statistics. */
	public void printnetstat() {
		for(int d=0;d<net.net.length;d++) {
			System.out.println("node: "+net.net[d].id
					+" cum_out: "+net.net[d].getcum());
		}
	}
	
	/** Read profile from file. */
	public void readprofile() {
		
		if(!pr.exists()) {
			System.out.println("Profile is missing. Specify a valid profile.");
		}else {
			try{
		    	Scanner scanner = new Scanner(pr);
   
		    	while(scanner.hasNext()){
		    		String[] params= scanner.nextLine().split(" ");
		    		
		    		switch(params[0]) {
					case "type": type=params[1];
					break;
					case "function": function=params[1];
					break;
					case "ofunction": ofunction=params[1];
					break;
					case "bias": bias=Double.parseDouble(params[1]);
					break;
					case "inputs": inputs=Integer.parseInt(params[1]);
					break;
					case "outputs": outputs=Integer.parseInt(params[1]);
					break;
					case "hidden": hidden=Integer.parseInt(params[1]);
					break;
					case "nodes": nodes=Integer.parseInt(params[1]);
					break;
					case "hnodes": hnodes=Integer.parseInt(params[1]);
					break;
					case "upper": upper=Integer.parseInt(params[1]);
					break;
					case "lower": lower=Integer.parseInt(params[1]);
					break;
		    		}
		    	}
		    	scanner.close();
			}
			catch (IOException e) {
			       e.printStackTrace();
			   }
		}
	}
	
	/** Choose topology. */
	public void maketopology() {	
		
		switch(type) {
			case "ff": makeff();
			break;
			case "vff": makevff();
			break;
			case "frn": makefrn();
			break;
			case "fb": makefb();
			break;
		}
	}
	
	/** Generate feed forward network. **/
	public void makeff() {
		int allnodes=inputs+outputs+(hidden*nodes);

		//generate topology for feed forward network
		topo=new Topology("ff", inputs, outputs, hidden, nodes, allnodes,
				function, ofunction, bias, upper, lower);
	}
	
	/** UNDER DEVELOPMENT */
	public void makevff() {
		
	}
	
	/** UNDER DEVELOPMENT */
	public void makefrn() {
		
	}
	
	/** UNDER DEVELOPMENT */
	public void makefb() {
		
	}
	
	/** Generate network structure. **/
	public void makenetwork() {
		net=new NNetwork(topo);
	}
	
	/** Train network. */
	public void train() {
		datasets.TrainingSet trset=new datasets.TrainingSet(trainingset,delimiter);
		trset.readtraining();
		ga.Init_ga g = new ga.Init_ga(net, trainingset, delimiter, outputs);
		g.init();
	}
	
	/** Train network with single input. */
	public void trainsingle(Double[] theset, int vnfsize) {
		ga.Init_ga g = new ga.Init_ga(net, theset, outputs, vnfsize);
		g.init();
	}
	
	/** Read training set. */
	public void readtset() {
		Double e=0.0;
		datasets.TrainingSet trset=new datasets.TrainingSet(trainingset,delimiter);
		trset.readtraining();
		for(int i=0;i<trset.getsize();i++) {
			net.runff(trset.getset(i));
			e+=net.fitness(trset.getset(i));
		}
		e=(1.0/trset.getsize())*e;
		System.out.println("Mean Square Error: "+e);
	}
	
	/** Run neural network. */
	public Double[][] runANN(Double[] tset) {
		
		net.runff(tset);
		Double e=net.fitness(tset);
		
		Double[] out=new Double[net.output.size()];
		for(int i=0;i<net.output.size();i++) {
			out[i]=net.output.get(i);
		}
		
		int printout[]=new int[out.length];
		for(int i=0;i<out.length;i++) {
			printout[i]=out[i].intValue();
		}
		Double[][] result=new Double[2][net.output.size()];
		
		result[0]=out;
		result[1][0]=e;
		
		return result;
	}
	
	/** get number of output nodes*/
	public int getoutputs() {
		return outputs;
	}
	
	/** get number of intput nodes*/
	public int getintputs() {
		return inputs;
	}
	
	/** get network. */
	public NNetwork getnet() {
		return net;
	}

	/** get current model */
	public double[] getcurrentmodel() {
		return mod;
	}
	
	/** UNDER DEVELOPMENT. Run neural network on command line mode. */
	public void init() {
		System.out.println("Artificial Neural Network");
		
		System.out.print(">");
		Scanner input1 = new Scanner(System.in);
		while(input1.hasNext()) {
			String s=input1.next();
			if(s.equals("exit")) {
				System.exit(0);
				input1.close();
			}else {
				System.out.print(">");
			}
		}
		
	}
}

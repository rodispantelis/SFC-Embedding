package ga;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/** Set parameters for Genetic Algorithm **/
public class Init_ga {
    								//GA parameters
	int population=-1, generations=-1, supergenerations=-1;
	double crossprob=-1.0, mutprob=-1.0;
	boolean checked=false;			//validity of parameters file
									//optimization parameters
	String command[]= {"0"}, tokens[]= {"0"}, target="min", variables[][]= {{"0"},{"0"}};
	int parameter_dom[][];
	int edges,outputs;				//number of edges and output nodes	
	nnetwork.NNetwork net;			//network object
	String filename, del;			//filename and delimiter
	Double[] input;					//network input
	int vnfsize;					//SFC size
	int lower=-10, upper=10;		//upper and lower bounds of link weights
	String settingsfile="gasettings";
	
	public Init_ga(nnetwork.NNetwork net, String filename, String del, int outputs) {
		this.net=net;
		this.upper=net.gettopo().getupper();
		this.lower=net.gettopo().getlower();
		this.filename=filename;
		this.del=del;
		this.outputs=outputs;
	}
	
	public Init_ga(nnetwork.NNetwork net, Double[] input, int outputs, int vnfsize) {
		this.net=net;
		this.upper=net.gettopo().getupper();
		this.lower=net.gettopo().getlower();
		this.input=input;
		this.outputs=outputs;
		this.vnfsize=vnfsize;
	}
	
	/** Initialize procedure. **/
	public void init() {
		
	    checkfiles(settingsfile);
	    
	    if(checked) {//if file is valid continue
	    	setparams();
	    	GA ga=new GA(population, generations, supergenerations, tokens, command,
	    			crossprob, mutprob, parameter_dom, target, net, input, edges, outputs, vnfsize);
	    	ga.init();
	    }
	}   

	/** Set optimization parameters. **/
	public void setparams() {
		int[] ev=net.gettopo().getev();
		edges=ev.length;
		
		parameter_dom=new int[1][2];
		parameter_dom[0][0]=lower;
		parameter_dom[0][1]=upper;
	}
	
	/** Checks if files exit and if they are valid. **/
	public void checkfiles(String s) {
		File st=new File(s);

		if(!st.exists()) {
			System.out.println("Settings file is missing. Specify a valid file.");
		}
		if(st.exists()) {
			setsettings(st);
			checked=true;
		}
	}
 	
	/** Set GA settings file */
	public void setsettingsfile(String a) {
		settingsfile=a;
	}
	
	/** Loads settings from file. **/
 	public void setsettings(File st) {
 		
		try{
	    	Scanner scanner = new Scanner(st);

	    	while(scanner.hasNext()){
	    		String[] params= scanner.nextLine().split(" ");
	    		if(params[0].equals("population")) {
	    			population=Integer.parseInt(params[1]);
	    		}else if(params[0].equals("generations")) {
	    			generations=Integer.parseInt(params[1]);
	    		}else if(params[0].equals("supergenerations")) {
	    			supergenerations=Integer.parseInt(params[1]);
	    		}else if(params[0].equals("crossover_probability")) {
	    			crossprob=Double.parseDouble(params[1]);
	    		}else if(params[0].equals("mutation_probability")) {
	    			mutprob=Double.parseDouble(params[1]);
	    		}
	    	}
	    	scanner.close();
		}
		catch (IOException e) {
		       e.printStackTrace();
		   }
 	}
}

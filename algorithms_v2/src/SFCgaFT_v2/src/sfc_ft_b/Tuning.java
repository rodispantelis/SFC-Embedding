package sfc_ft_b;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.IntStream;

	/** orchestrates the tuning of the Embedding Genetic Algorithm
	 *  by running the Parameter Adjustment Genetic Algorithm */
public class Tuning {
    
	/** names of the settings and profile files */
	String p="profile", s="settings";
	/** PAGA parameter */
	int population=-1, generations=-1, supergenerations=-1;
	/** PAGA parameter */
	double crossprob=-1.0, mutprob=-1.0;
	/** are parameter and profiel files checked? */
	boolean checked=false;
	/** should wait for PAGA procedure to wait? */
	boolean wait=false;
	/** tokens of the PAGA variables */
	String command[]= {"0"}, tokens[]= {"0"}, target="", variables[][]= {{"0"},{"0"}};
	/** variable domains */
	int parameter_dom[][];
	/** object that implements the PAGA*/
	public GA_b ga;
	/** substrate network */
	network.FTnetwork pnet2;
	/** VNFgraph */
	services.VNFgraph cnet2;		
	
	/** construct Tuning and input parameters and variables from files */
	public Tuning() {
	    checkfiles(p, s);
	}   

	/** load substrate network and VNFgraph */
	public void loadgraphs(services.VNFgraph incnet, network.FTnetwork inpnet) {	
		this.pnet2=inpnet;
		this.cnet2=incnet;
		initga();
	}
	
	private void initga() {
    	ga=new GA_b(population, generations, supergenerations, tokens, command,
    			crossprob, mutprob, parameter_dom, target, cnet2, pnet2);
	}
	
	/** initialize tuning procedure */
	public void initTuning() {
		wait=true;
    	ga.init();
    	wait=false;
	}
	
	/** should wait for PAGA procedure to wait? */
	public boolean shouldwait() {
		return wait;
	}
	
	public void setwait(boolean w) {
		wait=w;
	}
	
	/** checks if files exit and if they are valid */
	public void checkfiles(String p, String s) {

		File pr=new File(p);
		File st=new File(s);
		
		if(!pr.exists()) {
			System.out.println("Profile is missing. Specify a valid profile.");
		}
		if(!st.exists()) {
			System.out.println("Settings file is missing. Specify a valid file.");
		}
		if(pr.exists() && st.exists()) {
			setsettings(st);
			setprofile(pr);
			checkvars();
		}
		
		if(checked) {
			parameter_dom=new int[tokens.length][2];

 			for(int c1=0;c1<tokens.length;c1++) {
 				for(int c2=0;c2<variables.length;c2++) {
 					if(tokens[c1].equals(variables[c2][0])) {
 						parameter_dom[c1][0]=Integer.parseInt(variables[c2][1]);
 						parameter_dom[c1][1]=Integer.parseInt(variables[c2][2]);
 					}
 				}
 			}	
		}
	}
 	
	/** loads settings from file */
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
 	
 	/** loads profile from file */
 	public void setprofile(File pr) {
 		
 		ArrayList<String[]> vars=new ArrayList<String[]>();
		try{
	    	Scanner scanner = new Scanner(pr);

	    	while(scanner.hasNext()){
	    		String[] params= scanner.nextLine().split(" ");
	    		if(!params[0].split("")[0].equals("%") && !params[0].split("")[0].equals("")) {
		    		if(params[0].equals("command")) {
		    			command=IntStream.range(1, params.length)
                                .mapToObj(i -> params[i])
                                .toArray(String[]::new);
		    		}else if(params[0].equals("tokens")) {
		    			tokens=IntStream.range(1, params.length)
                                .mapToObj(i -> params[i])
                                .toArray(String[]::new);
		    		}else if(params[0].equals("target")) {
		    			target=params[1];
		    		}else{
		    			String temp[]=new String[3];
		    			String temp2="";
		    			temp[0]=params[0];
		    			for(int i=1;i<params.length;i++) {
		    				temp2+=params[i];
		    			}
		    			String temp3[]=temp2.split("-");
		    			if(temp3.length==2) {
		    				temp[1]=temp3[0];
		    				temp[2]=temp3[1];
		    				vars.add(temp);
		    			}
		    		}
	    		}
	    	}
	    	scanner.close();
		}
		catch (IOException e) {
		       e.printStackTrace();
		   }
		
		variables = new String[vars.size()][3];
		
		for(int v=0;v<vars.size();v++) {
			variables[v]=vars.get(v);
		}		
 	}
  
	/** checks the validity of profile and settings files */
 	public void checkvars() {

 		boolean checkprofile=true;
 		boolean checksettings=true;
 		
		if(population<2 || generations<2 || supergenerations<1 || 
				crossprob<0.0 || mutprob<0.0) {
			checkprofile=false;
		}
 		
 		if(target.equals("")) {
 			checkprofile=false;
 		}
 		
 		for(int t1=0;t1<tokens.length;t1++) {
 			for(int t2=(t1+1);t2<tokens.length;t2++) {
 				if(tokens[t1].equals(tokens[t2])) {
 					System.out.println("There are duplicate values in the tokens section.");
 					checkprofile=false;
 					break;
 				}
 			}
 		}
 		
 		for(int t3=0;t3<variables.length;t3++) {
 			for(int t4=(t3+1);t4<variables.length;t4++) {
 				if(variables[t3][0].equals(variables[t4][0])) {
 					System.out.println("There are duplicate parameters.");
 					checkprofile=false;
 					break;
 				}
 			}
 		}
 		
 		if(checkprofile) {
 			for(int p1=0;p1<tokens.length;p1++) {
 				boolean temp=false;
 				for(int p2=0;p2<variables.length;p2++) {
 					if(tokens[p1].equals(variables[p2][0])) {
 						temp=true;
 					}
 				}
 				if(!temp) {
 					checksettings=false;
 					System.out.println("There is not 1-1 correspondence between the tokens and the parameters.");
 					break;
 				}
 			}
 		}
 		
 		if(!checkprofile) {
 			System.out.println("The profile file is invalid");
 		}
 		if(!checksettings) {
 			System.out.println("The settings file is invalid");
 		}
 		checked=checkprofile && checksettings;
 	}

}

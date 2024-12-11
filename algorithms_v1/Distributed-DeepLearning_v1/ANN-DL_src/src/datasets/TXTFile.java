package datasets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**  reads datasets stored in text files */
public class TXTFile {
	ArrayList<Double[]> tset=new ArrayList<Double[]>();

	/** Read file. **/
	public void readfile(String filename, String del) {
	File file=new File(filename);
	
	if(!file.exists()) {
		System.out.println("File "+filename+" is missing. Specify a valid file.");
	}else {
		try{
	    	Scanner scanner = new Scanner(file);

	    	while(scanner.hasNext()){
	    		String[] params= scanner.nextLine().split(del);
	    		Double[] temp=new Double[params.length];
	    		for(int i=0;i<params.length;i++){
	    			temp[i]=Double.parseDouble(params[i]);
	    		}
	    		tset.add(temp);
	    	}
	    	scanner.close();
		}
		catch (IOException e) {
		       e.printStackTrace();
		   }
	}
	}
	
	public int getsize() {
		return tset.size();
	}
}

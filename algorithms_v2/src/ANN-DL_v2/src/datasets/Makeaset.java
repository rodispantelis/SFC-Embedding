package datasets;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

/*   
Copyright 2024 Panteleimon Rodis

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this software except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

/** generate a dataset of random data */
public class Makeaset {

	int reclength=11, records=500;
	double lower=0.0, upper=20.0;
	
	/** generate random dataset given the number of record */
	public void generate() {
		for(int rec=0;rec<records;rec++) {
			generate2();
		}
	}
	
	/** generate random dataset; define parameters */
	public void generate(int reclength, int records, double lower, double upper) {
		this.reclength=reclength;
		this.records=records;
		this.lower=lower;
		this.upper=upper;
		
		for(int rec=0;rec<records;rec++) {
			generate2();
		}
	}
	
	/** generate random dataset */
	public void generate2() {
		double[] record=new double[reclength];
		double dif=upper-lower;
		int r=(int) (Math.random()*(reclength-6));
		
		for(int i=0;i<r;i++) {
			record[i]=-1;
		}
		record[reclength-1]=-1;
		
		for(int i=r;i<(reclength-1);i++) {
			double rr=Math.random()*dif;
			record[i]=(double)Math.round(rr*1000d) / 1000d;
		}

		double ind=10000000;
		
		for(int ii=r;ii<(reclength-2);ii++) {
			double t=record[ii]-record[reclength-2];
			if(t>=0 && t<ind) {
				record[reclength-1]=ii;
				ind=t;
			}
		}

		storedata(Arrays.toString(record));
	}
	
	/** store generated dataset in a file */
	public void storedata(String b) {

        FileWriter fw = null;
        BufferedWriter bw = null;
        PrintWriter pw = null;

		try {
			try {
				fw = new FileWriter("data.csv",true);
			} catch (IOException e) {
				e.printStackTrace();
			}
			bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			pw.println(b);//Adds an end to the line
			pw.flush();
		}finally {
	        try {
	             pw.close();
	             bw.close();
	             fw.close();
	        } catch (IOException io) { 
	        	}
		}
	}
}

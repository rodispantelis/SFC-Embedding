package datasets;

/** generates objects of training datasets */
public class TrainingSet extends TXTFile{
	String filename, del;
	
	public TrainingSet(String filename, String del) {
		this.filename=filename;
		this.del=del;
	}
	
	/** read training set from file **/
	public void readtraining() {
		readfile(filename, del);
	}
	
	/** get dataset **/
	public Double[] getset(int index) {
		return tset.get(index);
	}
}

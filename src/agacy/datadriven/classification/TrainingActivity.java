package agacy.datadriven.classification;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This classes carries the information extracted from the training data about one activity.
 * @author ramol_na
 *
 */
public class TrainingActivity {

	/**
	 * Name (id) of this activity
	 */
	private String name;

	/**
	 * Mapping between features (id) and their number of occurrence for this activity in the training data.
	 */
	private HashMap<String, Integer> features;
	
	/**
	 * Number of occurence of the activity in the training data
	 */
	private int nbrOcc;
	
	
	//** Method **//

	public TrainingActivity(String name)
	{
		this.name = name;
		this.nbrOcc = 1; //At least one if it exists
		features = new HashMap<String, Integer>();
	}
	
	public String getName() 
	{
		return name;
	}	
	
	public int getNbrOcc() {
		return nbrOcc;
	}
	
	public ArrayList<String> getFeatureList()
	{
		return new ArrayList<String>(features.keySet());
	}
	
	public Integer getFeatureCount(String feat)
	{
		return features.get(feat);
	}
	
	/**
	 * Increase the occurrence counter
	 */
	public void increaseOcc()
	{
		nbrOcc++;
		//System.out.println("Increasing occurence... new val: "+nbrOcc);		
	}
	
	/**
	 * Add a feature.
	 * If it wasn't met yet, creates it.
	 * Otherwise, increase its counter.
	 */
	public void addFeature(String featId)
	{
		features.put(featId, features.containsKey(featId)? features.get(featId)+1 : 1 );		
	}
	
	@Override
	public boolean equals(Object  A) {
	    return this.name.equals(((TrainingActivity)A).getName());
	}
}

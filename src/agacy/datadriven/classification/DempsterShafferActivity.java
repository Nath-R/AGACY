package agacy.datadriven.classification;

import java.util.ArrayList;
import java.util.HashMap;

import agacy.datadriven.feature.Feature;

/**
 * Activity representation in the Dempster Shaffer model.
 * It carries information including the name and the mass of the linked features
 * @author ramol_na
 *
 */
public class DempsterShafferActivity {

	//** Methods **//
	
	String name;


	/**
	 * Pair of feature (id) and their weight for this activity ( M_f(Act) )	
	 * The weight is between [0,1]
	 * It reprent only M_f(Act) and not M_f(Act,!Act) which is supposed to be: M_f(Act,!Act) = 1 - M_f(Act)
	 */
	HashMap<String, Double> featMass;
	
	//** Method **//
	
	/**
	 * Creates the activity from an activity extracted from the training data.
	 * It computes the weight according to the number of occurence of each feature
	 * @param ta
	 */
	public DempsterShafferActivity(TrainingActivity ta)
	{
		name = ta.getName();
		
		ArrayList<String> featIds = ta.getFeatureList();
		featMass = new HashMap<String, Double>();
		
		System.out.println("Generating dempster activity:"+ name+" "+featIds);
		
		for(String feat: featIds)
		{
			//For each feat, divided t's occ number against the nimer of occ of the featueres
			//System.out.println("Mass: "+((double)ta.getFeatureCount(feat))+" / "+ ((double)ta.getNbrOcc()) );
			featMass.put(feat, new Double( ((double)ta.getFeatureCount(feat)) / ((double)ta.getNbrOcc()) ) ); 
		}
		
	}
	
	public double computeMass( ArrayList<Feature> featInst )
	{
		double value = 0.0;
				
		for(Feature F: featInst)
		{
			if(featMass.containsKey(F.getName()))
			{ value += F.getWeight() * featMass.get(F.getName()); }
			
		}
		
		return value;
	}
	
	
	public String getName() {
		return name;
	}
	
}

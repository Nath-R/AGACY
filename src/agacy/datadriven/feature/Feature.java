package agacy.datadriven.feature;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.TreeSet;

/**
 * An instance of a feature resulting from the analysis of the action set
 * @author ramol_na
 *
 */
public class Feature {

	/**
	 * Threshold between long and short features in seconds.
	 */
	//private static final double LONGSHORTTHRESH = 300;
	
	/**
	 * X factor for computation of the weight
	 */
	private static final double CHI = 0.5;
		
	/**
	 * Path to the feature 'database'
	 */
	private static final String FEATPATH = "res/datadriven/feature/feature.feat";
	
	/**
	 * Action the feature depends on
	 */
	ArrayList<String> childActions;
	
	/**
	 * Actions that were actually met for now
	 * Ordered by time
	 */
	TreeSet<Action> observedActions;
	
	/**
	 * Fact
	 */
	double fact;
	
	/**
	 * Weight of this feature
	 */
	double weight;
	
	/**
	 * Long feature or not.
	 * If not it supposes short.
	 */
	boolean longFeature;
	
	/**
	 * Name (id) of the feature
	 */
	String name;

	/**
	 * Duration in second
	 */
	double duration;
	
	
	
	
	/** Method **/
	
	public Feature(String name, boolean longFeature, double duration, ArrayList<String> childActions)
	{
		this.name = name;
		this.longFeature = longFeature;
		this.duration = duration;
		this.childActions = childActions;
		
		this.weight = 0.0;
		this.fact = 0.0;
		this.observedActions = new TreeSet<Action>( new ActionComparator() );
	}
	
	/**
	 * Erase instance info to only keep the structural information
	 */
	public void reset()
	{
		weight = 0.0;
		fact = 0.0;
		observedActions.clear();
	}
	
	public String toString()
	{
		String ret= "";
		
		ret += name+"(";
		if(longFeature)
		{ ret += "long"+" "; }
		else
		{ ret += "short"+" "; }	
		ret += duration+"  ";
		
		ret += "w="+weight;
		
		ret += ")";
		
//		for(String action: childActions)
//		{
//			ret += " "+action;
//		}
		
		return ret;
	}
	
	/**
	 * Check through the set of actions to compute the weight
	 * TODO redo the shitty algo
	 * @param actions
	 * @return true if the Feature was recognized
	 */
	public double detect(TreeSet<Action> actions)
	{
		observedActions.clear();
		//System.out.println(childActions);
		
		//Go through all action and add the defining ones to the observation set
		for(Action a: actions)
		{
			//Check if the action is one of the action defining the feature
			boolean actDetected = false;
			
			for(String actName: childActions)
			{
				if(actName.equals( a.getValue() ))
				{ actDetected = true; } //Can only be set to true
			}			
			
			//If yes, add it into the observed set of action (clear the set before)
			if(actDetected)
			{
				observedActions.add(a);
			}			
		}
				
		//Go thorugh observaction and compute the weigh
		for(Action obsA: observedActions)
		{
			//Get the time difference with the last
			double time = observedActions.last().getTime() - obsA.getTime();
			//System.out.println(obsA.getUncertainty() * computeFact(time));
			weight += obsA.getUncertainty() * computeFact(time);
				
		}
		
		return weight; 
	}
	
	private double computeFact(double patternDuration)
	{
		if(patternDuration == 0) 
		{
			fact = 1;
		}
		if(longFeature)
		{
			//Convert to hours
			double durationH = patternDuration / 3600.0;
			fact = Math.exp(-1*CHI*durationH);
		}
		else if( CHI * patternDuration > 1 )
		{
			fact = 1 / (CHI * patternDuration);
		}
		else
		{
			fact = 1;
		}
		
		return fact;
	}
	
	/**
	 * Tells if the given action is part of the feature
	 * @return -1 if not in this feature, the position in the arraylist otherwise
	 */
	public int containsAction( String actName )
	{
		for(int i=0; i<childActions.size(); i++)
		{		
			if(childActions.get(i).equals(actName))			
			{ return i; }
		}
		
		return -1;
	}
	
	/**
	 * Insert a new action after position at pos and push the following action
	 */
	public void inserChildAction( String actName, int pos )
	{
		childActions.add(pos, actName);
	}
	
	public String getName()
	{
		return name;
	}
	
	public double getWeight()
	{
		return weight;
	}
	
	/**
	 * Read the knowledge of features and put them in a list
	 * Example of format of the string feature:   breakfast long 1800: toasterUsed MealReady
	 * @throws IOException 
	 */
	public static ArrayList<Feature> loadFeaturesFromFile() throws IOException
	{
		InputStream fis = new FileInputStream(FEATPATH);
	    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
	    BufferedReader br = new BufferedReader(isr);
	    
	    ArrayList<Feature> loadedFeature = new ArrayList<Feature>();
	    
	    String line;
	    
	    //Going through each line
	    //A line = a feature
	    while( (line = br.readLine()) != null)
	    {
	    	//Cut the string in two part
	    	String[] splitLine = line.split(": ");
	    	
	    	//Generate the list of action
	    	ArrayList<String> actions = new ArrayList<String>();
	    	for(String action: splitLine[1].split(" ") )
	    	{
	    		actions.add(action);
	    	}
	    	
	    	//Create the new feature
	    	String name = splitLine[0].split(" ")[0];
	    	String longFeature = splitLine[0].split(" ")[1];
	    	String duration = splitLine[0].split(" ")[2];
	    	Feature newf = new Feature(name, longFeature.contains("long"), Double.parseDouble(duration), actions);
	    	
	    	//System.out.println("Generated: "+newf.toString());
	    	
	    	//Adding in the list:
	    	loadedFeature.add(newf);
	    }
	    
	    br.close();
	    
	    return loadedFeature;
	}
	
}


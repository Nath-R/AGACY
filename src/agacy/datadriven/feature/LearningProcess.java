package agacy.datadriven.feature;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Learning process
 * This process link newly added action (as rules) to features.
 * I goes through the following step:
 * 1- Go through action rules to extract events dependencies to action (action done by n and m events...)
 * 2- Extract actions dependencies to features (features are composed by action a, b, etc...)
 * 3- Classify action to features (in particular new and non classified action) by measuring distance with already classified action
 * 4- Update the feature's knowledge
 * @author ramol_na
 *
 */
public class LearningProcess {

	/**
	 * Path to the rules generating the actions
	 */
	private static final String RULEPATHACTION ="res/knowledge/reasoning/rulesActions.txt"; 
	
	/**
	 * Path to the features knowledge
	 */
	private static final String FEATPATH = "res/datadriven/feature/feature.feat";
	
	/**
	 * Similarity threshold to consider two actions similar enough to be added under the same features
	 */
	private static final double SIMTHRESH = 0.4;
	
	/**
	 * Features knowledge
	 * It uses the same feature class used for the recognition process (same class as instance)
	 * But it does not use all its functionalities.
	 */
	ArrayList<Feature> features;
	
	/**
	 * Action (structure) knowledge
	 * Extracted from the rules
	 */
	ArrayList<ActionStructure> actions;
	
	
	//** Methods **//
	
	/**
	 * Constructor
	 */
	public LearningProcess()
	{
		actions = new ArrayList<ActionStructure>();
		
		//Loading features
		try {
			features = Feature.loadFeaturesFromFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Extracts action with their event property
	 * Rule must follow the Jena format ([rule: yet some triple -> yet some other])
	 * Name of events and action are supposed to be described by the hasValue property
	 * @throws IOException 
	 */
	public void extractAction() throws IOException
	{
		byte[] encoded = Files.readAllBytes(Paths.get(RULEPATHACTION));
		String fileStr =  new String(encoded, "UTF-8");
		System.out.println(fileStr);
		
		Pattern p = Pattern.compile("\\[(.*?)\\]", Pattern.DOTALL);
		Matcher m = p.matcher(fileStr);
		while(m.find())
		{
		    System.out.println("\n\n\n RULE:\n");
		    System.out.println(m.group());
		    String rule = m.group();
		    
		    //Split rules around ->
		    //TODO check size of split
		    String ruleLeft = rule.split("->")[0];
		    String ruleRight = rule.split("->")[1];
		    
		    //Find the values (hasValue " ")
		    Pattern pVal = Pattern.compile("hasValue \\\"(.*?)\\\"", Pattern.DOTALL);
		    Matcher mValLeft = pVal.matcher(ruleLeft);
		    Matcher mValRight = pVal.matcher(ruleRight);
		    
		    
		    String action=""; //Only one action shall be present, otherwise, only use the first one
		    while(mValRight.find())
		    {	
		    	String res = mValRight.group().replace("\"", "").replace("hasValue ", ""); //Remove the extra stuff
		    	System.out.println("Action: "+res); 
		    	action = res;
		    }
		    
		    ArrayList<String> events = new ArrayList<String>();		    
		    while(mValLeft.find())
		    {
		    	String res = mValLeft.group().replace("\"", "").replace("hasValue ", "");
		    	System.out.println("Events: "+res);
		    	events.add(res);
		    }
		    
		    //Check if the action was already created
		    boolean found = false;
		    for(ActionStructure AS: actions)
		    {
		    	if(AS.getLabel().equals(action))
		    	{
		    		found = true;
		    		//Update this action with new event
		    		
		    	}
		    }
		    
		    //Otherwise Creating the new actionStructure...
		    if(!found)		   
		    { actions.add( new ActionStructure(action, events) ); }
		}	    
	}
	
	/**
	 * Analyze the actions composition of events and update the features with actions.
	 * In other words learning ! 
	 * Action are then added in the features.
	 * The addition is done by putting the action after the similar action that lead to this insertion.
	 * TODO Proper addition (update feature action dependencies sequence, parallel, end, etc...)
	 * TODO Consider generating new features instead of modifying existing one
	 * TODO Consider similarity between actions
	 */
	public void updateFeatures()
	{
		//Comparing actions between each other
		for(int i=0; i<actions.size(); i++)
		{
			for(int j=i+1; j<actions.size(); j++)
			{
				ActionStructure A = actions.get(i);
				ActionStructure B = actions.get(j);
				
				double similarity = A.compare(B);
				
				System.out.println("Comparing "+A.getLabel()+" and "+B.getLabel()+": "+similarity);
				
				//If action are different enough -> nothing to do 
				//If action are totally similar -> ignore (todo: similarity in feature)
				//If action partly similar, go through features and check if they are in
				
				if( similarity > SIMTHRESH )
				{
					//Go through features...
					for(Feature F: features)
					{
						//If features includes both: nothing to do
						if( F.containsAction(A.getLabel()) >= 0 && F.containsAction(B.getLabel()) >= 0 )
						{ 
							System.out.println("Both action in feature "+F.toString());
						}						
						//If features includes none: nothing to do
						else if( F.containsAction(A.getLabel()) < 0 && F.containsAction(B.getLabel()) < 0 )
						{
							System.out.println("No action in feature "+F.toString());
						}						
						//If features includes A but not B: include B after A
						else if( F.containsAction(A.getLabel()) >= 0 && F.containsAction(B.getLabel()) < 0 )
						{ 
							System.out.println("Inserting action "+B.getLabel()+" after "+A.getLabel()+" in feature "+F.toString() );
							F.inserChildAction(B.getLabel(), F.containsAction(A.getLabel())+1 ); 
						}	
						//If features includes B but not A: include A after B
						else if( F.containsAction(A.getLabel()) < 0 && F.containsAction(B.getLabel()) >= 0 )
						{ 
							System.out.println("Inserting action "+A.getLabel()+" after "+B.getLabel()+" in feature "+F.toString() );
							F.inserChildAction(A.getLabel(), F.containsAction(B.getLabel())+1 ); 
						}	
						//Otherwise: impossible...						
					}					
				}				
			}
		}
	}
	
	/**
	 * Save the new set of feature in the file
	 * @throws IOException 
	 */
	public void saveFeatures() throws IOException
	{
		PrintWriter writer = new PrintWriter(new File(FEATPATH));

		for(Feature F: features)
		{
			writer.write(F.toString()+"\n");
		}
		
		writer.close();
	}
	
}

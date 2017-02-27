package agacy.datadriven.feature;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.TreeSet;

import agacy.datadriven.classification.DempsterShafferClassifier;
import agacy.datadriven.classification.SVMClassifier;
import agacy.knowledge.ontology.OntologyManager;

/**
 * Feature extraction process.
 * It is a thread that periodically updates its knowledges of action and extract features from them
 * Action are stored in a list (FIFO) and only the newest one are added.
 * Feature are for now described in an expert provided file.
 * The time window duration are for now defined by the period of the thread.
 * @author ramol_na
 *
 */
public class FeatureExtraction extends Thread {

	
	/**
	 * Reference to ontology manager
	 */
	OntologyManager OM;
	
	/**
	 * Sleep duration in milliseconds
	 * It also define the duration of the time window in the model
	 */
	long SLEEPDUR = 5000;
	
	/**
	 * Time windows size in seconds
	 */
	double WINSIZE = 180;
	
	/**
	 * Actions sequence S.
	 * It carries all the observed actions (the very oldest one are removed)
	 * Only new (younger) action are added and the list is sorted by action date based on their timestamp.
	 * It's order in ascend: the oldest come first. 
	 */
	TreeSet<Action> actionSequence;
	
	/**
	 * List of all existing features.
	 * They are stored in a file and loaded when the class is constructed.
	 * This are NOT recognized features, but a representation of their structure.
	 */
	ArrayList<Feature> features;
	
	DempsterShafferClassifier dsc;
	
	SVMClassifier svm;
	
	String setName;
	
	/**
	 * Constructor
	 */
	public FeatureExtraction( String newSetName) {
		System.out.println("Preparing Feature extraction process....");
		
		OM = OntologyManager.getInstance();
		actionSequence = new TreeSet<Action>( new ActionComparator() );
		setName = newSetName;
		
		dsc = new DempsterShafferClassifier();
		dsc.generateModelFromFile();
		
		svm = new SVMClassifier();
		svm.generateModelFromFile();
		
		
		//Loading feature
		features = new ArrayList<Feature>();
		try {
			features = Feature.loadFeaturesFromFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {

		System.out.println("Starting feature extraction process...");
		
		Double lastActionTime = 0.0;
		
		while(!Thread.currentThread().isInterrupted())
		{
			//Sleeping...		
			try {
				Thread.sleep(SLEEPDUR);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
			
			System.out.println("Extracting features...");
			
			//Step 1 extract the action and update the action list
			ArrayList <String> actionsStr = OM.extractActions();
			
			updateActionsList(actionsStr, lastActionTime);
			
			ArrayList<TreeSet<Action>> subActSet= divideActionSet(actionSequence, WINSIZE);
			debugDisplaySubset(subActSet);
			
			int ite = 0; //counting how many time we went in
			
			//Printiling the log in a file
//			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
//			Date d = new Date();
			File file = new File("res/dataset/results/"+setName+".txt");
			PrintStream ps_console = new PrintStream(System.out);
	        try {
				FileOutputStream fos = new FileOutputStream(file);
				PrintStream ps = new PrintStream(fos);
				System.setOut(ps);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for(TreeSet<Action> subAct: subActSet)
			{		
				ite++;
				
				//Step 2 analyze the list...			
				//For each feature, check if it triggered or not			
				for(Feature f: features)
				{
					//Remove the previous info
					f.reset();
					//Update the weights
					f.detect(subAct);
				}
				
				//Step 3 saving/providing the features 
				System.out.println("\n\nResults for timewindow "+ite+" :");
				System.out.println(subAct);
				System.out.println(features);
				if(subAct.size()>0)
				{
					System.out.println("--- DS:");
					dsc.recognizeActivities(features);
					System.out.println("--- SVM:");
					svm.recognizeActivities(features);	
				}
				else
				{
					System.out.println("none");
				}				
				
			}
			
			//Default
						
			System.setOut(ps_console);
			
			//Getting the last time of the current list
			if(actionSequence.size() > 0)
			{ lastActionTime = actionSequence.last().getTime(); }
			
			//Clearing the list
			//(or putting it somewhere)
			actionSequence.clear();
			for(Feature f: features)
			{ f.reset(); }
		}
	}
	
	private void updateActionsList(ArrayList <String> actionsStr, Double newestTime)
	{
		//Get current max value (If list not reset)
		//Double oldestTime = 0.0;
		//if(actionSequence.size() > 0)
		//{ oldestTime = actionSequence.last().getTime(); }
		
		for(int i=0; i<actionsStr.size()-(actionsStr.size()%3); i+=3)
		{
			Action newAct = new Action(actionsStr.get(i+0), Double.parseDouble( actionsStr.get(i+1) ), Double.parseDouble( actionsStr.get(i+2) ) );
						
			//Check if action is new
			if(newAct.getTime() > newestTime) //Action came before the last one registered: already in (or too late)
			{
				actionSequence.add(newAct);
			}
			else
			{
				//System.out.println("Action not added");
			}
		}
		
		System.out.println("Actions: "+actionSequence.toString());
	}
	
	/**
	 * Divide a set of an action set into multiple subset matching the duration size
	 * @param winSize window size in seconds
	 * @return An arraylist of treeset per time window
	 */
	private ArrayList<TreeSet<Action>> divideActionSet(TreeSet<Action> actSet, Double winSize)
	{
		//Error case
		if(actSet.size() <= 0)
		{ return null; }
		
		//Return
		ArrayList<TreeSet<Action>> ret = new ArrayList<TreeSet<Action>>();
		
		//Start time
		//Double startTime = actSet.first().getTime();
		Double startTime = 0.0;
		
		//First result
		ret.add(new TreeSet<Action>(new ActionComparator()));
		
		//Time window iteration
		int winIt = 1;
		
		for(Action a: actSet)
		{
			while(a.getTime() > startTime + winIt*winSize) //While the current action is out of the time window... create a new one until it matches
			{
				winIt++;
				ret.add(new TreeSet<Action>( new ActionComparator() ));				
			}
			ret.get(winIt-1).add(a);
		}
		
		return ret;
	}
	
	/**
	 * Print all the action subset
	 */
	private void debugDisplaySubset(ArrayList<TreeSet<Action>> actSubSet)
	{
		int i=0;
		
		for(TreeSet<Action> tsa: actSubSet)
		{
			i++;
			System.out.println("Time window "+i+" :"+tsa);
		}
	}
	
}






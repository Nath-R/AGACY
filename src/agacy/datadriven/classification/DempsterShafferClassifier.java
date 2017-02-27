package agacy.datadriven.classification;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

import agacy.datadriven.feature.Feature;

public class DempsterShafferClassifier extends ActivityClassifier {

	//** Attributes **//
	
	private static final String EXPERTFILE = "res/datadriven/classification/expertKnoweldge.act";
	
	/**
	 * List of activity extracted from training data	
	 */
	private ArrayList<TrainingActivity> learnedAct;
	
	/**
	 * List of dempster shaffer activity
	 */
	private ArrayList<DempsterShafferActivity> dempAct;
	
	//** Methods **//
	
	/**
	 * Constructor
	 */
	public DempsterShafferClassifier() {
		learnedAct = new ArrayList<TrainingActivity>();
		dempAct = new ArrayList<DempsterShafferActivity>();
	}
	
	@Override
	public void generateModelFromFile() {

		//Loading content from file
		try {
			
		InputStream fis;	
		fis = new FileInputStream(EXPERTFILE);
		
	    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
	    BufferedReader br = new BufferedReader(isr);
	    
	    
	    String line;
	    
	    //Going through each line
	    //A line = a feature
	    while( (line = br.readLine()) != null)
	    {
	    	//Cut the string in two part
	    	String[] splitLine = line.split(": ");
	    	
	    	//Check if the activty existi and create if not create the activity
	    	
	    	TrainingActivity newA = new TrainingActivity(splitLine[0]);
	    	
	    	if(learnedAct.contains(newA))
	    	{ learnedAct.get( learnedAct.indexOf(newA) ).increaseOcc(); }
	    	else
	    	{ learnedAct.add(newA);	}

	    	for(String feat: splitLine[1].split(" ") )
	    	{
	    		learnedAct.get( learnedAct.indexOf(newA) ).addFeature(feat);
	    	}

	    	
	    	//System.out.println("Generated: "+newA.getName());
	    	
	    	
	    }
	    
	    br.close();
	    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//Generating the model
		
		for(TrainingActivity ta: learnedAct)
		{
			dempAct.add(new DempsterShafferActivity(ta));
		}
		

	}

	@Override
	public void recognizeActivities(ArrayList<Feature> feats) {
		
		DempsterShafferActivity recoAct = null;
		Double prevMass = 0.0;
		Double totalMass = 0.0;
		
		//Detect the act with the highest  mass and sum the total for upcoming normalization
		for(DempsterShafferActivity dsa: dempAct)
		{
			Double mass = dsa.computeMass(feats);
			totalMass += mass;
			//System.out.println("Activity (non normalized): "+ dsa.getName()+" "+mass.toString() );
			if(mass > prevMass)
			{
				prevMass = mass;
				recoAct = dsa;
			}
		}
		
		//Going through activities again and normalize
		for(DempsterShafferActivity dsa: dempAct)
		{
			Double mass = dsa.computeMass(feats);
			System.out.println("Activity: "+ dsa.getName()+" "+(mass/totalMass) );
		}
		
		if(recoAct!=null)
		{ System.out.println("Recognized activity with DS: "+ recoAct.getName()  ); }
		else
		{ System.out.println("Recognized activity with DS: none"); }
	}

}

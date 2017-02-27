package agacy.datadriven.classification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import agacy.datadriven.feature.Feature;
import libsvm.svm;
import libsvm.svm_model;

/**
 * Use a SVM to classify the data.
 * It first transform the learning file into a binary numerical file.
 * The transformed training data are then send to the training algorithm of SVM that generate a model file.
 * This model file is then use for classifying.
 *
 * @author ramol_na
 *
 */
public class SVMClassifier extends ActivityClassifier {

	private static final String EXPERTFILE = "res/datadriven/classification/expertKnoweldge.act";
	
	HashMap<String, Integer> mapActId;
	
	HashMap<String, Integer> mapFeatId;
	
	public SVMClassifier() {
		mapActId = new HashMap<String, Integer>();
		mapFeatId = new HashMap<String, Integer>();
	}
	
	
	/**
	 * Transform the training data into numerical values:
	 * Example:
	 *  Breakfast: eatingtoast morning sittingkitchen  -> 2 1:1 2:1 3:0 4:1 5:0
	 *  
	 *  It transforms it into a binary set: 1 the feature is present, -1 it is not
	 *  
	 *  It also set the mapping between act(features) and id
	 * @throws IOException 
	 */
	public void transformTrainingData(File trainingFile) throws IOException
	{
		//Collection 1: Activity <> features   Each sample of the file
		ArrayList<SampleActivity> samples = new ArrayList<SampleActivity>();
		
		//Collection 2: Activity <> id (int)  List of all activities with their associated id  (update it)
		mapActId = new HashMap<String, Integer>();
		
		//Collection 3:  Feature <> id (int) List of all features with their associated id  (update it)
		mapFeatId = new HashMap<String, Integer>();
		
		//Collection 4: List of all features, extracted from  the previous collection actually
		ArrayList<String> features; //Initialize afterward directly from collection 3
		
		//Collection 5: Activity id <> Feature id   Transformation of collection 1 using collection 2 and 3
		ArrayList<SampleNumActivity>  numSamples = new ArrayList<SampleNumActivity>();
		
		InputStream fis;	
		fis = new FileInputStream(trainingFile);
		
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
	    	
	    	SampleActivity newA = new SampleActivity();
	    	newA.name = splitLine[0];
	    	newA.features = new ArrayList<String>();
	    	
	    	//If not encountered yet, add it to the map act<>id
	    	if( !mapActId.containsKey(newA.name) )
	    	{
	    		mapActId.put(newA.name, mapActId.size() + 1); //Id is the size + 1  (starts from 1 and not 0)
	    	}

	    	for(String feat: splitLine[1].split(" ") )
	    	{
	    		newA.features.add(feat);
	    		
	    		//If we meet the feature for the first time add it to the map feat<>id
	    		if(! mapFeatId.containsKey(feat) )
	    		{
	    			mapFeatId.put(feat, mapFeatId.size() + 1);
	    		} 
	    	}
	    	
	    	samples.add(newA);	    	
	    }
	    
	    br.close();
	    
	    //Extract features array from  map
	    features = new ArrayList<>( mapFeatId.keySet() );
	    
	    //Activities and features loaded, transform them into numerical
	    for(SampleActivity sa: samples)
	    {
	    	SampleNumActivity newSna = new SampleNumActivity();
	    	
	    	newSna.actId = mapActId.get(sa.name);
	    	
	    	newSna.featIds = new ArrayList<Integer>();
	    	
	    	for(String featName: sa.features)
	    	{
	    		newSna.featIds.add( mapFeatId.get(featName) );
	    	}
	    	
	    	numSamples.add(newSna);
	    }	    
	    
	    //Generate the new file    	
    	PrintWriter writer = new PrintWriter(trainingFile+"num", "UTF-8");
	    
	    for(SampleNumActivity sna: numSamples)
	    {
	    	String lineStr = sna.actId.toString();
	    	
	    	for(String featName: features)
	    	{
	    		Integer featId = mapFeatId.get(featName);
	    		lineStr += " "+featId+":"+(sna.featIds.contains(featId)? "1" : "0");
	    	}
    		
    		writer.println(lineStr);
	    }
	    
	    writer.close();
	}

	@Override
	public void generateModelFromFile() {
		SVMTrain svmt = new SVMTrain();		
		
		try {
			transformTrainingData(new File(EXPERTFILE) );
			
			svmt.run(EXPERTFILE+"num", EXPERTFILE+"model");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void recognizeActivities(ArrayList<Feature> feats) {
		
		//Transform feats into an array of value using the matchings
		ArrayList<String> allFeatures = new ArrayList<String>(mapFeatId.keySet());
		
		String line = "0"; //Default ast id
		for(String featName: allFeatures)
    	{
    		Integer featId = mapFeatId.get(featName);
    		line += " "+featId+":";
    		
    		boolean found = false;
    		
    		for(Feature f: feats) //Go through all feature...
    		{
    			if(f.getName().equals(featName))//If feature match the current id, select it's weigh
    			{
    				if(f.getWeight() > 0.0)
    				{ line+=f.getWeight(); }
    				else
    				{ line+="0"; }
    				found = true;
    			}
    		}
    		
    		if(!found) //If the current features is not in the param
    		{
    			line+="-1";
    		}
    	}
		
		//System.out.println(line);
		
		//Save it to a temp file
		try {
			PrintWriter writer = new PrintWriter(EXPERTFILE+"_instancetmp", "UTF-8");
			
			writer.write(line);
			
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Launch the SVM reco
		try {
			SVMPredict svmp = new SVMPredict();
			
			InputStream fis = new FileInputStream(new File(EXPERTFILE+"_instancetmp"));
			InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
		    BufferedReader br = new BufferedReader(isr);	
		    
		    svm_model model = svm.svm_load_model(EXPERTFILE+"model");
			
			double resArray[];
			
			resArray = svmp.predict(br, model, 1);	
			String actName = "";
			int i = 1;
			double curMax = 0.0;
			String recoAct = "";
			for(double res: resArray)
			{
		        for (Entry<String, Integer> entry : mapActId.entrySet() ) {
		            if (entry.getValue().equals( (int) i)) {
		            	actName = entry.getKey();
		            }
		        }
		        System.out.println("Activity: "+actName+" "+res);
		        
		        //Check highest prob act
		        if(res > curMax)
		        { 
		        	recoAct = actName;
		        	curMax = res;
		        }
		        
		        i++;
			}
			System.out.println("Recognized activity with SVM: "+ recoAct  );
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}


class SampleActivity
{
	public String name;
	
	public ArrayList<String> features;
}

class SampleNumActivity
{
	public Integer actId;
	
	public ArrayList<Integer> featIds;
}

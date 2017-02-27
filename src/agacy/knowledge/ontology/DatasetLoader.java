package agacy.knowledge.ontology;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Classing carrying methods to read and load data from datasets into the ontology
 * @author ramol_na
 *
 */
public class DatasetLoader {
	
	public static final double RELIABLETHRESH = 0.8;
	
	public static final double FAKEEVENTRATE = 0.0;

	/**
	 * Loads event data from files that can be found in given directory
	 * @param dirPath Path to the directory carrying the dataset
	 * @throws IOException 
	 */
	public static void loadEventDataset(String dirPath) throws IOException
	{
		//Step 1 open file and ontology
		
		OntologyManager OM = OntologyManager.getInstance();
		
		InputStream fis;	
		fis = new FileInputStream(dirPath+"/"+"level3.dat");		
	    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
	    BufferedReader br = new BufferedReader(isr);	    
	    
	    String line;		
		
		//Step 2 read line per line...
	    while( (line = br.readLine()) != null)
	    {
	    	//Spliting the line
	    	String splitLine[] = line.split(" ");
	    	Double startTime = Double.parseDouble(splitLine[0]) / 1000; //Convert to second (millisecond in file)
	    	String value = splitLine[2].substring(2);
	    	Double uncertainty = Math.random()*(1-RELIABLETHRESH) + RELIABLETHRESH; //Random uncertainty: it's a reliable value, certainty > 0.5
	    	
	    	System.out.println("Loading event: "+value+" time:"+startTime+" uncert:"+uncertainty);
	    	OM.insertEvent("Hela", value, startTime, uncertainty);
	    	
	    	generateRandomFakeEvent(startTime, OM);
	    }
		
		br.close();
	}
	
	/**
	 * Generates a fake event with low uncertainty and random value
	 */
	public static void generateRandomFakeEvent(Double startTimeOrig, OntologyManager OM)
	{		
		if(Math.random() > FAKEEVENTRATE)
		{return;}
		
		ArrayList<String> values = new ArrayList<>();
		
		values.add("InteractwithLazychair");
//		values.add("OpenDoor");
//		values.add("OpenFridge");
//		values.add("CloseFridge");
		values.add("OpenDishwasher");
		values.add("CloseDishwasher");
//		values.add("OpenDrawer1");
//		values.add("OpenDrawer2");
//		values.add("OpenDrawer3");
//		values.add("CloseDrawer1");
//		values.add("CloseDrawer2");
//		values.add("CloseDrawer3");
//		values.add("FetchCup");
//		values.add("PutdownCup");
		values.add("FetchSugar");
		values.add("FetchMilk");
		values.add("PutdownMilk");
		values.add("FetchCup");
		values.add("PutdownCup");
//		values.add("FetchKnifeSalami");
//		values.add("PutdownKnifeSalami");
		values.add("FetchPlate");
		values.add("PutdownPlate");
		values.add("FetchGlass");
		values.add("PutdownGlass");
		values.add("FetchBread");
		values.add("PutdownBread");
//		values.add("FetchCheese");
//		values.add("PutdownCheese");
		values.add("FetchKnifeCheese");
		values.add("PutdownKnifeCheese");

    	Double startTime = startTimeOrig + (Math.random()*20 - 10); //Convert to second (millisecond in file)
    	String value = values.get( (int) Math.floor( Math.random() * values.size() ) );
    	Double uncertainty = Math.random()*RELIABLETHRESH/2; //Random uncertainty: it's a unreliable value, certainty < 0.5
    	
    	System.out.println("Loading random event: "+value+" time:"+startTime+" uncert:"+uncertainty);
    	OM.insertEvent("Hela", value, startTime, uncertainty);
	}
	
}

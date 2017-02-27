package agacy.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.jena.query.Dataset;

import agacy.datadriven.classification.SVMClassifier;
import agacy.datadriven.feature.FeatureExtraction;
import agacy.datadriven.feature.LearningProcess;
import agacy.knowledge.ontology.DatasetLoader;
import agacy.knowledge.ontology.OntologyManager;
import agacy.knowledge.ontology.RuleGenerator;
import agacy.knowledge.reasoning.ReasoningProcess;

public class mainDebug {

	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		System.out.println("Starting debug program...");
		
//		LearningProcess LP = new LearningProcess();
//		
//		try {
//			LP.extractAction();
//			LP.updateFeatures();
//			LP.saveFeatures();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
//		SVMClassifier svmc = new SVMClassifier();
//		svmc.generateModelFromFile();
		
//		ArrayList<String> events = new ArrayList<String>();
//		events.add("OpenDrawer1");
//		events.add("PutdownBread");
//		events.add("CloseDrawer1");
//		System.out.println( RuleGenerator.generateJenaActionRule(events, "PutawayBread", 10.0) );
//		
		
		OntologyManager OM = OntologyManager.getInstance();

//		OM.insertObservation("Hela", 20.0, "Sensor1", "Location", "Kitchen", 0.5);
//		OM.insertObservation("Hela", 22.0, "Sensor1", "Locomotion", "Sitted", 0.8);
//		OM.insertObservation("Hela", 21.0, "Sensor1", "Object", "Plate", 0.7);
//		OM.insertObservation("Hela", 21.0, "Sensor1", "Object", "Fork", 0.8);
//		OM.insertObservation("Hela", 19.0, "Sensor1", "Object", "Toaster", 0.8);
		
		for(int i=0; i<3; i++)
		for(int j=1; j<4; j++)
		{
			OM.reset();
			
			String setName = "S1"+i+"-ADL"+j;
			System.out.println("Dataset: "+setName);
		
			try {
				DatasetLoader.loadEventDataset("res/dataset/annotations/Levels-3-2-1/"+setName+"parsed.dat");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			ReasoningProcess RP = new ReasoningProcess(OM);
			RP.start();
			
			//Stopping for dataSet run (one run is enough)
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			RP.stop();
			
			FeatureExtraction FE = new FeatureExtraction(setName);
			FE.start();
			
			//OM.export();
			
			try {
				Thread.sleep(7000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			RP.stop(); //Deprecated, but it works... TODO
			FE.stop();
	
		}
		System.out.println("Closing debug program");
	}

}

package agacy.datadriven.classification;

import java.util.ArrayList;

import agacy.datadriven.feature.Feature;

public abstract class ActivityClassifier {

	
	//** Methods **//
	
	/**
	 * Load the activity from a file associating a set of observed features to actions.
	 * i.e. machine learning
	 */
	public abstract void generateModelFromFile();
	
	/**
	 * Recognize the activities from the generated features.
	 * Return the list of activity associated with a probability.
	 */
	public abstract void recognizeActivities( ArrayList<Feature> feats );
}

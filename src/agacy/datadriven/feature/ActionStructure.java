package agacy.datadriven.feature;

import java.util.ArrayList;

/**
 * Action representation for learning process
 * It carries the dependencies between an action and its composing events ("structure" of the action)
 * It is different from the "Action" class that represents the action's instance
 * @author ramol_na
 *
 */
public class ActionStructure {
	
	//** Attributes **//
	
	/**
	 * Name of the action (identifier)
	 * Shall be unique
	 */
	private String label;
	
	/**
	 * List of composing events
	 */
	private ArrayList<String> composingEvents;
	
	
	
	//** Methods **//
	

	/**
	 * Constructor
	 */
	public ActionStructure()
	{
		composingEvents = new ArrayList<String>();
	}
	
	public ActionStructure(String name, ArrayList<String> events)
	{
		this.label = name;
		this.composingEvents = events;
	}
	
	public String getLabel() {
		return label;
	}

	public ArrayList<String> getComposingEvents() {
		return composingEvents;
	}
	
	public void addEvents( ArrayList<String> newEvents )
	{
		for(String newEvent: newEvents)
		{
			//Check if not already in the list and insert if not
			if(!composingEvents.contains(newEvent))
			{ composingEvents.add(newEvent); }
		}
	}
	
	/**
	 * Compares two action based on their composing events.
	 * Returns  the degree of similarity (distance) between the two actions.
	 * We suppose one events is represented only ONCE per actions.
	 * @return 1 if the two actions are similar, 0 if they have no event in common, in between if partly similar
	 */
	public double compare(ActionStructure b)
	{
		
		//Going through the two event list
		ArrayList<String> eventListB = b.getComposingEvents();
		
		//Counter the number of similar events
		int simEvCnt = 0;
		
		for(String eventA: composingEvents)
		{
			for(String eventB: eventListB)
			{
				if(eventA.equals(eventB))
				{ simEvCnt++; }
			}
		}
		
		return ((double)simEvCnt) / ((double)Math.max(composingEvents.size(), eventListB.size()));
	}
}

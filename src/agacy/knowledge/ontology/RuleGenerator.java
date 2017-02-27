package agacy.knowledge.ontology;

import java.util.ArrayList;

/**
 * Generate Jena's rule from simple rules
 * @author ramol_na
 *
 */
public class RuleGenerator {

	/**
	 * Generata a rule to generate actions from events
	 * @param events
	 * @param maxDuration
	 * @return
	 */
	public static String generateJenaActionRule(ArrayList<String> events, String action, Double maxDuration)
	{
		String rule="";
		
		//Left part: events and constraint
		//Events
		for(int i=0; i<events.size(); i++)
		{
			rule += "(?user AGACY:hasEvent ?event"+i+"),\n"
					+ "(?event"+i+" rdf:type AGACY:Event),\n"
					+ "(?event"+i+" AGACY:hasValue \""+events.get(i)+"\"),\n"
					+ "(?event"+i+" AGACY:relatedTime ?time"+i+"),\n"
					+ "(?time"+i+"  AGACY:hasValue ?tval"+i+"),\n"
					+ "(?event"+i+" AGACY:uncertaintyLevel ?ulvl"+i+"),\n";
		}
		
		//Time dependencies
		for(int i=1; i<events.size(); i++)
		{
			String coupId = ""+(i-1)+""+i;
			
			//Difference of time
			rule += "difference(?tval"+i+", ?tval"+(i-1)+", ?diff"+coupId+"),\n"
					+"ge(?diff"+coupId+", 0.0),\n"
					+"le(?diff"+coupId+", "+maxDuration+"),\n";
		}
		
		//Uncertainty
		String uncertOff = "0"; //Last uncertain value
		for(int i=1; i<events.size(); i++)
		{
			String coupId = ""+uncertOff+""+i;
			
			//Min of uncertainty with prev value
			rule += "min(?ulvl"+uncertOff+", ?ulvl"+i+", ?ulvl"+coupId+"),\n";
			
			uncertOff = coupId;
		}
		
		//Action slot
		rule += "makeTemp(?action)\n";
		
		
		//Arrow...
		rule += "->\n";
		
		//Right part: action
		rule += "(?action rdf:type AGACY:Action),\n"
				+"(?user AGACY:hasAction ?action),\n"
				+"(?action AGACY:hasValue \""+action+"\"),\n"
				+"(?action AGACY:relatedTime ?time1),\n"; //Adjust the time
				
		//Event relation to action
		for(int i=1; i<events.size(); i++)
		{			
			//Difference of time
			rule += "(?action AGACY:composedOf ?event"+i+"),"
					+"(?event"+i+" AGACY:partOf ?action),";
		}
		
		rule += "(?action AGACY:uncertaintyLevel ?ulvl"+uncertOff+")\n\n";
		
		return rule;
	}
}

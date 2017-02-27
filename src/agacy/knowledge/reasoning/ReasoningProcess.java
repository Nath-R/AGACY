package agacy.knowledge.reasoning;

import agacy.knowledge.ontology.OntologyManager;

/**
 * Thread that periodically apply rules
 * @author ramol_na
 *
 */
public class ReasoningProcess extends Thread {

	/**
	 * Reference to ontology manager
	 */
	OntologyManager OM;
	
	/**
	 * Sleep duration in milliseconds
	 */
	long SLEEPDUR = 2000;
	
	
	/**
	 * Constructor
	 */
	public ReasoningProcess(OntologyManager OM) {
		this.OM = OM;
	}
	
	@Override
	public void run() {

		while(!Thread.currentThread().isInterrupted())
		{
			//Sleeping...		
			try {
				Thread.sleep(SLEEPDUR);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}		
			
			//Launching the inference process
			System.out.println("Applying rules...");
			
			OM.applyEventRules();
			
			OM.applyActionRules();
			
			OM.export(); //debug
			
			//Send new actions ?
		}
	}

}

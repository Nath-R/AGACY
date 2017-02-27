package agacy.knowledge.ontology;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.shared.uuid.JenaUUID;
import org.apache.jena.shared.uuid.UUID_V4_Gen;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;


/**
 * Manage the ontology.
 * The class regroup all the calls to Jena.
 * It uses the singleton design pattern.
 *  
 * @author ramol_na
 *
 */
public class OntologyManager 
{
	
	/**
	 * OntologyManager instance
	 */
	private static OntologyManager OM_INSTANCE = null;

	/**
	 * Main constructor
	 */
	private OntologyManager()
	{
		model = getModel();
		loadData(model);	
	}
	
	/**
	 * Access point
	 */
	public static synchronized OntologyManager getInstance()
	{
		if(OM_INSTANCE == null)
		{ OM_INSTANCE = new OntologyManager(); }
		
		return OM_INSTANCE;
	}
	
	
	//** Attributes **//
	
	/**
	 * Path of the physical and basic ontology
	 */
	public static final String ONTOPATH = "res/knowledge/ontology/AGACY-activity.owl";
	
	/**
	 * Path for the rules
	 */
	public static final String RULEPATHEVENT ="res/knowledge/reasoning/rulesEvents.txt";
	public static final String RULEPATHACTION ="res/knowledge/reasoning/rulesActions.txt";
	
	/**
	 * The current model of the ontology.
	 * Carries all the context data/
	 */
	private OntModel model;
	
	/**
	 * The model after inference (rules). 
	 */
	private InfModel actModel;
	
	
	
	//** Methods **//
	
	/**
	 * Create the ontological model.
	 * @return the instaciated model
	 */
	private OntModel getModel() {
        return ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
    }
	
	/**
	 * Fill the model m with the content of the physical ontology.
	 * @see ONTOPATH
	 * @param m model to fill
	 */
	private void loadData( Model m ) {
    	String path = new String(ONTOPATH);
        FileManager.get().readModel( m, path );
    }
	
	 /**
     * Reset
     * Restore to the emptied version of the ontology
     * Totally not optimal
     */
    public void reset()
    {
    	model.close();
    	model = getModel();
    	loadData(model);
    }
    
    
    /**
     * Add a triple into the ontology.
     * Triple is provided as string.
     */
    public void updateOntology( String subject, String predicate, String object )
    {
    	System.out.println("Updating context ontology...");
    	
    	String prefix = "prefix rdf: <" + RDF.getURI() + ">\n" +
                "prefix owl: <" + OWL.getURI() + ">\n"+
                "prefix xsd: <"+ XSD.getURI() +"> \n"+
                "prefix rdfs: <" + RDFS.getURI() + ">\n" +
                "prefix :<" + "http://nara.wp.tem-tsp.eu/ontologies/AGACY.owl#" + ">\n" ;  
    	
    	String query = prefix + "insert data {"+subject+" "+predicate+" "+object+"}";
    	
    	System.out.println(query);
    	
    	UpdateAction.parseExecute(query, model);	

    }
    
    public void insertData()
    {}
    
    public void query()
    {}
    
    public void remove()
    {}
    
    /**
     * Apply rules for event recognition
     */
    public void applyEventRules()
    {
    	Reasoner reasoner = new GenericRuleReasoner( Rule.rulesFromURL( RULEPATHEVENT ) );    	
    	
    	actModel = ModelFactory.createInfModel( reasoner, model );
    }
    
    /**
     * Apply rules to generate actions
     * Shall be called after the event generation.
     */
    public void applyActionRules()
    {
    	Reasoner reasoner = new GenericRuleReasoner( Rule.rulesFromURL( RULEPATHACTION ) );    	

    	actModel = ModelFactory.createInfModel( reasoner, actModel ); //Uses the inferred models with events
    }
    
    /**
     * Insert an observed context information in the ontology
     * @param user Name of the user (not the node URI)
     * @param time Timestamp of the observation
     * @param sensorName Name of the sensor (not URI)
     * @param objectType Type of the observed context data (location, temperature, etc...)
     * @param objectValue Value of the observed context data (kitchen, 10c, etc...)
     */
    public void insertObservation(String user, Double time, String sensorName, String objectType, String objectValue, double uncertainty)
    {
    	//First: create required node
    	//TODO check/add user
    	
    	UUID_V4_Gen idGen = new UUID_V4_Gen();
    	
    	//Creating a time instance
    	JenaUUID timeResId = idGen.generate();
    	//Resource timeRes = model.createResource(timeResId.asString());
    	//timeRes.addProperty(RDF.type, "AGACY:Time");
    	//timeRes.addLiteral(model.getProperty("AGACY:hasValue"), time);
    	
    	//Creating the object instance
    	//TODO check if type exist
    	JenaUUID objResId = idGen.generate();
    	//Resource objRes = model.createResource(objResId.asString());
    	//objRes.addProperty(RDF.type, "AGACY:"+objectType);
    	//objRes.addLiteral(model.getProperty("AGACY:hasValue"), objectValue);
    	
    	
    	
    	//Second: add with the relation on them all
    	String prefix = "prefix rdf: <" + RDF.getURI() + ">\n" +
                "prefix owl: <" + OWL.getURI() + ">\n"+
                "prefix xsd: <"+ XSD.getURI() +"> \n"+
                "prefix rdfs: <" + RDFS.getURI() + ">\n" +
                "prefix :<" + "http://nara.wp.tem-tsp.eu/ontologies/AGACY.owl#" + ">\n" ;  
    	
    	
    	String queryTimeRes =prefix + "insert data { 	"
    			+ ":"+timeResId+" rdf:type :Time . "
    			+ ":"+timeResId+" :hasValue '"+time+"'"
    			+ "}";
    	
    	String queryObjRes =prefix + "insert data { 	"
    			+ ":"+objResId+" rdf:type :"+objectType+" . "
    			+ ":"+objResId+" :hasValue '"+objectValue+"'"
    			+ "}";
    	
    	
    	String query = prefix + "insert { 	"
    			+ "?user :hasObject _:blanknode . " 
    			+ "_:blanknode rdf:type :Uncertainty ."
				+ "_:blanknode :relatedTime :"+timeResId+" . "	
				+ "_:blanknode :accordingTo  ?sensor . "
				+ "_:blanknode :relatedObject :"+objResId+" . "
				+ "_:blanknode :uncertaintyLevel '"+uncertainty+"' . "
    			+ "}"
    			+ "where{ "
    			+ "?user rdf:type :Person . ?user :hasName ?name . FILTER(?name='"+user+"'^^xsd:string ) ."
				+ "?sensor rdf:type :Sensor . ?sensor :hasName ?nameSen . FILTER(?nameSen='"+sensorName+"'^^xsd:string )}";
    	
    	//System.out.println(queryTimeRes);
    	//System.out.println(query);
    	
    	UpdateAction.parseExecute(queryTimeRes, model);
    	UpdateAction.parseExecute(queryObjRes, model);
    	UpdateAction.parseExecute(query, model);
    }
    
    /**
     * Insert an event in the ontology.
     * It is used for testing from a dataset where events are provided
     */
    public void insertEvent(String user, String value, Double time, Double uncertainty)
    {
    	UUID_V4_Gen idGen = new UUID_V4_Gen();

		//TODO check user/ add it
    	
    	//Creating a time instance
    	JenaUUID timeResId = idGen.generate();
    	//Resource timeRes = model.createResource(timeResId.asString());
    	//timeRes.addProperty(RDF.type, "AGACY:Time");
    	//timeRes.addLiteral(model.getProperty("AGACY:hasValue"), time);
    	
   	
    	//Second: add with the relation on them all
    	String prefix = "prefix rdf: <" + RDF.getURI() + ">\n" +
                "prefix owl: <" + OWL.getURI() + ">\n"+
                "prefix xsd: <"+ XSD.getURI() +"> \n"+
                "prefix rdfs: <" + RDFS.getURI() + ">\n" +
                "prefix :<" + "http://nara.wp.tem-tsp.eu/ontologies/AGACY.owl#" + ">\n" ;  
    	
    	
    	String queryTimeRes =prefix + "insert data { 	"
    			+ ":"+timeResId+" rdf:type :Time . "
    			+ ":"+timeResId+" :hasValue "+time+""
    			+ "}";    	
    	
    	String query = prefix + "insert { 	"
    			+ "?user :hasEvent _:blanknode . " 
    			+ "_:blanknode rdf:type :Event ."
    			+ "_:blanknode :hasValue \""+value+"\" . "
				+ "_:blanknode :relatedTime :"+timeResId+" . "
				+ "_:blanknode :uncertaintyLevel "+uncertainty+" "
    			+ "}"
    			+ "where{ "
    			+ "?user rdf:type :Person . ?user :hasName ?name . FILTER(?name='"+user+"'^^xsd:string )"
				+ "}";
    	
    	UpdateAction.parseExecute(queryTimeRes, model);
    	UpdateAction.parseExecute(query, model);
    	
    }
    
    /**
     * Save the current model as a turtle file
     */
    public void export()
    {
    	FileWriter out = null;
    	try {

    	  // OR Turtle format - compact and more readable
    	  // use this variant if you're not sure which to use!
    	  out = new FileWriter( "res/knowledge/ontology/exportModel.ttl" );
    	  model.write( out, "Turtle" );
    	  
    	  out = new FileWriter( "res/knowledge/ontology/exportInfModel.ttl" );
    	  actModel.write( out, "Turtle" );
    	} catch (IOException e) {
			e.printStackTrace();
		}
    	finally {
    	  if (out != null) {
    	    try {out.close();} catch (IOException ignore) {}
    	  }
    	}
    }
    
    /**
     * Returns the actions presents in the ontology
     */
    public ArrayList <String> extractActions()
    {
    	String prefix = "prefix rdf: <" + RDF.getURI() + ">\n" +
                "prefix owl: <" + OWL.getURI() + ">\n"+
                "prefix xsd: <"+ XSD.getURI() +"> \n"+
                "prefix rdfs: <" + RDFS.getURI() + ">\n" +
                "prefix AGACY:<" + "http://nara.wp.tem-tsp.eu/ontologies/AGACY.owl#" + ">\n" ;  
    	
    	String queryStr = prefix +
                "select ?actionVal ?time ?unLevel \n "+ 
                "where {"+
                "		 ?action a AGACY:Action . \n " +  
                "        ?action AGACY:hasValue ?actionVal . \n" +
                "        ?action AGACY:relatedTime ?timeVal . \n" +
                "        ?timeVal AGACY:hasValue ?time . \n" +
                "        ?action AGACY:uncertaintyLevel ?unLevel     \n" +        
                "}";
    	
//    	String queryStr = prefix +
//              "select ?user ?value \n "+ 
//              "where {"+
//              "		 ?user AGACY:composedOf ?value  \n "  + 
//              "}";
    	
    	 ArrayList <String> res = new ArrayList <String> ();
         //System.out.println(q);
         Query query = QueryFactory.create( queryStr );
         QueryExecution qexec = QueryExecutionFactory.create( query, actModel );
         try {
             ResultSet results = qexec.execSelect();            
             //ResultSetFormatter.out( results, m );
             
             while (results.hasNext()) {
                   QuerySolution solution = results.next();
                   // get the value of the variables in the select clause
                   String value = solution.get("actionVal").asLiteral().getLexicalForm();
                   res.add(value);
                   String time = solution.get("time").asLiteral().getLexicalForm();
                   res.add(time);
                   String unLevel = solution.get("unLevel").asLiteral().getLexicalForm();
                   res.add(unLevel);
                   //System.out.println(solution.get("value"));
             }
         }
         finally {
             qexec.close();
         }
         
         return res;
    }
    
    
    public void debug_test_insertion()
    {
    	
    	
    	String prefix = "prefix rdf: <" + RDF.getURI() + ">\n" +
                "prefix owl: <" + OWL.getURI() + ">\n"+
                "prefix xsd: <"+ XSD.getURI() +"> \n"+
                "prefix rdfs: <" + RDFS.getURI() + ">\n" +
                "prefix :<" + "http://nara.wp.tem-tsp.eu/ontologies/AGACY.owl#" + ">\n" ;  
    	
    	String query = prefix + "insert { 	"
    			+ "?user :hasObject _:blanknode ."    			
				+ "_:blanknode :relatedTime '10'"
				
    			+ "}"
    			+ "where{ ?user rdf:type :Person . ?user :hasName 'Hela'}";
    	
    	System.out.println(query);
    	
    	UpdateAction.parseExecute(query, model);
    	
    	//Save the model to visualize the update
    	FileWriter out = null;
    	try {

    	  // OR Turtle format - compact and more readable
    	  // use this variant if you're not sure which to use!
    	  out = new FileWriter( "res/knowledge/ontology/testModel.ttl" );
    	  model.write( out, "Turtle" );
    	} catch (IOException e) {
			e.printStackTrace();
		}
    	finally {
    	  if (out != null) {
    	    try {out.close();} catch (IOException ignore) {}
    	  }
    	}
    }
}

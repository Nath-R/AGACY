package agacy.datadriven.feature;

/**
 * Java representation of the action extracted and carried in the ontology
 * @author ramol_na
 *
 */
public class Action {

	//** Attribute **//
	
	String value; //~name
	
	double time;

	double uncertainty; //Uncertainty level
	
	//** Methods **//
	

	public Action(String value, double time, double uncertainy)
	{
		this.value = value;
		this.time = time;
		this.uncertainty = uncertainy;
	}
	
	
	public String toString()
	{
		return value+"(date:"+time+", uncertainty:"+uncertainty+")";
	}
	
	
	public double getTime() {
		return time;
	}
	
	
	public String getValue() {
		return value;
	}


	public void setValue(String value) {
		this.value = value;
	}
	
	public double getUncertainty() {
		return uncertainty;
	}

	public void setUncertainty(double uncertainty) {
		this.uncertainty = uncertainty;
	}

}

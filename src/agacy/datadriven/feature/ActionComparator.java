package agacy.datadriven.feature;
import java.util.Comparator;

/**
 * Comparator for action in the treeSet
 * @author ramol_na
 *
 */
class ActionComparator implements Comparator<Action>
{

	@Override
	public int compare(Action o1, Action o2) {
		if(o1.getTime() > o2.getTime())
		{return 1;}
		else
		{return -1;}
	}
	
}

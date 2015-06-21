package geek.nerd.variablecomparators;

import java.util.Comparator;

import abscon.instance.components.PVariable;

public class SortByName implements Comparator<PVariable> {

	public SortByName() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compare(PVariable o1, PVariable o2) {
		// TODO Auto-generated method stub
		return o1.getName().compareTo(o2.getName());
	}

}

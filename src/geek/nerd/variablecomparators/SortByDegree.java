package geek.nerd.variablecomparators;

import java.util.Comparator;

import abscon.instance.components.PVariable;

public class SortByDegree implements Comparator<PVariable> {

	public SortByDegree() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compare(PVariable v1, PVariable v2) {
		return ((v2.getNeighbors().entrySet().size() == v1.getNeighbors().entrySet().size())? v1.getName().compareTo(v2.getName()) : v2.getNeighbors().entrySet().size() - v1.getNeighbors().entrySet().size());
	}

}

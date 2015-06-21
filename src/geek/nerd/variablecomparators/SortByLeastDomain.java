package geek.nerd.variablecomparators;

import java.util.Comparator;

import abscon.instance.components.PVariable;

public class SortByLeastDomain implements Comparator<PVariable> {

	public SortByLeastDomain() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compare(PVariable o1, PVariable o2) {
		// TODO Auto-generated method stub
		return (o1.getCurrentDomain().size() == o2.getCurrentDomain().size() ? o1.getName().compareTo(o2.getName()) : o1.getCurrentDomain().size() - o2.getCurrentDomain().size());
	}

}

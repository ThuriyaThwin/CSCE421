package geek.nerd.variablecomparators;

import java.util.Comparator;

import abscon.instance.components.PVariable;

public class SortByDomainDegree implements Comparator<PVariable> {

	public SortByDomainDegree() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compare(PVariable o1, PVariable o2) {
		if (o1.getNeighbors().isEmpty()) {
			return 1;
		}else if (o2.getNeighbors().isEmpty()) {
			return -1;
		}else{
			return (o1.getDomain().getValues().length/o1.getNeighbors().entrySet().size() == o2.getDomain().getValues().length/o2.getNeighbors().entrySet().size() ? o1.getName().compareTo(o2.getName()) : o1.getDomain().getValues().length/o1.getNeighbors().entrySet().size() - o2.getDomain().getValues().length/o2.getNeighbors().entrySet().size());
		}
	}

}

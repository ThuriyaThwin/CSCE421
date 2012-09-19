package abscon.instance.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PVariable {
	private String name;

	private PDomain domain;
	
	private List<Integer> currentDomain;
	
	private List<PConstraint> constraints= new ArrayList<PConstraint>();
	
	private Map<PConstraint, PVariable> neighbors = new HashMap<PConstraint, PVariable>();
	
	private List<PVariable> neighborVariables = new ArrayList<PVariable>();
	
	private int fval;


	public PVariable(String name, PDomain domain) {
		this.name = name;
		this.domain = domain;
		this.currentDomain = new ArrayList<Integer>();
		for (int i : domain.getValues()) {
			this.currentDomain.add(i);
		}
		this.fval = 0;
	}
	
	
	public List<PVariable> getNeighborVariables() {
		return neighborVariables;
	}


	public void setNeighborVariables() {
		for (PConstraint c : constraints) {
			for (PVariable v : c.getScope()) {
				if (v != this && !neighborVariables.contains(v)){
					neighborVariables.add(v);
				}
			}
		}
		
	}
	
	public int getFval() {
		return this.domain.getValues().length-this.currentDomain.size();
	}

	public void removeDomainValue(Integer i) {
		this.currentDomain.remove(i);
	}

	public List<Integer> getCurrentDomain() {
		return currentDomain;
	}


	public void setcurrentDomain(List<Integer> currentDomain) {
		this.currentDomain = currentDomain;
	}


	public String getName() {
		return name;
	}

	public PDomain getDomain() {
		return domain;
	}
	
	public void putNeighbor(PConstraint constraint, PVariable neighbor) {
		if (neighbors.isEmpty()){
			neighbors.put(constraint, neighbor);
		}else if (!neighbors.containsKey(constraint)) {
			neighbors.put(constraint, neighbor);
		}
	}

	public Map<PConstraint, PVariable> getNeighbors() {
		return neighbors;
	}

	public void addConstraint(PConstraint constraint) {
		if (!constraints.contains(constraint)) {
			constraints.add(constraint);
		}
	}

	public List<PConstraint> getConstraints() {
		return constraints;
	}


	@Override
	public String toString() {
		String s = "";
		for (int i = 0; i < domain.getValues().length; i++) {
			s = s+" "+domain.getValues()[i];
		}
		String c = "";
		for (int i = 0; i < constraints.size(); i++) {
			c = c + " " + constraints.get(i).getName();
		}
		String nbs = "";
		for (PVariable v : neighborVariables) {
			nbs = nbs + " " + v.getName();
		}
		
		String modifiedDomain = "";
		for (int d : this.currentDomain) {
			modifiedDomain = modifiedDomain + " " + d;
		}
		
		String result = "  Variable " + name + "\n    Associated domain " + domain.getName() + ", values in the domain: " + s + "\n" + "    Associated constraints: " + c + "\n" + "    Neighbors: "+nbs + "\n" + "    Current Domain: " + modifiedDomain + "\n";
		result = result + "fval: " + this.getFval() + "\n";
		return result;
	}
}

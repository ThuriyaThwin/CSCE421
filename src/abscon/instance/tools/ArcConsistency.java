package abscon.instance.tools;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import abscon.instance.components.PConstraint;
import abscon.instance.components.PVariable;

public class ArcConsistency {

	private PVariable[] variables;
	private PConstraint[] constraints;
	private int numOfCC;
	private int fval;
	private List<Pair> currentPair;
	private List<Pair> originalPair;
	private double iSize;
	private double fSize;
	private double fEffect;
	private long setupTime;
	private long cpu;
	
	public ArcConsistency(PVariable[] variables, PConstraint[] constraints) {
		this.variables = variables;
		this.constraints = constraints;
		this.numOfCC = 0;
		this.fval = 0;
		this.currentPair = new ArrayList<Pair>();
		this.originalPair = new ArrayList<Pair>();
		iSize = 1;
		fSize = 1;
		fEffect = 0.00;
		setupTime = 0;
		cpu = 0;
	}

	public void checkNodeConsistency() {
		//long start = getCpuTime();

		for (PConstraint c : this.constraints) {
			if (c.getScope().length == 1) {
				List<Integer> temp = new ArrayList<Integer>();
				for (Integer j : c.getScope()[0].getCurrentDomain()) {
					temp.add(j);
				}
				for (Integer i : temp) {
					if (c.computeCostOf(new int[]{i.intValue()}) == 1) {
						c.getScope()[0].removeDomainValue(i);
						fval ++;
					}
				}
			}
		}
		
		//this.cpu += (this.getCpuTime()-start);
	}
	
	public boolean check(PVariable vi, PVariable vj, Integer viValue, Integer vjValue, PConstraint c, char flag){
		if (this.getConstraintBetweenVariables(vi, vj) == null) {
			return true;
		}
		
		if (vi.getNeighbors().containsValue(vj)){
			numOfCC ++;
			if (flag == 'P'){
				return (c.computeCostOf(new int[]{viValue.intValue(), vjValue.intValue()}) == 0 ? true : false);
			}else {
				return (c.computeCostOf(new int[]{vjValue.intValue(), viValue.intValue()}) == 0 ? true : false);
			}
		} else {
			return true;
		}
		
	}
	
	public boolean supported(PVariable vi, PVariable vj, Integer viValue, PConstraint c, char flag) {
		boolean supported = false;
		
		List<Integer> temp = new ArrayList<Integer>();
		for (Integer j : vj.getCurrentDomain()) {
			temp.add(j);
		}
		
		for (Integer vjValue : temp) {
			if (this.check(vi, vj, viValue, vjValue, c, flag)){
				supported = true;
				return supported;
			}			
		}
		
		return supported;
	}
	
	public boolean revised(PVariable vi, PVariable vj, PConstraint c, char flag) {
		boolean revised = false;
		
		List<Integer> temp = new ArrayList<Integer>();
		for (Integer j : vi.getCurrentDomain()) {
			temp.add(j);
		}
		
		for (Integer viValue : temp) {

			boolean found = supported(vi, vj, viValue, c, flag);
			
			if (!found) {
				revised = true;
				vi.getCurrentDomain().remove(viValue);
				fval ++;
			}
		}
		return revised;
	}
	
	public boolean arcConsistencyOne() {
		long startAlgo = getCpuTime();

		this.checkNodeConsistency();
		boolean change = false;
		boolean done = true;
		

		do {
			change = false;
			for (PConstraint c : constraints ){
				
				if (c.getArity()!=1) {

					boolean updated1 = revised(c.getScope()[0], c.getScope()[1], c, 'P');
					boolean updated2 = revised(c.getScope()[1], c.getScope()[0], c, 'N');
					
					if (c.getScope()[0].getCurrentDomain().isEmpty()) {
						return false;
					} else {
						change = (updated1 || change || updated2);
					}
				}
			}
		} while (change);
		this.cpu = getCpuTime() - startAlgo;

		return done; 
	}
	
	public boolean arcConsistencyThree() {
		long startAlgo = getCpuTime();

		this.checkNodeConsistency();
		boolean change = false;
		boolean done = true;
		
		long start = getCpuTime();
		for (PConstraint c : constraints) {
			if (c.getArity()!=1){
			Pair<PVariable, PVariable> newL = new Pair<PVariable, PVariable>(c.getScope()[0], c.getScope()[1], c, 'P');
			Pair<PVariable, PVariable> newR = new Pair<PVariable, PVariable>(c.getScope()[1], c.getScope()[0], c, 'N');
			currentPair.add(newL);
			currentPair.add(newR);
			originalPair.add(newL);
			originalPair.add(newR);
			}
		}
		this.setupTime = getCpuTime() - start;
		
		while (!currentPair.isEmpty()) {
			Pair<PVariable, PVariable> current = currentPair.get(0);
			currentPair.remove(current);
			if (revised(current.getLeft(), current.getRight(), current.getConstraint(), current.getFlag())) {
				for (Pair p : originalPair) {
					if (p.getRight().equals(current.getLeft()) && !currentPair.contains(p)) {
						currentPair.add(p);
					}
				}
			}
			for (PVariable v : variables) {
				if (v.getCurrentDomain().isEmpty()) {
					return false;
				}
			}
		}
		

		this.cpu = getCpuTime() - startAlgo;
		
		return done;
	}
	
	public PConstraint getConstraintBetweenVariables(PVariable vi, PVariable vj) {
		Iterator i = vi.getNeighbors().keySet().iterator();
		while (i.hasNext()){
			PConstraint key = (PConstraint) i.next();
			if (vi.getNeighbors().get(key).equals(vj)){
				return key;
			}
		}
		return null;
	}
	
	
	/** Get CPU time in nanoseconds. */
	public long getCpuTime() {
	    ThreadMXBean bean = ManagementFactory.getThreadMXBean();
	    return bean.isCurrentThreadCpuTimeSupported( ) ?
	        bean.getCurrentThreadCpuTime() : 0L;
	}
	 
	/** Get user time in nanoseconds. */
	public long getUserTime() {
	    ThreadMXBean bean = ManagementFactory.getThreadMXBean();
	    return bean.isCurrentThreadCpuTimeSupported() ?
	        bean.getCurrentThreadUserTime() : 0L;
	}

	/** Get system time in nanoseconds. */
	public long getSystemTime() {
	    ThreadMXBean bean = ManagementFactory.getThreadMXBean();
	    return bean.isCurrentThreadCpuTimeSupported() ?
	        (bean.getCurrentThreadCpuTime() - bean.getCurrentThreadUserTime()) : 0L;
	}
/*	*//**
	 * Test if the original domain has been deep copied to a new list.
	 *//*
	public void testDomain() {
		String domainValues = "";
		for (int i : variables[0].getCurrentDomain()) {
			domainValues = domainValues + i;
		}
		
		System.out.println(domainValues);
		
		variables[0].getCurrentDomain().clear();
		System.out.println(variables[0].getCurrentDomain().isEmpty());
		
		String modified = "";
		for (int i : variables[0].getDomain().getValues()){
			modified += i;
		}
		System.out.println("Original domain after modifying: " + modified);
	}*/
	

	public int getNumOfCC() {
		return numOfCC;
	}

	public void setNumOfCC(int numOfCC) {
		this.numOfCC = numOfCC;
	}

	public int getFval() {
		return fval;
	}

	public void setFval(int fval) {
		this.fval = fval;
	}
	
	public double getFEffect() {
		fEffect = (double)this.getISize() / (double)this.getFSize();
		return this.fEffect;
	}

	public double getFSize() {
		for (PVariable v : variables) {
			this.fSize *= v.getCurrentDomain().size();
		}
		
		if (this.fSize > 1000000000){
			this.fSize = Math.log(this.fSize);
		}
		
		return this.fSize;
	}
	
	public double getISize() {
		for (PVariable v : variables) {
			this.iSize *= v.getDomain().getValues().length;
		}
		
		if (this.iSize > 1000000000) {
			this.iSize = Math.log(this.iSize);
			//System.out.print("iSize too large. Log of original value: ");
		}
		
		return this.iSize;
	}

	public double getSetupTime() {
		return (double)this.setupTime/1000000;
	}

	public double getCpu() {
		return (double)this.cpu/1000000;
	}
	
	
}

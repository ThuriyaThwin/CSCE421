package abscon.instance.components;

import geek.nerd.csp.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;


public class PVariable {
	private String name;
	private PDomain domain;
	private List<Integer> currentDomain;
	private List<PConstraint> constraints= new ArrayList<PConstraint>();
	private Map<PConstraint, PVariable> neighbors = new HashMap<PConstraint, PVariable>();
	private List<PVariable> neighborVariables = new ArrayList<PVariable>();
	private int fval;
	private List<Integer> initialDomain;
	private List<Integer> confSet;
	private List<PVariable> confVar;
	private Stack<ArrayList<Integer>> reductions;
	private List<Integer> futureFC;
	private List<Integer> pastFC;
	private List<PVariable> futurefcVariable;
	private List<PVariable> pastfcVariable;
	private Map<Pair<PVariable, Integer>, Integer> lastSupport;
	private int cbf;
	private List<PVariable> widthNeighbors;
	private boolean reducted;

	public PVariable(String name, PDomain domain) {
		this.name = name;
		this.domain = domain;
		this.currentDomain = new ArrayList<Integer>();
		initialDomain = new ArrayList<Integer>();
		for (int i : domain.getValues()) {
			this.initialDomain.add(i);
		}
		this.fval = 0;
		this.confSet = new ArrayList<Integer>();
		this.reductions = new Stack<ArrayList<Integer>>();
		this.futureFC = new ArrayList<Integer>();
		this.pastFC = new ArrayList<Integer>();
		this.futurefcVariable = new ArrayList<PVariable>();
		this.pastfcVariable = new ArrayList<PVariable>();
		this.lastSupport = new HashMap<Pair<PVariable,Integer>, Integer>();
		this.confVar = new ArrayList<PVariable>();
		this.cbf = -1;
		this.widthNeighbors = new ArrayList<PVariable>();
		this.reducted = false;
	}

	public void pushReduction(ArrayList<Integer> reduction){
		this.reductions.push(reduction);
	}

	public void addFromRevise(Integer value){
		if(isReducted()){
			if(this.reductions.isEmpty()){
				ArrayList<Integer> reduction = new ArrayList<Integer>();
				reduction.add(value);
				this.reductions.push(reduction);
			}else{
				this.reductions.peek().add(value);
			}
		}else{
			ArrayList<Integer> reduction = new ArrayList<Integer>();
			reduction.add(value);
			this.reductions.push(reduction);
		}
	}

	public void addReductionsBack(){
		if (!this.reductions.isEmpty()){
			for (Integer value : this.reductions.pop()){
				if(!this.currentDomain.contains(value)){
					this.currentDomain.add(value);
				} else {
					throw new RuntimeException("Issue with restoring reduction");
				}
			}
		}
	}

	public void resetEverything(){
		this.reductions = new Stack<ArrayList<Integer>>();
		this.pastfcVariable = new ArrayList<PVariable>();
		this.futurefcVariable = new ArrayList<PVariable>();
		this.resetCurrentDomain();
		this.confVar = new ArrayList<PVariable>();
	}
	public void removeFromPastFC(Integer variableIndex){
		if(!this.pastFC.isEmpty()){
			if(this.pastFC.contains(variableIndex)){
				this.pastFC.remove(variableIndex);
			}
		}
	}

	public void removeFromPastFCVariable(PVariable v) {
		if(!this.pastfcVariable.isEmpty()){
			if(this.pastfcVariable.contains(v)){
				this.pastfcVariable.remove(v);
			}
		}
	}

	public void setLastSupported(Pair<PVariable, Integer> pair, Integer value){
		this.lastSupport.put(pair, value);
	}

	public String getLastSupported(Pair<PVariable, Integer> pair){
		if(this.lastSupport.get(pair) != null){
			return this.lastSupport.get(pair).toString();
		}
		return null;
	}

	public void pushConfVar(PVariable v ){
		if(!this.confVar.contains(v)) {
			this.confVar.add(v);
		}
	}

	public List<PVariable> getConfVar() {
		return confVar;
	}

	public void addFutureFCVariable(PVariable v){
		if(!futurefcVariable.contains(v)){
			this.futurefcVariable.add(v);
		}
	}
	public void resetFutureFCVariable(){
		this.futurefcVariable = new ArrayList<PVariable>();
	}

	public void addPastFCVariable(PVariable v){
		if(!this.pastfcVariable.contains(v)){
			this.pastfcVariable.add(v);
		}
	}

	public void resetFutureFC(){
		this.futureFC = new ArrayList<Integer>();
	}

	public void addFutureFC(int value){
		this.futureFC.add(value);
	}

	public void addPastFC(int value){
		this.pastFC.add(value);
	}


	public List<Integer> getFutureFC() {
		return futureFC;
	}

	public void setNeighborVariables() {
		for (PConstraint c : constraints) {
			for (PVariable v : c.getScope()) {
				if (v != this && !neighborVariables.contains(v)){
					neighborVariables.add(v);
				}
			}
		}

		for (PVariable v : neighborVariables){
			widthNeighbors.add(v);
		}
	}

	public void removeWidthneighbor(PVariable v){
		if(widthNeighbors.contains(v)){
			widthNeighbors.remove(v);
		}
	}

	public List<PVariable> getWidthNeighbors() {
		return widthNeighbors;
	}

	public void printConfSet() {
		if (this.confSet.isEmpty()) {
			System.out.println(this.name + " Conf-set: " + "EMPTY");
		}else {
			String results = "";
			for (Integer i : this.confSet) {
				results += i;
			}
			System.out.println(this.name + " Conf-set: " + results);
		}
	}
	public void pushNew(Integer value) {
		if (!this.confSet.contains(value)){
			this.confSet.add(value);
		}
	}

	public void removeConf(Integer value) {
		if (this.confSet.contains(value)){
			this.confSet.remove(value);
		}
	}

	public void removeConfVar(PVariable var){
		if (this.confVar.contains(var)){
			this.confVar.remove(var);
		}
	}

	public void resetConfSet() {
		this.confVar = new ArrayList<PVariable>();
	}

	public Integer maxConfSet(){
		Integer maxConf = Collections.max(confSet);
		return maxConf;
	}

	public Integer maxConfSetPastFC(){
		Integer maxConf;
		Integer maxPastFC;
		if(confSet.isEmpty()){
			maxConf = 0;
		}else{
			maxConf = Collections.max(confSet);
		}
		if(pastFC.isEmpty()){
			maxPastFC = 0;
		}else{
			maxPastFC = Collections.max(pastFC);
		}
		return (maxConf > maxPastFC ? maxConf : maxPastFC);
	}

	public void resetCurrentDomain() {
		this.currentDomain.clear();
		for (int i : initialDomain) {
			this.currentDomain.add(i);
		}
	}

	public boolean isCurrentDomainEmpty() {
		return this.currentDomain.isEmpty();
	}

	public int getFval() {
		return this.domain.getValues().length-this.currentDomain.size();
	}

	public void removeFromCurrent(Integer i) {
		this.currentDomain.remove(i);
	}

	public void removeFromInitial(Integer i) {
		this.initialDomain.remove(i);
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

	public List<Integer> getInitialDomain() {
		return initialDomain;
	}

	public List<Integer> getConfSet() {
		return confSet;
	}

	public Stack<ArrayList<Integer>> getReductions() {
		return reductions;
	}

	public List<PVariable> getNeighborVariables() {
		return neighborVariables;
	}

	public List<PVariable> getFuturefcVariable() {
		return futurefcVariable;
	}

	public List<Integer> getPastFC() {
		return pastFC;
	}

	public int getCbf() {
		return cbf;
	}

	public void setCbf(int cbf) {
		this.cbf = cbf;
	}

	public boolean isReducted() {
		return reducted;
	}

	public void setReducted(boolean reducted) {
		this.reducted = reducted;
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
		for (int d : this.initialDomain) {
			modifiedDomain = modifiedDomain + " " + d;
		}
		String result = ""	;
		result = " " + name + "\n"+ "    Initial Domain: " + modifiedDomain + "\n";//    Associated domain " + domain.getName() + ", values in the domain: " + s + "\n" + "    Associated constraints: " + c + "\n" + "    Neighbors: "+nbs + "\n" + "    Current Domain: " + modifiedDomain + "\n";
		//result = result + "fval: " + this.getFval() + "\n";
		return result;
	}

	public List<PVariable> getPastfcVariable() {
		return pastfcVariable;
	}
}

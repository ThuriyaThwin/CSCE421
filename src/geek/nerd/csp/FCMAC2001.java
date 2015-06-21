package geek.nerd.csp;

import geek.nerd.variablecomparators.SortByDegree;
import geek.nerd.variablecomparators.SortByDomainDegree;
import geek.nerd.variablecomparators.SortByLeastDomain;
import geek.nerd.variablecomparators.SortByName;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
/*
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;*/

import abscon.instance.components.PConstraint;
import abscon.instance.components.PVariable;

public class FCMAC2001 {

	private PVariable[] variables;
	private PConstraint[] constraints;
	private int firstNumOfcc;
	private int firstNumOfnv;
	private int firstNumOfbt;
	private int numOfcc;
	private int numOfnv;
	private int numOfbt;
	private String status;
	private int numOfVariables;
	private boolean consistent;
	private Stack<Pair<PVariable, Integer>> currentPath;
	private int[] solution;
	private int numOfSolutions;
	private boolean firstTimeAfterSolution;
	private boolean firstSolution;
	private String variableOrderingHeuristic;
	private String varStaticDynamic;
	private String valueOrderingHeuristic;
	private String valStaticDynamic;
	private long firstSolutionTime;
	private long allSolutionsTime;
	private long preProcessingTime;
	private String strategy;
	private ArrayList<PVariable> unassignedVariables;
	private Stack<PVariable> instantiatedVariables;
	private List<Pair> currentPair;
	private List<Pair> originalPair;
	private String fileName;

	public FCMAC2001(PVariable[] variables, PConstraint[] constraints, String orderStrategy, String fileName) {
		this.fileName = fileName;
		this.variables = variables;
		this.constraints = constraints; 
		this.numOfcc = 0;
		this.numOfnv = 0;
		this.numOfbt = 0;
		this.firstNumOfcc = 0;
		this.firstNumOfnv = 0;
		this.firstNumOfbt = 0;
		this.numOfSolutions = 0;
		this.status = "unknown";
		this.numOfVariables = variables.length;
		this.consistent = false;
		this.currentPath = new Stack<Pair<PVariable,Integer>>();
		this.solution = new int[variables.length];
		this.firstTimeAfterSolution = false;
		this.firstSolution = true;
		if(orderStrategy.equalsIgnoreCase("-uddd")){
			this.variableOrderingHeuristic = "domain degree domain";
		}else if(orderStrategy.equalsIgnoreCase("-uddeg")){
			this.variableOrderingHeuristic = "degree domain";
		}else if(orderStrategy.equalsIgnoreCase("-udld")){
			this.variableOrderingHeuristic = "least domain";
		}else if(orderStrategy.equalsIgnoreCase("-udlx")){
			this.variableOrderingHeuristic = "lexicographical";
		}else{
			this.variableOrderingHeuristic = "wrong ordering argument";
		}
		this.varStaticDynamic = "Dynamic";
		this.valueOrderingHeuristic = "not applicable";
		this.valStaticDynamic = "not applicable";
		this.firstSolutionTime = 0;
		this.allSolutionsTime = 0;
		this.strategy = orderStrategy;
		this.instantiatedVariables = new Stack<PVariable>();
		this.unassignedVariables = new ArrayList<PVariable>();
		/*for(PVariable v : variables){
			this.unassignedVariables.add(v);
		}*/
		this.currentPair = new ArrayList<Pair>();
		this.originalPair = new ArrayList<Pair>();
	}

	public String startFCMAC2001() throws IOException{//, InvalidFormatException {
		
		long startTime =0;
		this.sortUnassigned();
		if(preac2001()){
			for(PVariable v : variables){
				this.unassignedVariables.add(v);
			}
			for (PVariable v : variables) {
				v.resetCurrentDomain();
			}
			startTime = getCpuTime();
			status = bcssp();
		}else{
			status = "impossible";
		}
		if (status.equalsIgnoreCase("Solution") || numOfSolutions > 0) {
			//System.out.println("Number of solution found: " + numOfSolutions);
			/*int i = 0;
			for (Pair<PVariable, Integer> p : currentPath) {
				solution[i] = p.getRight().intValue();
				i++;
			}*/
			//System.out.println("===========================================");
			//printCurrentPath();
		}else{
			System.out.println("There is no solution.");
		}

		long endTime = getCpuTime();
		allSolutionsTime = (endTime - startTime + preProcessingTime)/1000000;
//		writeToExcel();
		System.out.println("===========================================");
		System.out.println("All Solutions.");
		System.out.println("#Solutions: " + numOfSolutions);
		System.out.println("cc : " + numOfcc);
		System.out.println("nv : " + numOfnv);
		System.out.println("bt : " + numOfbt);
		System.out.println("cpu : " + allSolutionsTime + " ms");
		System.out.printf("%-30s %-15s %n","variable-ordering-heuristic :", variableOrderingHeuristic);
		System.out.printf("%-30s %-15s %n","var-static-dynamic :" , varStaticDynamic);
		System.out.printf("%-30s %-15s %n","value-ordering-heuristic :" , valueOrderingHeuristic);
		System.out.printf("%-30s %-15s %n","val-static-dynamic :" , valStaticDynamic);
		//System.out.println(fileName + "  Written.");
		//System.out.println("===========================================");

		return status;
	}

	public String bcssp() {
		long startTime = getCpuTime();
		consistent = true;
		status = "unknown";
		while (status.equalsIgnoreCase("unknown")) {
			if (consistent) {
				this.sortUnassigned();
				fcmac2001label();
				//				System.out.println("After fclabel");
				//				this.printStatus();
			}else {
				fcmac2001unlabel();
				numOfbt++;
				//				System.out.println("After fcunlabel");
				//				this.printStatus();
				this.sortUnassigned();
			}

			if (consistent && this.unassignedVariables.isEmpty()) {
				numOfSolutions ++;
				long endTime = getCpuTime();
				firstSolutionTime = (endTime-startTime + preProcessingTime)/1000000;
				firstNumOfcc = numOfcc;
				firstNumOfnv = numOfnv;
				firstNumOfbt = numOfbt;
				//status = "solution";
				if (firstSolution) {
					System.out.println("===========================================");
					System.out.println("First Solutions.");
					System.out.println("cc : " + firstNumOfcc);
					System.out.println("nv : " + firstNumOfnv);
					System.out.println("bt : " + firstNumOfbt);
					System.out.println("cpu : " + firstSolutionTime + " ms");
					//printCurrentPath();
					//printVariables();
					System.out.println("===========================================");
					firstSolution = false;
					
					
					//This is special for phase transition
//					return "solution";
					
				}
				firstTimeAfterSolution = true;
			}else if (this.instantiatedVariables.isEmpty() && numOfSolutions > 0 && this.unassignedVariables.get(unassignedVariables.size()-1).isCurrentDomainEmpty()) {
				//numOfSolutions++;
				status = "solution";
			}else if (this.instantiatedVariables.isEmpty() && this.unassignedVariables.get(0).isCurrentDomainEmpty()){
				status = "impossible";
			}
		}

		return status;
	}

	public void fcmac2001label(){
		this.consistent = false;
		int valueIndex = 0;
		//System.out.println("Labeling: " + this.unassignedVariables.get(0).getName());
		if (numOfSolutions > 0 && firstTimeAfterSolution){
			//currentPath.peek().getLeft().removeFromCurrent(currentPath.peek().getRight());
			//System.out.println(currentPath.peek().getLeft().getName() + " value " + currentPath.peek().getRight());
			//currentPath.pop();
			int value = this.instantiatedVariables.peek().getCurrentDomain().get(0);
			this.instantiatedVariables.peek().removeFromCurrent(value);
			this.consistent = !this.instantiatedVariables.peek().isCurrentDomainEmpty();
			if(this.consistent){
				this.unassignedVariables.add(this.instantiatedVariables.pop());
				// this.updatedCurrentDomain(this.unassignedVariables.get(0));
			}
			firstTimeAfterSolution = false;
			//			System.out.println("Found a solution");
			return;
		}

		for(PVariable xvar : unassignedVariables) {
			for(PVariable rvar : xvar.getPastfcVariable()) {
				if(unassignedVariables.contains(rvar)) {
					System.out.println("Reductions from an unassigned variable");
				}
			}
		}

		while (!consistent && valueIndex < this.unassignedVariables.get(0).getCurrentDomain().size()){
			if(this.unassignedVariables.get(0).getName().equals("Q1") && this.unassignedVariables.get(0).getCurrentDomain().get(valueIndex) == 5) {
				System.out.println("break");
			}
			int value = this.unassignedVariables.get(0).getCurrentDomain().get(valueIndex);
			//currentPath.push(new Pair<PVariable, Integer>(this.unassignedVariables.get(0), value));
			numOfnv ++;
			consistent = true;
			int h = 1;
			while (consistent && h < this.unassignedVariables.size()) {
				consistent = checkForward(this.unassignedVariables.get(0), value, this.unassignedVariables.get(h));
				h++;
			}

			if (!consistent) {
				this.unassignedVariables.get(0).removeFromCurrent(value);
				//currentPath.pop();
				this.undoReduction(this.unassignedVariables.get(0));
				valueIndex --;
			}else{

				/**
				 * If the check-forward is consistent, do AC2001 among future variables.
				 */

				ArrayList<PVariable> toAC2001 = new ArrayList<PVariable>();
				if(unassignedVariables.size() > 2){
					for(int i = 2; i < unassignedVariables.size(); i++){
						toAC2001.add(unassignedVariables.get(i));
					}
					this.consistent = ac2001(toAC2001);
				}
				if (!consistent) {
					this.unassignedVariables.get(0).removeFromCurrent(value);
					//currentPath.pop();
					this.undoReduction(this.unassignedVariables.get(0));
					valueIndex --;
				}
			}

			for(PVariable v : unassignedVariables){
				v.setReducted(false);
			}
			
			valueIndex ++;
		}//end of outer-while
		this.instantiatedVariables.push(this.unassignedVariables.remove(0));
	}

	public void fcmac2001unlabel(){

		if (this.instantiatedVariables.size()==1){
			if(!this.instantiatedVariables.peek().isCurrentDomainEmpty()){
				int value = this.instantiatedVariables.peek().getCurrentDomain().get(0);
				this.instantiatedVariables.peek().removeFromCurrent(value);
			}
			if(this.instantiatedVariables.peek().isCurrentDomainEmpty()){
				this.unassignedVariables.add(this.instantiatedVariables.pop());
			}else{
				for(PVariable v : unassignedVariables){
					v.resetEverything();
				}
				this.unassignedVariables.add(this.instantiatedVariables.pop());
				this.consistent = true;
			}
		}else{
			this.updatedCurrentDomain(this.instantiatedVariables.peek());
			this.unassignedVariables.add(this.instantiatedVariables.pop());
			this.undoReduction(this.instantiatedVariables.peek());
			int val = this.instantiatedVariables.peek().getCurrentDomain().get(0);
			this.instantiatedVariables.peek().removeFromCurrent(val);
			this.consistent = !this.instantiatedVariables.peek().getCurrentDomain().isEmpty();
			if(this.consistent) {
				this.unassignedVariables.add(this.instantiatedVariables.pop());
			}
		}
	}

	public boolean checkForward(PVariable vi, int viValue, PVariable vj){
		ArrayList<Integer> reduction = new ArrayList<Integer>();
		for (Integer vjvalue : vj.getCurrentDomain()) {
			if (!this.check(new Pair<PVariable, Integer>(vi, viValue), new Pair<PVariable, Integer>(vj, vjvalue))){
				reduction.add(vjvalue);
			}
		}
		if (!reduction.isEmpty()){
			for (Integer i : reduction){
				vj.removeFromCurrent(i);
			}
			vj.pushReduction(reduction);
			vj.setReducted(true);
			vi.addFutureFCVariable(vj);
			vj.addPastFCVariable(vi);
		}
		return !vj.isCurrentDomainEmpty();
	}

	public void undoReduction(PVariable vi){
		if(!vi.getFuturefcVariable().isEmpty()){
			for (PVariable vj : vi.getFuturefcVariable()){
				vj.addReductionsBack();
				Collections.sort(vj.getCurrentDomain());
				vj.removeFromPastFCVariable(vi);
			}
		}
		vi.resetFutureFCVariable();
	}

	public void updatedCurrentDomain(PVariable vi) {
		vi.resetCurrentDomain();
		int size = vi.getReductions().size();
		Stack<ArrayList<Integer>> temp = new Stack<ArrayList<Integer>>();
		for(int i = 0; i < size; i++){
			for (Integer value : vi.getReductions().peek()){
				vi.removeFromCurrent(value);
			}
			temp.push(vi.getReductions().pop());
		}
		for (int i = 0; i < size; i++){
			vi.pushReduction(temp.pop());
		}
	}

	public boolean ac2001(ArrayList<PVariable> listOfVariables){
		boolean done = true;
		originalPair = new ArrayList<Pair>();
		ArrayList<PConstraint> constraintsBetween = new ArrayList<PConstraint>();
		for(int i = 0; i < listOfVariables.size()-1; i++){
			for(int j = 1; j < listOfVariables.size(); j++){
				if(getConstraint(listOfVariables.get(i), listOfVariables.get(j))!=null){
					constraintsBetween.add(getConstraint(listOfVariables.get(i), listOfVariables.get(j)));
				}
			}
		}

		for (PConstraint c : constraintsBetween) {
			if (c.getArity()!=1){
				Pair<PVariable, PVariable> newL = new Pair<PVariable, PVariable>(c.getScope()[0], c.getScope()[1], c, 'P');
				Pair<PVariable, PVariable> newR = new Pair<PVariable, PVariable>(c.getScope()[1], c.getScope()[0], c, 'N');
				currentPair.add(newL);
				currentPair.add(newR);
				originalPair.add(newL);
				originalPair.add(newR);
			}
		}
		while (!currentPair.isEmpty()) {
			Pair<PVariable, PVariable> current = currentPair.get(0);
			currentPair.remove(current);
			if (revise2001(current.getLeft(), current.getRight(), current.getConstraint(), current.getFlag())) {
				for (Pair p : originalPair) {
					if (p.getRight().equals(current.getLeft()) && !currentPair.contains(p)) {
						currentPair.add(p);
					}
				}
			}

			for (PVariable v : listOfVariables) {
				if (v.getCurrentDomain().isEmpty()) {
					return false;
				}
			}

		}
		return done;
	}

	public boolean revise2001(PVariable vi, PVariable vj, PConstraint c, char flag) {
		boolean revised = false;

		List<Integer> temp = new ArrayList<Integer>();
		for (Integer j : vi.getCurrentDomain()) {
			temp.add(j);
		}

		for (Integer viValue : temp) {

			boolean found = supported(vi, vj, viValue, c, flag);

			if (!found) {
				revised = true;
				vi.addFromRevise(viValue);
				vi.removeFromCurrent(viValue);
				if(!vi.isReducted()){
					unassignedVariables.get(0).addFutureFCVariable(vi);
					vi.addPastFCVariable(unassignedVariables.get(0));
				}
				vi.setReducted(true);
			}
		}
		return revised;
	}

	public boolean supported(PVariable vi, PVariable vj, Integer viValue, PConstraint c, char flag) {
		boolean supported = false;

		List<Integer> temp = new ArrayList<Integer>();
		for (Integer j : vj.getCurrentDomain()) {
			temp.add(j);
		}

		String last = vj.getLastSupported(new Pair<PVariable, Integer>(vi, viValue));
		Integer b;
		if(last != null){
			b = Integer.parseInt(last);
		}else{
			b = null;
		}

		if(!vj.getCurrentDomain().contains(b)){
			if(!vj.isCurrentDomainEmpty()){
			b = vj.getCurrentDomain().get(0);
			}
			
			while (b != null && !this.check(vi, vj, viValue, b, c, flag) ){
				if(vj.getCurrentDomain().indexOf(b) + 1 < vj.getCurrentDomain().size()){
					b = vj.getCurrentDomain().get(vj.getCurrentDomain().indexOf(b)+1);
				}else{
					b = null;
				}
			}
			if(b != null){
				vj.setLastSupported(new Pair<PVariable, Integer>(vi, viValue), b);
				return true;
			}
		}else{
			return true;
		}
		return supported;
	}

	public boolean check(PVariable vi, PVariable vj, Integer viValue, Integer vjValue, PConstraint c, char flag){
		if (this.getConstraintBetweenVariables(vi, vj) == null) {
			return true;
		}

		if (vi.getNeighbors().containsValue(vj)){
			numOfcc++;
			if (flag == 'P'){
				return (c.computeCostOf(new int[]{viValue.intValue(), vjValue.intValue()}) == 0 ? true : false);
			}else {
				return (c.computeCostOf(new int[]{vjValue.intValue(), viValue.intValue()}) == 0 ? true : false);
			}
		} else {
			return true;
		}

	}
	public void printVariables() {
		for (PVariable v : variables) {
			System.out.println(v);
		}
	}

	public void printCurrentPath() {
		for (Pair<PVariable,Integer> p : currentPath) {
			System.out.println(p.getLeft().getName() + " with value " + p.getRight());
		}
	}

	public int[] retrieveSolution() {
		return this.solution;
	}
	public void checkNodeConsistency() {
		//long start = getCpuTime();
		for (PConstraint c : this.constraints) {
			if (c.getArity() == 1) {
				this.numOfcc ++;
				List<Integer> temp = new ArrayList<Integer>();

				for (Integer j : c.getScope()[0].getInitialDomain()) {
					temp.add(j);
				}

				for (Integer i : temp) {
					if (c.computeCostOf(new int[]{i.intValue(), i.intValue()}) == 1) {
						c.getScope()[0].removeFromInitial(i);
					}
				}
			}
		}
		//this.cpu += (this.getCpuTime()-start);
	}
	public PConstraint getConstraint(PVariable vi, PVariable vj) {
		Iterator i = vi.getNeighbors().keySet().iterator();
		while (i.hasNext()){
			PConstraint key = (PConstraint) i.next();
			if (vi.getNeighbors().get(key).equals(vj)){
				return key;
			}
		}
		return null;
	}

	public ArrayList<PConstraint> getConstraintBetweenVariables(PVariable vi, PVariable vj) {
		//Iterator i = vi.getNeighbors().keySet().iterator();
		ArrayList<PConstraint> constraintsBetweenTwo = new ArrayList<PConstraint>();
		for (PConstraint c : vi.getConstraints()) {
			for (PVariable v : c.getScope()) {
				if (v == vj) {
					constraintsBetweenTwo.add(c);
				}
			}
		}
		return constraintsBetweenTwo;

		/*		while (i.hasNext()){
			PConstraint key = (PConstraint) i.next();
			if (vi.getNeighbors().get(key).equals(vj)){
				return key;
			}
		}
		return null;*/
	}

	public boolean check(Pair<PVariable, Integer> vi, Pair<PVariable, Integer> vj){
		if (this.getConstraintBetweenVariables(vi.getLeft(), vj.getLeft()) == null) {
			return true;
		}else {
			for (PConstraint c : getConstraintBetweenVariables(vi.getLeft(), vj.getLeft())){
				numOfcc++;
				if (c.getScope()[0].equals(vi.getLeft()) && c.getScope()[1].equals(vj.getLeft())){
					if (c.computeCostOf(new int[]{vi.getRight().intValue(), vj.getRight().intValue()}) == 1) {
						return false;
					}
				}else if (c.getScope()[1].equals(vi.getLeft()) && c.getScope()[0].equals(vj.getLeft())){
					if (c.computeCostOf(new int[]{vj.getRight().intValue(), vi.getRight().intValue()}) == 1) {
						return false;
					}
				}else {
					System.out.println("Check function error!");
				}
			}
			return true;
		}

		/*		if (vi.getNeighbors().containsValue(vj)){
			numOfcc ++;
				return (c.computeCostOf(new int[]{viValue.intValue(), vjValue.intValue()}) == 0 ? true : false);
		} else {
			return true;
		}*/
	}

	public void sortUnassigned(){
		if(this.strategy.equalsIgnoreCase("-udld")){
			Collections.sort(this.unassignedVariables, new SortByLeastDomain());
		}else if(this.strategy.equalsIgnoreCase("-uddeg")){
			Collections.sort(this.unassignedVariables, new SortByDegree());
		}else if(this.strategy.equalsIgnoreCase("-uddd")){
			Collections.sort(this.unassignedVariables, new SortByDomainDegree());
		}else if (this.strategy.equalsIgnoreCase("-udlx")){
			Collections.sort(unassignedVariables, new SortByName());
		}
	}

	/*	*//**
	 * Sort variables depending on the given arguments.
	 * Displaying is only for debugging purpose. 
	 * @param method
	 *//*
	public void sortVariables(String method) {
		if (method.equalsIgnoreCase("-ulx")){
			Arrays.sort(variables, new SortByName());
			variableOrderingHeuristic = "id-var-st : lexicographical ordering";
		}else if (method.equalsIgnoreCase("-uld")){
			Arrays.sort(variables, new SortByLeastDomain());
			variableOrderingHeuristic = "ldvar : least-domain variable-ordering heuristic";
		}else if (method.equalsIgnoreCase("-udeg")){
			Arrays.sort(variables, new SortByDegree());
			variableOrderingHeuristic = "deg-var-st : degree variable-ordering heuristic";
		}else if (method.equalsIgnoreCase("-udd")){
			Arrays.sort(variables, new SortByDomainDegree());
			variableOrderingHeuristic = "ddr-var-st : domain-degree ratio variable-orderin heuristic";
		}

				System.out.println("===================Ordered Variable========================");
		for (PVariable v : variables) {
			System.out.println(v.getName());
		}
	}*/

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
	public void printStatus() {
		System.out.println("Current Path: ------------------------------");
		this.printCurrentPath();
		for (PVariable v : variables){
			if(this.instantiatedVariables.contains(v)) {
				System.out.print("+");
			} else {
				System.out.print("-");
			}
			String values = "";
			for (Integer i : v.getCurrentDomain()){
				values += " " + i;
			}
			System.out.println(v.getName() + "current-domain:" + values);
		}
		System.out.println("---------------------------------");

	}
	
	/**
	 * Everything named with "pre" is for pre-processing
	 * 
	 */
	public boolean precheck(PVariable vi, PVariable vj, Integer viValue, Integer vjValue, PConstraint c, char flag){
		if (this.pregetConstraintBetweenVariables(vi, vj) == null) {
			return true;
		}

		if (vi.getNeighbors().containsValue(vj)){
			numOfcc ++;
			if (flag == 'P'){
				return (c.computeCostOf(new int[]{viValue.intValue(), vjValue.intValue()}) == 0 ? true : false);
			}else {
				return (c.computeCostOf(new int[]{vjValue.intValue(), viValue.intValue()}) == 0 ? true : false);
			}
		} else {
			return true;
		}

	}

	public boolean presupported(PVariable vi, PVariable vj, Integer viValue, PConstraint c, char flag) {
		boolean supported = false;
		List<Integer> temp = new ArrayList<Integer>();
		for (Integer j : vj.getInitialDomain()) {
			temp.add(j);
		}

		String last = vj.getLastSupported(new Pair<PVariable, Integer>(vi, viValue));
		Integer b;
		if(last != null){
			b = Integer.parseInt(last);
		}else{
			b = null;
		}

		if(!vj.getInitialDomain().contains(b)){
			b = vj.getInitialDomain().get(0);
			while (b != null && !this.precheck(vi, vj, viValue, b, c, flag) ){
				if(vj.getInitialDomain().indexOf(b) + 1 < vj.getInitialDomain().size()){
					b = vj.getInitialDomain().get(vj.getInitialDomain().indexOf(b)+1);
				}else{
					b = null;
				}
			}
			if(b != null){
				vj.setLastSupported(new Pair<PVariable, Integer>(vi, viValue), b);
				return true;
			}
		}else{
			return true;
		}
		return supported;
	}

	public boolean prerevise2001(PVariable vi, PVariable vj, PConstraint c, char flag) {
		boolean revised = false;

		List<Integer> temp = new ArrayList<Integer>();
		for (Integer j : vi.getInitialDomain()) {
			temp.add(j);
		}

		for (Integer viValue : temp) {

			boolean found = presupported(vi, vj, viValue, c, flag);

			if (!found) {
				revised = true;
				vi.removeFromInitial(viValue);
			}
		}
		return revised;
	}

	public boolean preac2001() {

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

		while (!currentPair.isEmpty()) {
			Pair<PVariable, PVariable> current = currentPair.get(0);
			currentPair.remove(current);
			if (prerevise2001(current.getLeft(), current.getRight(), current.getConstraint(), current.getFlag())) {
				for (Pair p : originalPair) {
					if (p.getRight().equals(current.getLeft()) && !currentPair.contains(p)) {
						currentPair.add(p);
					}
				}
			}

			for (PVariable v : variables) {
				if (v.getInitialDomain().isEmpty()) {
					preProcessingTime = getCpuTime() - start;
					return false;
				}
			}
		}
		preProcessingTime = getCpuTime() - start;


		return done;
	}

	public PConstraint pregetConstraintBetweenVariables(PVariable vi, PVariable vj) {
		Iterator i = vi.getNeighbors().keySet().iterator();
		while (i.hasNext()){
			PConstraint key = (PConstraint) i.next();
			if (vi.getNeighbors().get(key).equals(vj)){
				return key;
			}
		}
		return null;
	}
	
	/**
	 * Uses POI java API to write results to a spreadsheet.
	 * @throws IOException
	 * @throws InvalidFormatException
	 */

/*		public void writeToExcel() throws IOException, InvalidFormatException {
		File inputFile = new File("FCMAC2001-results.xls");
		if (!inputFile.exists()){
			Workbook wb = new HSSFWorkbook();
		    Sheet sheet1 = wb.createSheet("Results1");
		    FileOutputStream fileOut = new FileOutputStream("FCMAC2001-results.xls");
		    wb.write(fileOut);
		    fileOut.close();
		}
		InputStream inp = new FileInputStream(inputFile);
		Workbook wb = WorkbookFactory.create(inp);
		Sheet sheet = wb.getSheetAt(0);
		Row row = sheet.createRow(sheet.getLastRowNum()+1);
		row.createCell(0).setCellValue(fileName);
		row.createCell(1).setCellValue("FCMAC2001");
		row.createCell(2).setCellValue(strategy);
		row.createCell(3).setCellValue(firstNumOfcc);
		row.createCell(4).setCellValue(firstNumOfnv);
		row.createCell(5).setCellValue(firstNumOfbt);
		row.createCell(6).setCellValue(firstSolutionTime);
		row.createCell(7).setCellValue(numOfcc);
		row.createCell(8).setCellValue(numOfnv);
		row.createCell(9).setCellValue(numOfbt);
		row.createCell(10).setCellValue(allSolutionsTime);
		row.createCell(11).setCellValue(numOfSolutions);
		FileOutputStream fileOut = new FileOutputStream(inputFile);
		wb.write(fileOut);
		fileOut.close();
	}*/


}

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
import java.util.List;
import java.util.Stack;

/*import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;*/

import abscon.instance.components.PConstraint;
import abscon.instance.components.PVariable;

public class DynamicFCCBJ {

	private PVariable[] variables;
	private PConstraint[] constraints;
	private int firstNumOfcc;
	private int firstNumOfnv;
	private int firstNumOfbt;
	private int numOfcc;
	private int numOfnv;
	private int numOfbt;
	private String status;
	private boolean consistent;
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
	private String strategy;
	private ArrayList<PVariable> unassignedVariables;
	private Stack<PVariable> instantiatedVariables;
	private String fileName;

	/*
	 * Constructor
	 */
	public DynamicFCCBJ(PVariable[] variables, PConstraint[] constraints, String orderStrategy, String fileName) {
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
		this.consistent = false;
		this.solution = new int[variables.length];
		this.firstTimeAfterSolution = false;
		this.firstSolution = true;
		this.fileName = fileName;
		if(orderStrategy.equalsIgnoreCase("-uddd")){
			this.variableOrderingHeuristic = "domain degree domain";
		}else if(orderStrategy.equalsIgnoreCase("-uddeg")){
			this.variableOrderingHeuristic = "degree domain";
		}else if(orderStrategy.equalsIgnoreCase("-udld")){
			this.variableOrderingHeuristic = "least domain";
		}
		else if(orderStrategy.equalsIgnoreCase("-udlx")){
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
		for(PVariable v : variables){
			this.unassignedVariables.add(v);
		}
		for (PVariable var : variables) {
			var.setCbf(0);
		}
	}

	public String startDynamicFCCBJ() throws IOException{//, InvalidFormatException {
		this.checkNodeConsistency();
		for (PVariable v : variables) {
			v.resetCurrentDomain();
		}
		long startTime = getCpuTime();
		this.sortUnassigned();
		String status = bcssp();
		if (status.equalsIgnoreCase("Solution") || numOfSolutions > 0) {

		}else{
			System.out.println("There is no solution.");
		}

		long endTime = getCpuTime();
		allSolutionsTime = (endTime - startTime)/1000000;
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
				fclabel();
//				System.out.println("After fclabel");
//				this.printStatus();
			}else {
				fcunlabel();
				numOfbt++;
//				System.out.println("After fcunlabel");
//				this.printStatus();
//				this.sortUnassigned();
				//this.checkNodeConsistency();
			}

			if (consistent && this.unassignedVariables.isEmpty()) {
				for (PVariable i : instantiatedVariables) {
					i.setCbf(1);
				}
				for(PVariable u : unassignedVariables){
					u.setCbf(0);
				}
				numOfSolutions ++;
				long endTime = getCpuTime();
				firstSolutionTime = (endTime-startTime)/1000000;
				firstNumOfcc = numOfcc;
				firstNumOfnv = numOfnv;
				firstNumOfbt = numOfbt;
				//status = "solution";
				if (firstSolution) {
//					printStatus();
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
				//status = "solution";
//				printStatus();
				//System.out.println("===========================================");
				firstTimeAfterSolution = true;
			}else if (this.instantiatedVariables.isEmpty() && numOfSolutions > 0 && this.unassignedVariables.get(unassignedVariables.size()-1).isCurrentDomainEmpty()) {
				//numOfSolutions++;
				status = "solution";
			}else if (this.instantiatedVariables.size() == 1 && this.instantiatedVariables.get(0).isCurrentDomainEmpty()){
				status = "impossible";
			}
		}

		return status;
	}

	public void fclabel(){
		this.consistent = false;
		int valueIndex = 0;
		if (numOfSolutions > 0 && firstTimeAfterSolution){
			int value = this.instantiatedVariables.peek().getCurrentDomain().get(0);
			this.instantiatedVariables.peek().removeFromCurrent(value);
			this.consistent = !this.instantiatedVariables.peek().isCurrentDomainEmpty();
			if(this.consistent){
				this.unassignedVariables.add(this.instantiatedVariables.pop());
				// this.updatedCurrentDomain(this.unassignedVariables.get(0));
			}
			firstTimeAfterSolution = false;
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
				if(h != 1){
					for(PVariable i : unassignedVariables.get(h-1).getPastfcVariable()){
						unassignedVariables.get(0).pushConfVar(i);
					}
				}
				valueIndex --;
			}
			valueIndex ++;
		}//end of outer-while
//		System.out.println("Label: " + unassignedVariables.get(0));
		this.instantiatedVariables.push(this.unassignedVariables.remove(0));
	}

	public void fcunlabel(){
		int h = 0;
		/*
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
		*/
			if(instantiatedVariables.peek().getCbf() == 1){
				h = instantiatedVariables.size()-2;
				instantiatedVariables.peek().setCbf(0);
			}else{
				if(!unassignedVariables.isEmpty()){
					if(!instantiatedVariables.get(instantiatedVariables.size()-1).getPastfcVariable().isEmpty() || !instantiatedVariables.get(instantiatedVariables.size()-1).getConfVar().isEmpty()){
						h = getMaxConfPast(instantiatedVariables.get(instantiatedVariables.size()-1).getConfVar(), instantiatedVariables.get(instantiatedVariables.size()-1).getPastfcVariable());
					}
				}
			}

			if(!unassignedVariables.isEmpty()){
				for (PVariable i : instantiatedVariables.peek().getConfVar()){
					instantiatedVariables.get(h).pushConfVar(i);
				}
				for (PVariable i : instantiatedVariables.peek().getPastfcVariable()){
					instantiatedVariables.get(h).pushConfVar(i);
				}
			}

			instantiatedVariables.get(h).removeConfVar(instantiatedVariables.get(h));
			/*
			for (int j = h+1; j < instantiatedVariables.size(); j++){
				instantiatedVariables.get(j).resetConfSet();
				undoReduction(instantiatedVariables.get(j));
				updatedCurrentDomain(instantiatedVariables.get(j));
			}
			*/

			// for (int j = h + 1; j < instantiatedVariables.size(); j++){
			while(instantiatedVariables.size() - 1 > h) {
				instantiatedVariables.peek().resetConfSet();
				undoReduction(instantiatedVariables.peek());
				updatedCurrentDomain(instantiatedVariables.peek());
				unassignedVariables.add(instantiatedVariables.pop());
			}
			
			// unassignedVariables.add(instantiatedVariables.pop());
			/*
			if(h > instantiatedVariables.size()-1){
				undoReduction(instantiatedVariables.peek());
				if(!instantiatedVariables.peek().isCurrentDomainEmpty()){
					instantiatedVariables.peek().removeFromCurrent(instantiatedVariables.peek().getCurrentDomain().get(0));
				}
			}else{
				undoReduction(instantiatedVariables.get(h));
				if(!instantiatedVariables.get(h).isCurrentDomainEmpty()){
					instantiatedVariables.get(h).removeFromCurrent(instantiatedVariables.get(h).getCurrentDomain().get(0));
				}
			}
			*/
			undoReduction(instantiatedVariables.get(h));
			if(!instantiatedVariables.get(h).isCurrentDomainEmpty()){
				instantiatedVariables.get(h).removeFromCurrent(instantiatedVariables.get(h).getCurrentDomain().get(0));
			}
			
			/*this.updatedCurrentDomain(this.instantiatedVariables.peek());
			this.unassignedVariables.add(this.instantiatedVariables.pop());
			this.undoReduction(this.instantiatedVariables.peek());
			int val = this.instantiatedVariables.peek().getCurrentDomain().get(0);
			this.instantiatedVariables.peek().removeFromCurrent(val);*/
			
			this.consistent = !this.instantiatedVariables.peek().getCurrentDomain().isEmpty();
			if(this.consistent) {
				this.unassignedVariables.add(this.instantiatedVariables.pop());
			}
		// }
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
			vi.addFutureFCVariable(vj);
			vj.addPastFCVariable(vi);
			// vj.pushConfVar(vi);
			//			if(instantiatedVariables.contains(vi)){
			//				vj.pushNew(instantiatedVariables.indexOf(vi));
			//			}
		}
		return !vj.isCurrentDomainEmpty();
	}

	public void undoReduction(PVariable vi){
		if(!vi.getFuturefcVariable().isEmpty()){
			for (PVariable vj : vi.getFuturefcVariable()){
				vj.addReductionsBack();
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

	public void printVariables() {
		for (PVariable v : variables) {
			System.out.println(v);
		}
	}

//	public void printCurrentPath() {
//		for (Pair<PVariable,Integer> p : currentPath) {
//			System.out.println(p.getLeft().getName() + " with value " + p.getRight());
//		}
//	}

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
		}else if(this.strategy.equalsIgnoreCase("-udlx")){
			Collections.sort(unassignedVariables, new SortByName());
		}
	}

	/**
	 * Sort variables depending on the given arguments.
	 * Displaying is only for debugging purpose. 
	 * @param method
	 */
	public void sortVariables(String method) {
		if (method.equalsIgnoreCase("-udlx")){
			Arrays.sort(variables, new SortByName());
			variableOrderingHeuristic = "id-var-dyn : lexicographical ordering";
		}else if (method.equalsIgnoreCase("-udld")){
			Arrays.sort(variables, new SortByLeastDomain());
			variableOrderingHeuristic = "ldvar : least-domain variable-ordering heuristic";
		}else if (method.equalsIgnoreCase("-uddeg")){
			Arrays.sort(variables, new SortByDegree());
			variableOrderingHeuristic = "deg-var-dyn : degree variable-ordering heuristic";
		}else if (method.equalsIgnoreCase("-uddd")){
			Arrays.sort(variables, new SortByDomainDegree());
			variableOrderingHeuristic = "ddr-var-dyn : domain-degree ratio variable-orderin heuristic";
		}

		/*		System.out.println("===================Ordered Variable========================");
		for (PVariable v : variables) {
			System.out.println(v.getName());
		}*/
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

	public Integer getMaxConfPast(List<PVariable> list, List<PVariable> list2){
		Integer max = -1;
		for (PVariable conf : list){
			if(instantiatedVariables.indexOf(conf) > max) {
				max = instantiatedVariables.indexOf(conf);
			}
		}
		for (PVariable past : list2){
			if(instantiatedVariables.indexOf(past) > max){
				max = instantiatedVariables.indexOf(past);
			}
		}
		if(max == -1){
			max = instantiatedVariables.size()-1;
		}
		return max;
	}

	public void printStatus() {
		for (PVariable v : variables){
			if(this.instantiatedVariables.contains(v)) {
				System.out.print("+ ");
			} else {
				System.out.print("- ");
			}
			String values = "";
			for (Integer i : v.getCurrentDomain()){
				values += " " + i;
			}
			System.out.printf("%-13s %-15s %n ",v.getName() , values);
		}
		System.out.println("---------------------------------");

	}

	/**
	 * Uses POI java API to write results to a spreadsheet.
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
/*		public void writeToExcel() throws IOException, InvalidFormatException {
		File inputFile = new File("DynamicFCCBJ-results.xls");
		if (!inputFile.exists()){
			Workbook wb = new HSSFWorkbook();
		    Sheet sheet1 = wb.createSheet("Results1");
		    FileOutputStream fileOut = new FileOutputStream("DynamicFCCBJ-results.xls");
		    wb.write(fileOut);
		    fileOut.close();
		}
		InputStream inp = new FileInputStream(inputFile);
		Workbook wb = WorkbookFactory.create(inp);
		Sheet sheet = wb.getSheetAt(0);
		Row row = sheet.createRow(sheet.getLastRowNum()+1);
		row.createCell(0).setCellValue(fileName);
		row.createCell(1).setCellValue("dynamic-FCCBJ");
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

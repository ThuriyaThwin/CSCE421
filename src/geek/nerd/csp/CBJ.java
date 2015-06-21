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
import java.util.List;
import java.util.Stack;
/*
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;*/

import abscon.instance.components.PConstraint;
import abscon.instance.components.PVariable;
import abscon.instance.tools.InstanceParser;

public class CBJ {

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
	private int currentVariableIndex;
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
	private int[] cbf;

	public CBJ(PVariable[] variables, PConstraint[] constraints) {
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
		this.currentVariableIndex = 0;
		this.currentPath = new Stack<Pair<PVariable,Integer>>();
		this.solution = new int[variables.length];
		this.firstTimeAfterSolution = false;
		this.firstSolution = true;
		this.variableOrderingHeuristic = null;
		this.varStaticDynamic = "static";
		this.valStaticDynamic = "static";
		this.valueOrderingHeuristic = "not applicable";
		this.firstSolutionTime = 0;
		this.allSolutionsTime = 0;
		this.cbf = new int[variables.length];
		for (int i = 0; i < cbf.length; i ++) {
			cbf[i] = 0;
		}
	}

	public String startCBJ() throws IOException{//, InvalidFormatException {
		this.checkNodeConsistency();
		for (PVariable v : variables) {
			v.resetCurrentDomain();
		}
		long startTime = getCpuTime();
		String status = bcssp();
		if (status.equalsIgnoreCase("Solution") || numOfSolutions > 0) {
			int i = 0;
			for (Pair<PVariable, Integer> p : currentPath) {
				solution[i] = p.getRight().intValue();
				i++;
			}
		}else{
			System.out.println("There is no solution.");
		}
		
		long endTime = getCpuTime();
		allSolutionsTime = (endTime - startTime)/1000000;
		//writeToExcel();
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
		return status;
	}

	public String bcssp() {
		long startTime = getCpuTime();
		consistent = true;
		status = "unknown";
		currentVariableIndex = 0;
		while (status.equalsIgnoreCase("unknown")) {
			if (consistent) {
				currentVariableIndex = cbjlabel(currentVariableIndex);
/*				System.out.println("After Label: " );
				printCurrentPath();
				printConfSet();
				System.out.println("----------------------------------------");*/
			}else {
				currentVariableIndex = cbjunlabel(currentVariableIndex);
/*				System.out.println("After unlabel: ");
				printCurrentPath();
				printConfSet();
				System.out.println("----------------------------------------");*/
				numOfbt++;
			}

			if (currentVariableIndex > numOfVariables -1) {
/*				printCurrentPath();
				printConfSet();*/
				numOfSolutions ++;
				/*for (int i = 0; i < currentVariableIndex; i++){
					if (i!=currentVariableIndex-1) {
					variables[currentVariableIndex-1].pushNew(i);
					}
				}
				for (PVariable v : variables) {
					v.printConfSet();
				}*/
				for (int i = 0; i < cbf.length; i ++) {
					cbf[i] = 1;
				}
				long endTime = getCpuTime();
				firstSolutionTime = (endTime-startTime)/1000000;
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
					firstSolution = false;
				}
				currentVariableIndex = numOfVariables - 1;
				firstTimeAfterSolution = true;
			}else if (currentVariableIndex < 0 && numOfSolutions > 0) {
				status = "solution";
			}else if (currentVariableIndex < 0){
				status = "impossible";
			}
		}
		
		return status;
	}

	public int cbjlabel(int currentVariableIndex) {
		consistent = false;
		int valueIndex = 0;

		if (numOfSolutions > 0 && firstTimeAfterSolution){
			currentPath.peek().getLeft().removeFromCurrent(currentPath.peek().getRight());
			currentPath.pop();
			firstTimeAfterSolution = false;
		}
		
		while (!consistent && valueIndex < variables[currentVariableIndex].getCurrentDomain().size()){
			int value = variables[currentVariableIndex].getCurrentDomain().get(valueIndex);
			currentPath.push(new Pair<PVariable, Integer>(variables[currentVariableIndex], value));
			numOfnv ++;
			consistent = true;
			int h = 0;
			while (consistent && h < currentVariableIndex) {
				consistent = check(new Pair<PVariable, Integer>(variables[currentVariableIndex], value), currentPath.get(h));
				h++;
			}//end of inner-while

			if (!consistent) {
				if (currentVariableIndex != h-1){
					variables[currentVariableIndex].pushNew(h-1);
				}
				variables[currentVariableIndex].removeFromCurrent(value);
				currentPath.pop();
				valueIndex --;
			}
			valueIndex ++;
		}//end of outer-while

		if (consistent){
			return currentVariableIndex+1;
		}else {
			return currentVariableIndex;
		}
		//return 0;
	}

	public int cbjunlabel(int currentVariableIndex) {
		int h = 0;
		if (currentVariableIndex == 0) {
			return -1;
		}
		if (cbf[currentVariableIndex] == 1){
			h = currentVariableIndex - 1 ;
			cbf[currentVariableIndex] = 0;
		}else {
			h = variables[currentVariableIndex].maxConfSet();
		}
		
		for (Integer i : variables[currentVariableIndex].getConfSet()){
			variables[h].pushNew(i);
		}
		variables[h].removeConf(h);
		
/*		System.out.println("Inside unlabel: ");
		printCurrentPath();*/
		for (int j = h+1; j <= currentVariableIndex; j++) {
			variables[j].resetConfSet();
			variables[j].resetCurrentDomain();
		}
		for (int j = h+1; j< currentVariableIndex; j++) {
			currentPath.pop();
		}
/*		System.out.println("After reset: ");
		printCurrentPath();
		printConfSet();*/
		variables[h].removeFromCurrent(currentPath.pop().getRight());
		consistent = !variables[h].isCurrentDomainEmpty();
		return h;
	}

	public void printCurrentPath() {
		for (Pair<PVariable,Integer> p : currentPath) {
			System.out.println(p.getLeft().getName() + " with value " + p.getRight());
		}
	}

	public void printConfSet() {
		for (PVariable v : variables) {
			v.printConfSet();
		}
	}
	public int[] retrieveSolution() {
		return this.solution;
	}
	public void checkNodeConsistency() {
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
	
	/**
	 * Sort variables depending on the given arguments.
	 * Displaying is only for debugging purpose. 
	 * @param method
	 */
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
/*
	*//**
	 * Uses POI java API to write results to a spreadsheet.
	 * @throws IOException
	 * @throws InvalidFormatException
	 *//*
	public void writeToExcel() throws IOException, InvalidFormatException {
		File inputFile = new File("Xia-HW3-results.xls");
		if (!inputFile.exists()){
			Workbook wb = new HSSFWorkbook();
		    Sheet sheet1 = wb.createSheet("Results1");
		    FileOutputStream fileOut = new FileOutputStream("Xia-HW3-results.xls");
		    wb.write(fileOut);
		    fileOut.close();
		}
		InputStream inp = new FileInputStream(inputFile);
		Workbook wb = WorkbookFactory.create(inp);
		Sheet sheet = wb.getSheetAt(0);
		Row row = sheet.createRow(sheet.getLastRowNum()+1);
		row.createCell(0).setCellValue(fileName);
		row.createCell(1).setCellValue("CBJ");
		row.createCell(2).setCellValue(variableOrderingHeuristic);
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

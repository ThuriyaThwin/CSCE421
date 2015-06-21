package geek.nerd.csp;

import geek.nerd.variablecomparators.SortByDegree;
import geek.nerd.variablecomparators.SortByDomainDegree;
import geek.nerd.variablecomparators.SortByLeastDomain;
import geek.nerd.variablecomparators.SortByName;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import abscon.instance.components.PConstraint;
import abscon.instance.components.PVariable;
/*import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;*/

public class FCCBJ {

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
	private int width;
	private String orderStrategy;

	public FCCBJ(PVariable[] variables, PConstraint[] constraints, String strategy) {
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
		this.width = 0;
		this.orderStrategy = strategy;
		for (int i = 0; i < cbf.length; i ++) {
			cbf[i] = 0;
		}
		for (PVariable variable : variables) {
			variable.setNeighborVariables();
		}
	}

	public String startFCCBJ() throws IOException{//, InvalidFormatException {
		this.checkNodeConsistency();
		for (PVariable v : variables) {
			v.resetCurrentDomain();
		}
		long startTime = getCpuTime();
		String status = bcssp();
		if (status.equalsIgnoreCase("Solution") || numOfSolutions > 0) {
			//System.out.println("Number of solution found: " + numOfSolutions);
			int i = 0;
			for (Pair<PVariable, Integer> p : currentPath) {
				solution[i] = p.getRight().intValue();
				i++;
			}
			//System.out.println("===========================================");
			//printCurrentPath();
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
		if(orderStrategy.equalsIgnoreCase("-uw")){
			System.out.printf("%-30s %-15s %n","minimal width of the graph :" , width);
		}
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
		currentVariableIndex = 0;
		while (status.equalsIgnoreCase("unknown")) {
			if (consistent) {
				currentVariableIndex = fccbjlabel(currentVariableIndex);
//				System.out.println("After fccjblabel");
//				this.printStatus();
			}else {
				currentVariableIndex = fccbjunlabel(currentVariableIndex);
				numOfbt++;
//				System.out.println("After fccbjunlabel");
//				this.printStatus();
				//this.checkNodeConsistency();
			}
			if (currentVariableIndex > numOfVariables -1) {
				for (int i = 0; i < cbf.length; i ++) {
					cbf[i] = 1;
				}
				numOfSolutions ++;
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
					//printCurrentPath();
					//printVariables();
					System.out.println("===========================================");
					firstSolution = false;
				}
				//status = "solution";
				currentVariableIndex = numOfVariables - 1;
				//System.out.println("===========================================");
				firstTimeAfterSolution = true;
			}else if (currentVariableIndex < 0 && numOfSolutions > 0) {
				//numOfSolutions++;
				status = "solution";
			}else if (currentVariableIndex < 0){
				status = "impossible";
			}
		}

		return status;
	}

	public int fccbjlabel(int vi){
		this.consistent = false;
		int valueIndex = 0;

		if (numOfSolutions > 0 && firstTimeAfterSolution){
			currentPath.peek().getLeft().removeFromCurrent(currentPath.peek().getRight());
			//System.out.println(currentPath.peek().getLeft().getName() + " value " + currentPath.peek().getRight());
			currentPath.pop();
			firstTimeAfterSolution = false;
		}

		while (!consistent && valueIndex < variables[vi].getCurrentDomain().size()){
			int value = variables[vi].getCurrentDomain().get(valueIndex);
			currentPath.push(new Pair<PVariable, Integer>(variables[vi], value));
			numOfnv ++;
			consistent = true;
			int h = vi+1;
			//System.out.println(currentPath.get(h).getLeft().getName());
			while (consistent && h < variables.length) {
				consistent = checkForward(vi, value, h);
				h++;
			}//end of inner-while
			if (!consistent) {
				variables[vi].removeFromCurrent(value);
				currentPath.pop();
				this.undoReduction(vi);
				if (vi != h-1){
					for(Integer i : variables[h-1].getPastFC()){
						variables[vi].pushNew(i);	
					}
				}
				valueIndex --;
			}
			valueIndex ++;
		}//end of outer-while

		if (consistent){
			return vi+1;
		}else {
			return vi;
		}
	}

	public int fccbjunlabel(int vi){
		int h = 0;
		
		if (vi == 0){
			return -1;
		}

		if (cbf[vi] == 1){
			h = vi - 1 ;
			cbf[vi] = 0;
		}else {
			if(!variables[vi].getPastFC().isEmpty() || !variables[vi].getConfSet().isEmpty()){
				h = variables[vi].maxConfSetPastFC();
			}
		}

		for (Integer i : variables[vi].getConfSet()){
			variables[h].pushNew(i);
		}
		for (Integer i : variables[vi].getPastFC()){
			variables[h].pushNew(i);
		}

		variables[h].removeConf(h);
		for (int j = vi; j >= h+1; j --){
			variables[j].resetConfSet();
			undoReduction(j);
			updatedCurrentDomain(j);
		}
		for (int j = h + 1; j < vi; j++){
			currentPath.pop();
		}
		undoReduction(h);
		variables[h].removeFromCurrent(currentPath.pop().getRight());
		consistent = !variables[h].getCurrentDomain().isEmpty();
		return h;

	}

	public boolean checkForward(int vi, int viValue, int vj){
		ArrayList<Integer> reduction = new ArrayList<Integer>();
		for (Integer vjvalue : variables[vj].getCurrentDomain()) {
			if (!this.check(new Pair<PVariable, Integer>(variables[vi], viValue), new Pair<PVariable, Integer>(variables[vj], vjvalue))){
				reduction.add(vjvalue);
			}
		}
		if (!reduction.isEmpty()){
			for (Integer i : reduction){
				variables[vj].removeFromCurrent(i);
			}
			variables[vj].pushReduction(reduction);
			variables[vi].addFutureFC(vj);
			variables[vj].addPastFC(vi);
			variables[vj].pushNew(vi);
		}
		return !variables[vj].isCurrentDomainEmpty();
	}

	public void undoReduction(int vi){
		if(!variables[vi].getFutureFC().isEmpty()){
			for (Integer vj : variables[vi].getFutureFC()){
				variables[vj].addReductionsBack();
				variables[vj].removeFromPastFC(vi);
			}
		}
		variables[vi].resetFutureFC();
	}

	public void updatedCurrentDomain(int vi) {
		variables[vi].resetCurrentDomain();
		int size = variables[vi].getReductions().size();
		Stack<ArrayList<Integer>> temp = new Stack<ArrayList<Integer>>();
		for(int i = 0; i < size; i++){
			for (Integer value : variables[vi].getReductions().peek()){
				variables[vi].removeFromCurrent(value);
			}
			temp.push(variables[vi].getReductions().pop());
		}
		for (int i = 0; i < size; i++){
			variables[vi].pushReduction(temp.pop());
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
		}else if (method.equalsIgnoreCase("-uw")){
			this.width();
			variableOrderingHeuristic = "Minimal Width Ordering";
		}

		/*		System.out.println("===================Ordered Variable========================");
		for (PVariable v : variables) {
			System.out.println(v.getName());
		}*/
	}

	/**
	 * Width ordering of variables
	 */
	private void width() {
		List<PVariable> widthGraph = new ArrayList<PVariable>();
		for(PVariable v : variables){
			widthGraph.add(v);
		}
		Stack<PVariable> elimination = new Stack<PVariable>();
		for (PVariable v : widthGraph){
			if (v.getNeighborVariables().isEmpty()){
				elimination.push(v);

			}
		}
		for(PVariable v : elimination){
			if(widthGraph.contains(v)){
				widthGraph.remove(v);
			}
		}
		while(!widthGraph.isEmpty()){
			width = width + 1;
			boolean done = false;
			while(!done){
				done = true;
				for (PVariable v : widthGraph){
					if (v.getWidthNeighbors().size() <= width){
						for(PVariable n : v.getWidthNeighbors()){
							n.removeWidthneighbor(v);
						}
						elimination.push(v);
						done = false;
					}
				}
				if(!done){
					for(PVariable v : elimination){
						if(widthGraph.contains(v)){
							widthGraph.remove(v);
						}
					}
				}
			}
		}
		for(int i = 0; i < variables.length; i ++){
			variables[i] = elimination.pop();
		}
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
	public void printStatus() {
		System.out.println("Current Path: ------------------------------");
		this.printCurrentPath();
		for (PVariable v : variables){
			String values = "";
			for (Integer i : v.getCurrentDomain()){
				values += " " + i;
			}
			System.out.println(v.getName() + "current-domain:" + values);
		}
		System.out.println("---------------------------------");

	}
	/**
	 * Uses POI java API to write results to a spreadsheet.
	 * @throws IOException
	 * @throws InvalidFormatException
	 */

	/*	public void writeToExcel() throws IOException, InvalidFormatException {
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
		row.createCell(1).setCellValue("BT");
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

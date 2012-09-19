package abscon.instance.components;

public abstract class PConstraint {
	protected String name;

	protected char flag;
	
	protected PVariable[] scope;

	public String getName() {
		return name;
	}

	public PVariable[] getScope() {
		return scope;
	}

	public int getArity() {
		return scope.length;
	}

	public PConstraint(PConstraint c, char flag){
		this.name = c.getName();
		this.scope = c.getScope();
		this.flag = flag;
	}
	
	public PConstraint(String name, PVariable[] scope) {
		this.name = name;
		this.scope = scope;
	}

	public int getMaximalCost() {
		return 1;
	}

	/**
	 * For CSP, returns 0 if the constraint is satisfied and 1 if the constraint is violated. <br>
	 * For WCSP, returns the cost for the given tuple.
	 */
	public abstract long computeCostOf(int[] tuple);

	public String toString() {
		String s = "  Constraint " + name + ", scope = ";
		s += scope[0].getName();
		for (int i = 1; i < scope.length; i++)
			s += " " + scope[i].getName();
		return s;
	}

	public boolean isGuaranteedToBeDivisionByZeroFree() {
		return true;
	}

	public boolean isGuaranteedToBeOverflowFree() {
		return true;
	}
}

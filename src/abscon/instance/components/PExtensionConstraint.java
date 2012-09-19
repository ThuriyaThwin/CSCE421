package abscon.instance.components;


public class PExtensionConstraint extends PConstraint {

	private PRelation relation;

	public PRelation getRelation() {
		return relation;
	}

	public PExtensionConstraint(String name, PVariable[] scope, PRelation relation) {
		super(name, scope);
		this.relation = relation;
	}

	public int getMaximalCost() {
		return relation.getMaximalCost();
	}
	
	public long computeCostOf(int[] tuple) {
		return relation.computeCostOf(tuple);
	}

	@Override
	public String toString() {
		int displayLimit = 10;
		String output = super.toString() + ". Type: Extension "+ relation.semantics+"\n \t Tuples defined: ";
		for (int i = 0; i < Math.min(displayLimit, relation.nbTuples); i++) {
			output += "(";
			for (int j = 0; j < relation.arity; j++)
				output += (relation.tuples[i][j] + (j < relation.arity - 1 ? "," : ""));
			output += ") ";
		}
		
		return output+"\n";
	}
}

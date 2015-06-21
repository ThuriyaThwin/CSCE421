package geek.nerd.csp;

import abscon.instance.components.PConstraint;

public class Pair<L,R> {

	private final L left;
	private final R right;
	private char flag;
	private PConstraint constraint;

	public Pair(L left, R right, PConstraint constraint, char flag) {
		this.left = left;
		this.right = right;
		this.flag = flag;
		this.constraint = constraint;
	}
	
	public Pair(L left, R right) {
		this.left = left;
		this.right = right;
	}

	public PConstraint getConstraint() {
		return constraint;
	}

	public L getLeft() { 
		return left; 
	}
	  
	public R getRight() {
		return right; 
	}

	public char getFlag() {
		return this.flag;
	}
	
	@Override
	public int hashCode() { 
		return left.hashCode() ^ right.hashCode(); 
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (!(o instanceof Pair)) return false;
		Pair Pairo = (Pair) o;
		return this.left.equals(Pairo.getLeft()) && this.right.equals(Pairo.getRight());
	}
	
	public String toString(){
		return ""+left + right;
	}

}

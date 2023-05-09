package MainPack;


import java.util.HashMap;

public class Generator {
	
	private int n, d;
	private double p1, p2;

	/*
	 * constructor parameters -
	 * n, number of agents
	 * d, domain size
	 * p1, probability that two agents are constrainted
	 * p2, probability that a constraint takes the value "false"
	 */
	public Generator(int n, int d, double p1, double p2) {
		this.n = n;
		this.d = d;
		this.p1 = p1;
		this.p2 = p2;
	}

	// genetate csp
	public CSP generateDCSP() {
		
		HashMap<VarTuple, ConsTable> cons_tables = new HashMap<VarTuple, ConsTable>();
		
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				if (Math.random() < p1) {
					VarTuple at = new VarTuple(i, j);
					ConsTable ct = new ConsTable(d, p2);
					cons_tables.put(at, ct);
				}
			}
		}
		
		return new CSP(cons_tables, d, n);
	}
}

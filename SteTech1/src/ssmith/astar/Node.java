package ssmith.astar;

import ssmith.lang.GeometryFuncs;

/**
 * @author stephen smith
 *
 */
public class Node {

	private Node parent;
	public int x, z;
	private double heuristic;
	private float dist_from_start;

	public Node(int x, int z) {
		super();
		this.x = x;
		this.z = z;
	}

	public void setHeuristic(Node prnt, int targ_x, int targ_z, float dist) {
		if (prnt != null) {
			this.parent = prnt;
			this.dist_from_start = prnt.dist_from_start + dist;
		}
		double dist_to_target = GeometryFuncs.distance(x, z, targ_x, targ_z);

		heuristic = this.dist_from_start + dist_to_target;
	}

	public Node getParent() {
		return this.parent;
	}

	public double getHeuristic() {
		return this.heuristic;
	}

	public double getDistFromStart() {
		return this.dist_from_start;
	}

}

package ssmith.astar;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * @author stephen smith
 *
 */
public class PriorityList extends LinkedList<Node> {
	
	private static final long serialVersionUID = 1L;

	public PriorityList() {
	}
	
	public boolean add(Node n) {
		if (this.size() == 0) {
			super.add(n);
			return true;
		}
		
		ListIterator<Node> it = this.listIterator();
		Node node;
		int count = 0;
		while (it.hasNext()) {
			node = (Node)it.next();
			if (n.getHeuristic() < node.getHeuristic()) {
				super.add(count, n);
				return true;
			}
			count++;
		}
		// Got this far so it must be last.
		super.addLast(n);
		return true;
	}
	
	public void printList() {
		System.out.println("List: " + this.size());

		ListIterator<Node> it = this.listIterator();
		Node node;
		while (it.hasNext()) {
			node = (Node)it.next();
			System.out.println("Dist="+node.getHeuristic() + " ("+node.x + ","+node.z + ")");
		}
	}

}

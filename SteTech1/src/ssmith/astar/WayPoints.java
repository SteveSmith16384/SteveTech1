package ssmith.astar;

import java.awt.Point;
import java.util.ArrayList;

import ssmith.lang.GeometryFuncs;
import ssmith.lang.NumberFunctions;

/**
 * @author stephen smith
 *
 */
public class WayPoints extends ArrayList<Point> {

	private static final long serialVersionUID = 1L;

	public WayPoints() {
		super();
	}

	public void insertRoute(int pos, WayPoints w) {
		this.addAll(pos, w);
	}

	public void truncate(int amt) {
		while (this.size() > amt) {
			this.remove(this.size()-1);
		}
	}

	public void remove(int x, int y) {
		for (int i=0 ; i<this.size() ; i++) {
			Point p = get(i);
			if (p.x == x && p.y == y) {
				this.remove(p);
			}
		}
	}

	public boolean contains(int x, int y) {
		for (int i=0 ; i<this.size() ; i++) {
			Point p = get(i);
			if (p.x == x && p.y == y) {
				return true;
			}
		}
		return false;
	}

	public Point getClosestPoint(int x, int y) {
		double closest_dist = Double.MAX_VALUE;
		int closest_point = -1;
		for(int p=0 ; p<this.size() ; p++) {
			Point pnt = this.get(p);
			double dist = GeometryFuncs.distance(x, y, pnt.x, pnt.y);
			if (dist < closest_dist) {
				closest_dist = dist;
				closest_point = p;
			}
		}
		return (Point)this.get(closest_point);
	}

	public Point getNextPoint() {
		if (this.hasAnotherPoint()) {
			return get(0);
		} else {
			return null;
		}
	}

	public Point getLastPoint() {
		return get(size()-1);
	}

	public Point getPenultimatePoint() {
		if (this.size() >= 2) {
			return get(size()-2);
		} else {
			return null;
		}
	}

	public void removeCurrentPoint() {
		remove(0);
	}

	public boolean hasAnotherPoint() {
		return this.size() > 0;
	}

	public Point getRandomPoint() {
		return this.getRandomPoint(false);
	}

	public Point getRandomPoint(boolean remove) {
		if (this.hasAnotherPoint()) {
			int no = NumberFunctions.rnd(0, size()-1);
			Point p = get(no); 
			if (remove) {
				this.remove(no);
			}
			return p;
		}
		return null;
	}

	public void add(int x, int y) {
		add(new Point(x, y));
	}

	public void clear() {
		removeAll(this);
	}

	public WayPoints copy() {
		WayPoints w = new WayPoints();
		for (int i=0 ; i<this.size() ; i++) {
			Point p = get(i);
			w.add(p);
		}
		return w;
	}

}

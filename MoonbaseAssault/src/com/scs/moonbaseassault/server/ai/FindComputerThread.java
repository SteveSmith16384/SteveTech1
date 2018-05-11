package com.scs.moonbaseassault.server.ai;

import java.awt.Point;

import com.jme3.math.Vector3f;
import com.scs.moonbaseassault.components.IUnit;
import com.scs.moonbaseassault.server.MoonbaseAssaultServer;

import ssmith.astar.AStar;
import ssmith.astar.WayPoints;

public class FindComputerThread extends Thread {

	private MoonbaseAssaultServer game;
	private IUnit unit;
	public WayPoints route = null;

	public FindComputerThread(MoonbaseAssaultServer _game, IUnit _unit) {
		super("FindComputerThread")	;
		
		this.setDaemon(true);

		game = _game;
		unit = _unit;
	}


	public void run() {
		synchronized (game) { // to ensure they are checked one by one
			int closest_dist = 9999;
			AStar astar = new AStar(game);
			for (Point p : game.getComputerSquares()) {
				Vector3f pos = unit.getPhysicalEntity().getWorldTranslation();
				astar.findPath((int)pos.x, (int)pos.z, p.x, p.y, closest_dist, false);
				if (astar.wasSuccessful()) {
					if (astar.getRoute().size() < closest_dist) {
						closest_dist = astar.getRoute().size();
						route = astar.getRoute();
						break;
					}
				}
			}
		}
	}

}
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

		game = _game;
		unit = _unit;
	}


	public void run() {
		synchronized (game) { // to ensure they are checked one by one
			int closest_dist = 9999;
			AStar astar = new AStar(game);
			for (Point p : game.getComputerSquares()) {
				//MapSquare sq = game.mapdata.getMapSq_MaybeNULL(p.x, p.y);
				//if (sq.major_type == MapDataTable.MT_COMPUTER && sq.isDestroyed() == false) {
				// todo re-add  if (isComputerUnique(sq)) { // Check it hasn't been selected by another unit
				//if (unit.ai.isAIControlled(game)) {
				Vector3f pos = unit.getPhysicalEntity().getWorldTranslation();
				astar.findPath((int)pos.x, (int)pos.y, p.x, p.y, closest_dist, false);
				if (astar.wasSuccessful()) {
					if (astar.getRoute().size() < closest_dist) {
						closest_dist = astar.getRoute().size();
						route = astar.getRoute();
					}
				}
				/*} else {
							if (unit.canSee(sq)) {
								route = new WayPoints();
								route.add(p); // get computer for us to shoot at
							}
						}*/
				//}
				//}
			}
		}
	}

	/* todo
	private boolean isComputerUnique(MapSquare sq) {
		for (IUnit comrade_unit : game.units) {
			if (comrade_unit.getStatus() == UnitStatus.ST_DEPLOYED) {
				if (comrade_unit.getSide() == this.unit.getSide()) {
					if (comrade_unit != this.unit) {
						if (comrade_unit.ai.getTargetMapSquare() == sq) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}
	 */
}
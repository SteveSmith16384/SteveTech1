package com.scs.stevetech1.systems;

import java.util.LinkedList;

import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.entities.Entity;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public class EntityRemovalSystem extends AbstractSystem {

	private LinkedList<Integer> entitiesToRemove = new LinkedList<Integer>(); // Have a special list so we don't have to loop through ALL entities
	private IEntityController entityController;

	public EntityRemovalSystem(IEntityController _entityController) {
		entityController = _entityController;
	}


	public void markEntityForRemoval(IEntity e) {
		if (e != null) {
			e.markForRemoval();
			entitiesToRemove.add(e.getID());
		}
	}


	public int getNumEntities() {
		return entitiesToRemove.size();
	}


	public void actuallyRemoveEntities() {
		// Remove entities
		while (this.entitiesToRemove.size() > 0) { // Do it this way since removing some entities my cause more entities to be added to this list, e.g. avatar's weapons
			int id = this.entitiesToRemove.getFirst();
			entityController.actuallyRemoveEntity(id);
			this.entitiesToRemove.removeFirst();
		}
		entitiesToRemove.clear();
	}


}

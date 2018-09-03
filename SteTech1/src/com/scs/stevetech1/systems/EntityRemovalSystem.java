package com.scs.stevetech1.systems;

import java.util.LinkedList;

import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.entities.Entity;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public class EntityRemovalSystem extends AbstractSystem {

	private LinkedList<Integer> entitiesToRemove = new LinkedList<Integer>(); // Still have a list so we don't have to loop through ALL entities
	private IEntityController entityController;
	
	public EntityRemovalSystem(IEntityController _entityController) {
		entityController = _entityController;
	}
	
	
	public void markEntityForRemoval(int id) {
		entitiesToRemove.add(id);
		if (Globals.STRICT) {
			if (entityController instanceof AbstractGameServer) {
				AbstractGameServer s = (AbstractGameServer)this.entityController;
				Entity e = (Entity)s.entities.get(id);
				if (e != null && !e.markedForRemoval) {
					throw new RuntimeException("Todo");
				}
			} else if (entityController instanceof AbstractGameClient) {
				AbstractGameClient s = (AbstractGameClient)this.entityController;
				Entity e = (Entity)s.entities.get(id);
				if (e != null && !e.markedForRemoval) {
					throw new RuntimeException("Todo");
				}
			}
		}
	}

	
	public int getNumEntities() {
		return entitiesToRemove.size();
	}
	
	
	public void actuallyRemoveEntities() {
		// Remove entities
		while (this.entitiesToRemove.size() > 0) { // Do it this way since removing some entities my cause more entities to be added to this list, e.g. avatar's weapons
		//for(int id : entitiesToRemove) {
			int id = this.entitiesToRemove.getFirst();
			entityController.actuallyRemoveEntity(id);
			this.entitiesToRemove.removeFirst();
		}
		entitiesToRemove.clear();
	}
	
	
}

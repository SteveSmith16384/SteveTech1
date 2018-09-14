package com.scs.stevetech1.entities;

import java.io.IOException;
import java.util.HashMap;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public abstract class Entity implements IEntity, Savable {

	protected int gameId;
	public final int id;
	public final int type;
	protected transient IEntityController game;
	public final String entityName;
	private boolean requiresProcessing;
	
	public boolean removed = false;
	protected boolean markedForRemoval = false;

	// Server-only vars
	protected HashMap<String, Object> creationData;

	public Entity(IEntityController _module, int _id, int _type, String _name, boolean _requiresProcessing) {
		super();

		id = _id;
		type = _type;
		game = _module;
		entityName = _name;
		requiresProcessing = _requiresProcessing;


		gameId = game.getGameID();
	}


	@Override
	public String toString() {
		return "Entity_" + entityName + "_" + id;
	}


	/**
	 * Do not call this directly!  Should only be called by the game controller.  This method is for cleaing itself up
	 */
	public void remove() {
		if (Globals.STRICT) {
			if (!markedForRemoval) {
				throw new RuntimeException(this.entityName + " not marked for removal!");
			}
		}
		if (!removed) {
			//game.removeEntity(this.id);
			removed = true;
		}
	}


	@Override
	public void write(JmeExporter ex) throws IOException {

	}


	@Override
	public void read(JmeImporter im) throws IOException {

	}


	@Override
	public int getID() {
		return id;
	}


	@Override
	public int getType() {
		return type;
	}


	@Override
	public String getName() {
		return entityName;
	}


	public HashMap<String, Object> getCreationData() {
		return creationData;
	}


	@Override
	public int getGameID() {
		return gameId;
	}


	@Override
	public boolean requiresProcessing() {
		return requiresProcessing;
	}


	@Override
	public void markForRemoval() {
		this.markedForRemoval = true;
	}
	
	
	@Override
	public boolean isMarkedForRemoval() {
		return this.markedForRemoval;
	}

}

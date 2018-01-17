package com.scs.stevetech1.entities;

import java.io.IOException;
import java.util.HashMap;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.shared.IEntityController;

public abstract class Entity implements IEntity, Savable {
	
	public final int id;
	public final int type;
	protected transient IEntityController game;
	public final String name;

	// Server-only vars
	protected HashMap<String, Object> creationData;

	public Entity(IEntityController _module, int _id, int _type, String _name) {
		id = _id;
		type = _type;
		game = _module;
		name = _name;
	}


	@Override
	public String toString() {
		return "E_" + name + "_" + id;
	}


	public void remove() {
		game.removeEntity(this.id);
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
		return name;
	}


	public HashMap<String, Object> getCreationData() {
		return creationData;
	}


}

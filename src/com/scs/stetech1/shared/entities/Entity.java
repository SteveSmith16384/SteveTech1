package com.scs.stetech1.shared.entities;

import java.io.IOException;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;
import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.shared.IEntityController;

public abstract class Entity implements IEntity, Savable {
	
	public final int id;
	public final int type;
	protected transient IEntityController game;
	public final String name;

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

}

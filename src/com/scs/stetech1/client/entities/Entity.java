package com.scs.stetech1.client.entities;

import java.io.IOException;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;
import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.shared.IEntityController;

public class Entity implements IEntity, Savable {
	
	private static int nextId = 0;
	
	public final int id;
	protected IEntityController module;
	//protected GameModule module;
	public String name;

	public Entity(IEntityController _game, String _name) {
		id = nextId++;
		module = _game;
		//module = _module;
		name = _name;
	}


	@Override
	public String toString() {
		return "E_" + name + "_" + id;
	}


	public void remove() {
		module.removeEntity(this);
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

}

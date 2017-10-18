package com.scs.stetech1.client.entities;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;
import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.shared.IEntityController;

public class Entity implements IEntity, Savable {
	
	private static AtomicInteger nextID = new AtomicInteger();
	
	public int id;
	public final int type;
	protected IEntityController module;
	public String name;

	public Entity(IEntityController _module, int _type, String _name) {
		id = nextID.addAndGet(1);
		type = _type;
		module = _module;
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


	@Override
	public int getType() {
		return type;
	}

}

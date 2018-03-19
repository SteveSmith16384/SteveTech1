package com.scs.stevetech1.shared;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import com.jme3.app.SimpleApplication;
import com.scs.simplephysics.ICollisionListener;
import com.scs.simplephysics.SimplePhysicsController;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.ILaunchable;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.Globals;

import ssmith.util.FixedLoopTime;
import ssmith.util.RealtimeInterval;

/*
 * This class is shared by both the abstract client and abstract server.
 * 
 *//*
public abstract class AbstractGameController extends SimpleApplication implements ICollisionListener<PhysicalEntity>{ // todo - remove this class

	// ----------
	protected static AtomicInteger nextEntityID = new AtomicInteger(1);

	public HashMap<Integer, IEntity> entities = new HashMap<>(100);
	protected LinkedList<IEntity> entitiesToAdd = new LinkedList<IEntity>();
	protected LinkedList<Integer> entitiesToRemove = new LinkedList<Integer>();

	protected SimplePhysicsController<PhysicalEntity> physicsController; // Checks all collisions
	protected FixedLoopTime loopTimer;  // Keep client and server running at the same time
	
	public int tickrateMillis, clientRenderDelayMillis, timeoutMillis;
	// ----------

	public AbstractGameController(int _tickrateMillis, int _clientRenderDelayMillis, int _timeoutMillis) {
		super();
		
		tickrateMillis = _tickrateMillis;
		clientRenderDelayMillis = _clientRenderDelayMillis;
		timeoutMillis = _timeoutMillis;
		
		loopTimer = new FixedLoopTime(tickrateMillis);
	}

}

*/
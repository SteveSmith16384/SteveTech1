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

public abstract class AbstractGameController extends SimpleApplication implements ICollisionListener<PhysicalEntity>{

	protected static AtomicInteger nextEntityID = new AtomicInteger(1);

	public HashMap<Integer, IEntity> entities = new HashMap<>(100);
	//protected LinkedList<IEntity> toAdd = new LinkedList<IEntity>();
	protected HashMap<IEntity, Long> toAdd = new HashMap<IEntity, Long>();  // Entity::TimeToAdd
	protected HashMap<Integer, Long> toRemove = new HashMap<Integer, Long>();  // EntityID::TimeToRemove

	protected SimplePhysicsController<PhysicalEntity> physicsController; // Checks all collisions
	protected FixedLoopTime loopTimer = new FixedLoopTime(Globals.SERVER_TICKRATE_MS);
	protected RealtimeInterval sendPingInterval = new RealtimeInterval(Globals.PING_INTERVAL_MS);

	public AbstractGameController() {
		super();
	}

	
	@Override
	public boolean canCollide(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b) {
		PhysicalEntity pa = a.userObject;
		PhysicalEntity pb = b.userObject;

		if (!pa.collideable || !pb.collideable) {
			return false;
		}

		if (pa instanceof AbstractAvatar && pb instanceof AbstractAvatar) {
			// Avatars on the same side don't collide
			AbstractAvatar aa = (AbstractAvatar)pa;
			AbstractAvatar ab = (AbstractAvatar)pb;
			if (aa.side == ab.side) {
				return false;
			}
		}
		
		// Prevent bullets getting hit by the shooter
		if (pa instanceof ILaunchable) {
			ILaunchable aa = (ILaunchable)pa;
			if (aa.getLauncher() == pb) {
				return false;
			}
		}
		if (pb instanceof ILaunchable) {
			ILaunchable ab = (ILaunchable)pb;
			if (ab.getLauncher() == pa) {
				return false;
			}
		}

		if (pa instanceof ILaunchable && pb instanceof ILaunchable) {
			// Bullets don't collide with each other
			return false;
		}

		return true;
	}

}

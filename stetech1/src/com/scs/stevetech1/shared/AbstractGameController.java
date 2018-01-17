package com.scs.stevetech1.shared;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import com.jme3.app.SimpleApplication;
import com.scs.simplephysics.ICollisionListener;
import com.scs.simplephysics.SimplePhysicsController;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.Globals;
import com.scs.testgame.entities.Grenade;

import ssmith.util.FixedLoopTime;
import ssmith.util.RealtimeInterval;

public abstract class AbstractGameController extends SimpleApplication implements ICollisionListener<PhysicalEntity>{

	protected static AtomicInteger nextEntityID = new AtomicInteger(1);

	public HashMap<Integer, IEntity> entities = new HashMap<>(100);
	protected LinkedList<IEntity> toAdd = new LinkedList<IEntity>();
	protected LinkedList<Integer> toRemove = new LinkedList<Integer>(); 

	protected SimplePhysicsController<PhysicalEntity> physicsController; // Checks all collisions
	protected FixedLoopTime loopTimer = new FixedLoopTime(Globals.SERVER_TICKRATE_MS);
	protected RealtimeInterval sendPingInterval = new RealtimeInterval(Globals.PING_INTERVAL_MS);

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

		if (pa instanceof Grenade && pb instanceof AbstractAvatar) {
			Grenade aa = (Grenade)pa;
			AbstractAvatar ab = (AbstractAvatar)pb;
			if (aa.shooter == ab) {
				return false;
			}
		}
		if (pb instanceof Grenade && pa instanceof AbstractAvatar) {
			Grenade ab = (Grenade)pb;
			AbstractAvatar aa = (AbstractAvatar)pa;
			if (ab.shooter == aa) {
				return false;
			}
		}

		return true;
	}

}

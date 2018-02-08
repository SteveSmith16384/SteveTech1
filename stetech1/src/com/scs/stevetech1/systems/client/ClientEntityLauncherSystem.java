package com.scs.stevetech1.systems.client;

import java.util.HashMap;
import java.util.Iterator;

import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.ILaunchable;
import com.scs.stevetech1.netmessages.EntityLaunchedMessage;

/*
 * This is for launching bullets/grenades etc... caused by other players shooting
 */
public class ClientEntityLauncherSystem {

	private HashMap<ILaunchable, LaunchData> toLaunch = new HashMap<ILaunchable, LaunchData>();  // Entity::LaunchData
	private AbstractGameClient client;

	public ClientEntityLauncherSystem(AbstractGameClient _client) {
		super();

		client = _client;
	}


	public void scheduleLaunch(EntityLaunchedMessage elm) {
		ILaunchable l = (ILaunchable)client.entities.get(elm.entityID);
		if (l == null || l.hasBeenLaunched()) {
			/*
			 * It's probably our own entity that we've already manually launched.
			 */
			return;
		}
		if (l == null) {
			throw new RuntimeException("Launchable id " + elm.entityID + " not found");
		}
		this.toLaunch.put(l, elm.launchData);

	}


	public void process(long renderTime) {
		// Launch any launchables
		Iterator<ILaunchable> it3 = this.toLaunch.keySet().iterator();
		while (it3.hasNext()) {
			ILaunchable e = it3.next();
			LaunchData ld = this.toLaunch.get(e);
			it3.remove();
			IEntity shooter = client.entities.get(ld.shooterId);
			e.launch(shooter, ld.startPos, ld.dir);
		}

	}


}

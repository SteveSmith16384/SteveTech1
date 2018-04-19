package com.scs.stevetech1.systems.client;

import java.util.HashMap;
import java.util.Iterator;

import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IPlayerLaunchable;
import com.scs.stevetech1.netmessages.EntityLaunchedMessage;

/*
 * This is for launching bullets/grenades etc... caused by other players shooting
 */
public class ClientEntityLauncherSystem {

	private HashMap<IPlayerLaunchable, LaunchData> toLaunch = new HashMap<IPlayerLaunchable, LaunchData>();  // Entity::LaunchData
	private IClientApp client;

	public ClientEntityLauncherSystem(IClientApp _client) {
		super();

		client = _client;
	}


	public void scheduleLaunch(EntityLaunchedMessage elm) {
		IPlayerLaunchable l = (IPlayerLaunchable)client.getEntity(elm.entityID);
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
		Iterator<IPlayerLaunchable> it3 = this.toLaunch.keySet().iterator();
		while (it3.hasNext()) {
			IPlayerLaunchable e = it3.next();
			LaunchData ld = this.toLaunch.get(e);
			it3.remove();
			IEntity shooter = client.getEntity(ld.shooterId);
			e.launch(shooter, ld.startPos, ld.dir);
		}

	}


}

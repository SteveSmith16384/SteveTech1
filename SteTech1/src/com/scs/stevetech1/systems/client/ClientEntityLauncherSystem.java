package com.scs.stevetech1.systems.client;

/*
 * This is for launching bullets/grenades etc... caused by other players shooting
 */
/*
public class ClientEntityLauncherSystem {

	private HashMap<ILaunchable, LaunchData> toLaunch = new HashMap<ILaunchable, LaunchData>();  // Entity::LaunchData
	private IClientApp client;

	public ClientEntityLauncherSystem(IClientApp _client) {
		super();

		client = _client;
	}


	public void scheduleLaunch(EntityLaunchedMessage elm) {
		ILaunchable l = (ILaunchable)client.getEntity(elm.entityID);
		if (l == null || l.hasBeenLaunched()) {
			 // It's probably our own entity that we've already manually launched.
			return;
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
			IEntity shooter = client.getEntity(ld.shooterId);
			e.launch(shooter, ld.startPos, ld.dir);
		}

	}


}
*/
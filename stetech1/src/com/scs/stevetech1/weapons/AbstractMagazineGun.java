package com.scs.stevetech1.weapons;

import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.ICanShoot;
import com.scs.stevetech1.components.IEntityContainer;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractPlayersBullet;
import com.scs.stevetech1.netmessages.AbilityUpdateMessage;
import com.scs.stevetech1.server.AbstractEntityServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.AbstractAbility;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.shared.IEntityController;

public abstract class AbstractMagazineGun<T> extends AbstractAbility implements IAbility {

	protected float timeUntilShoot_secs = 0;
	protected int magazineSize;
	protected float shotInterval_secs, reloadInterval_secs;
	protected ClientData client; // Only used server-side

	public AbstractMagazineGun(IEntityController _game, int id, int type, int playerID, ICanShoot owner, int abilityNum, String _name, float shotInt, float reloadInt, int magSize, ClientData _client) { 
		super(_game, id, type, playerID, (AbstractAvatar)owner, abilityNum, _name);

		this.shotInterval_secs = shotInt;
		this.reloadInterval_secs = reloadInt;
		this.magazineSize = magSize;
		this.client = _client;
	}


	public abstract boolean launchBullet();

	protected abstract void createBullet(AbstractEntityServer server, int entityid, int playerID, IEntityContainer<AbstractPlayersBullet> owner, int side);

	public abstract int getBulletsInMag();

	@Override
	public final boolean activate() {
		if (this.timeUntilShoot_secs <= 0 && getBulletsInMag() > 0) {
			this.launchBullet();
			timeUntilShoot_secs = this.shotInterval_secs;
			return true;
		} else {
			if (getBulletsInMag() <= 0) {
				Globals.p("No bullets"); // Should never happen
			} else if (timeUntilShoot_secs > 0) {
				//Globals.p("Shooting too soon - wait for " + timeUntilShoot_secs + " secs");
			}
		}
		return false;
	}


	@Override
	public void processByServer(AbstractEntityServer server, float tpf_secs) {
		super.processByServer(server, tpf_secs);

		if (this.getBulletsInMag() <= 0) {
			// Reload
			//Globals.p("Reloading");
			reload(server);// Only server can reload
			this.timeUntilShoot_secs = this.reloadInterval_secs;
			server.gameNetworkServer.sendMessageToAll(new AbilityUpdateMessage(true, this));
		}
		timeUntilShoot_secs -= tpf_secs;
	}


	private void reload(AbstractEntityServer server) {
		IEntityContainer<AbstractPlayersBullet> irac = (IEntityContainer<AbstractPlayersBullet>)this;
		while (this.getBulletsInMag() < this.magazineSize) {
			createBullet(server, server.getNextEntityID(), playerID, irac, -1);
		}
	}


	@Override
	public void processByClient(IClientApp client, float tpf_secs) {
		timeUntilShoot_secs -= tpf_secs;
	}


	@Override
	public String getHudText() {
		if (this.getBulletsInMag() == this.magazineSize && this.timeUntilShoot_secs > shotInterval_secs) {
			return name + " RELOADING";
		} else {
			return name + " (" + this.getBulletsInMag() + "/" + this.magazineSize  +")";
		}
	}


	@Override
	public void encode(AbilityUpdateMessage aum) {
		aum.timeUntilShoot = timeUntilShoot_secs;

	}


	@Override
	public void decode(AbilityUpdateMessage aum) {
		timeUntilShoot_secs = aum.timeUntilShoot;
	}



}

package com.scs.stevetech1.weapons;

import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.ICanShoot;
import com.scs.stevetech1.components.IEntityContainer;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.netmessages.AbilityUpdateMessage;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.AbstractAbility;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.shared.IEntityController;

public abstract class AbstractMagazineGun<T> extends AbstractAbility implements IAbility {

	protected float timeUntilShoot_secs = 0;
	protected int magazineSize;
	protected float shotInterval_secs, reloadInterval_secs;


	public AbstractMagazineGun(IEntityController _game, int id, int type, ICanShoot owner, int num, String _name, float shotInt, float reloadInt, int magSize) { 
		super(_game, id, type, (AbstractAvatar)owner, num, _name);

		this.shotInterval_secs = shotInt;
		this.reloadInterval_secs = reloadInt;
		this.magazineSize = magSize;
		//this.bulletsLeftInMag = this.magazineSize;
	}


	public abstract boolean launchBullet();

	protected abstract void createBullet(AbstractGameServer server, int entityid, IEntityContainer owner, int side);

	public abstract int getBulletsInMag();

	@Override
	public final boolean activate() {
		if (this.timeUntilShoot_secs <= 0 && getBulletsInMag() > 0) {
			this.launchBullet();
			timeUntilShoot_secs = this.shotInterval_secs;
			//bulletsLeftInMag--;
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


	private void reload(AbstractGameServer server) {
		IEntityContainer<T> irac = (IEntityContainer)this;
		while (this.getBulletsInMag() < this.magazineSize) {
			createBullet(server, server.getNextEntityID(), irac, -1);
		}
	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		super.processByServer(server, tpf_secs);

		if (this.getBulletsInMag() <= 0) {
			// Reload
			Globals.p("Reloading");
			reload(server);// Only server can reload
			this.timeUntilShoot_secs = this.reloadInterval_secs;
			server.networkServer.sendMessageToAll(new AbilityUpdateMessage(true, this));
		}
		timeUntilShoot_secs -= tpf_secs;
	}


	@Override
	public void processByClient(AbstractGameClient client, float tpf_secs) {
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
		//aum.bulletsLeftInMag = bulletsLeftInMag;
		aum.timeUntilShoot = timeUntilShoot_secs;

	}


	@Override
	public void decode(AbilityUpdateMessage aum) {
		//this.bulletsLeftInMag = aum.bulletsLeftInMag;
		timeUntilShoot_secs = aum.timeUntilShoot;
	}



}

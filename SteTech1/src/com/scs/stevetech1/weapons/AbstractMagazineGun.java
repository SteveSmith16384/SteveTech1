package com.scs.stevetech1.weapons;

import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.ICanShoot;
import com.scs.stevetech1.components.IEntityContainer;
import com.scs.stevetech1.components.IReloadable;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractPlayersBullet;
import com.scs.stevetech1.netmessages.AbilityUpdateMessage;
import com.scs.stevetech1.netmessages.ClientGunReloadRequestMessage;
import com.scs.stevetech1.netmessages.GunReloadingMessage;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.AbstractAbility;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.shared.IEntityController;

public abstract class AbstractMagazineGun<T> extends AbstractAbility implements IAbility, IReloadable {

	protected float timeSinceLastReload = 0;
	protected float timeUntilShoot_secs = 0;
	protected int magazineSize;
	protected float shotInterval_secs, reloadInterval_secs;
	protected ClientData client; // Only used server-side
	private boolean toBeReloaded = true;

	public AbstractMagazineGun(IEntityController _game, int id, int type, int playerID, ICanShoot owner, int avatarID, int abilityNum, String _name, 
			float shotInt, float reloadInt, int magSize, ClientData _client) { 
		super(_game, id, type, playerID, (AbstractAvatar)owner, avatarID, abilityNum, _name);

		this.shotInterval_secs = shotInt;
		this.reloadInterval_secs = reloadInt;
		this.magazineSize = magSize;
		this.client = _client;
	}


	/**
	 * 
	 * @return whether the gun had a bullet to actually launch
	 */
	public abstract boolean launchBullet();

	protected abstract void createBullet(AbstractGameServer server, int entityid, int playerID, IEntityContainer<AbstractPlayersBullet> owner, int side);

	public abstract int getBulletsInMag();

	@Override
	public final boolean activate() {
		if (this.timeUntilShoot_secs <= 0 && getBulletsInMag() > 0) {
			this.launchBullet();
			timeUntilShoot_secs = this.shotInterval_secs;
			return true;
		} else {
			if (timeUntilShoot_secs > 0) {
				//Globals.p("Shooting too soon - wait for " + timeUntilShoot_secs + " secs");
			} else if (getBulletsInMag() <= 0) {
				if (game.isServer()) {
					// If activation failed, clear out bullets to force a reload, since they must be out of sync
					Globals.p("Forcing empty of magazine");
					this.emptyMagazine();
				} else {
					/*AbstractGameClient client = (AbstractGameClient) game;
					client.sendMessage(new ClientReloadingMessage(this.getID()));
					this.timeUntilShoot_secs = this.reloadInterval_secs;*/
				}
			}
		}
		return false;
	}

	protected abstract void emptyMagazine();


	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		super.processByServer(server, tpf_secs);


		if (toBeReloaded) {
			toBeReloaded = false;
			// Reload
			//Globals.p("Reloading");
			reload(server);// Only server can reload
			this.timeUntilShoot_secs = this.reloadInterval_secs;
			server.gameNetworkServer.sendMessageToAll(new AbilityUpdateMessage(true, this));
		}

		timeUntilShoot_secs -= tpf_secs;
		timeSinceLastReload += tpf_secs;
	}


	@Override
	public void reload(AbstractGameServer server) {
		if (timeSinceLastReload > 5) {
			server.gameNetworkServer.sendMessageToAll(new GunReloadingMessage(this, reloadInterval_secs)); // todo - only send to the owner
			Globals.p("Reloading " + this);
			this.emptyMagazine(); // Remove any existing bullets
			IEntityContainer<AbstractPlayersBullet> irac = (IEntityContainer<AbstractPlayersBullet>)this;
			while (this.getBulletsInMag() < this.magazineSize) {
				createBullet(server, server.getNextEntityID(), playerID, irac, this.owner.side);
			}
			this.timeUntilShoot_secs = this.reloadInterval_secs;
			timeSinceLastReload = 0;
			server.gameNetworkServer.sendMessageToAll(new AbilityUpdateMessage(true, this)); // todo - only send to the owner
		}
	}


	@Override
	public void processByClient(IClientApp client, float tpf_secs) {
		super.processByClient(client, tpf_secs);

		timeUntilShoot_secs -= tpf_secs;

		//if (client.getGameData().isInGame()) { // Reload even if waiting for players
		if (getBulletsInMag() <= 0 && timeUntilShoot_secs <= 0) {
			//AbstractGameClient client = (AbstractGameClient) game;
			client.sendMessage(new ClientGunReloadRequestMessage(this.getID()));
			this.timeUntilShoot_secs = this.reloadInterval_secs;
			Globals.p("Sending ClientReloadingMessage");
		}
		//}
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


	@Override
	public void setToBeReloaded() {
		toBeReloaded = true;

	}



}

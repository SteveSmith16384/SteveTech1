package com.scs.stevetech1.weapons;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.ICanShoot;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IReloadable;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractBullet;
import com.scs.stevetech1.netmessages.AbilityUpdateMessage;
import com.scs.stevetech1.netmessages.ClientReloadRequestMessage;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.AbstractAbility;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.shared.IEntityController;

public abstract class AbstractMagazineGun extends AbstractAbility implements IAbility, IReloadable {

	protected float timeSinceLastReload = 0;
	protected float timeUntilShoot_secs = 0;
	protected int magazineSize;
	protected float reloadInterval_secs;
	protected ClientData client; // Only used server-side
	private boolean toBeReloaded = true;
	private int bulletsInMag;

	public AbstractMagazineGun(IEntityController _game, int id, int type, int playerID, ICanShoot owner, int avatarID, byte abilityNum, String _name, 
			float shotInt, float reloadInt, int magSize, ClientData _client) { 
		super(_game, id, type, playerID, (AbstractAvatar)owner, avatarID, abilityNum, _name, shotInt);

		this.shotInterval_secs = shotInt;
		this.reloadInterval_secs = reloadInt;
		this.magazineSize = magSize;
		this.client = _client;

		bulletsInMag = this.magazineSize;
	}


	protected boolean launchBullet() {
		if (bulletsInMag > 0) {
			ICanShoot ic = (ICanShoot)owner;
			AbstractBullet bullet = createBullet(game.getNextEntityID(), playerID, super.owner, ic.getBulletStartPos(), ic.getShootDir(), this.owner.side);
			game.addEntity(bullet);
			//todo game.playSound(MASounds.SFX_AI_BULLET_FIRED, owner.getID(), bullet.getWorldTranslation(), Globals.DEF_VOL, false);
			return true;
		}
		return false;
	}


	/**
	 * Only called either by a client when their own player shoots, or by the server.
	 * @param entityid
	 * @param playerID
	 * @param shooter
	 * @param startPos
	 * @param dir
	 * @param side
	 * @return
	 */
	protected abstract AbstractBullet createBullet(int entityid, int playerID, IEntity shooter, Vector3f startPos, Vector3f dir, byte side);

	private int getBulletsInMag() {
		return bulletsInMag;
	}


	@Override
	public final boolean activate() {
		if (this.timeUntilShoot_secs <= 0 && getBulletsInMag() > 0) {
			this.launchBullet();
			bulletsInMag--;
			timeUntilShoot_secs = this.shotInterval_secs;
			return true;
		}

		return false;
	}

	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		super.processByServer(server, tpf_secs);

		this.sharedProcess(tpf_secs);

		/*
		if (toBeReloaded) {
			toBeReloaded = false;
			// Reload
			//Globals.p("Reloading");
			reload(server);// Only server can reload
			this.timeUntilShoot_secs = this.reloadInterval_secs;
			server.sendMessageToInGameClients(new AbilityUpdateMessage(true, this));
		}

		timeUntilShoot_secs -= tpf_secs;
		timeSinceLastReload += tpf_secs;
		 */
	}


	@Override
	public void processByClient(IClientApp client, float tpf_secs) {
		super.processByClient(client, tpf_secs);

		this.sharedProcess(tpf_secs);
		/*
		timeUntilShoot_secs -= tpf_secs;

		if (getBulletsInMag() <= 0 && timeUntilShoot_secs <= 0) {
			client.sendMessage(new ClientGunReloadRequestMessage(this.getID())); // Auto-reload
			this.timeUntilShoot_secs = this.reloadInterval_secs;
			//Globals.p("Sending ClientReloadingMessage");
		}
		 */
	}


	private void sharedProcess(float tpf_secs) {
		timeUntilShoot_secs -= tpf_secs;
		timeSinceLastReload += tpf_secs;

		if (getBulletsInMag() <= 0 && timeUntilShoot_secs <= 0) {
			toBeReloaded = true;
		}

		if (toBeReloaded) {
			toBeReloaded = false;
			reload();
		}


	}


	private void reload() {
		if (timeSinceLastReload > 5) {
			Globals.p("Reloading " + this);
			this.bulletsInMag = this.magazineSize;
			this.timeUntilShoot_secs = this.reloadInterval_secs;
			if (game.isServer()) {
				AbstractGameServer server = (AbstractGameServer)game;
				//server.gameNetworkServer.sendMessageToClient(client, new GunReloadingMessage(this, reloadInterval_secs));
				server.gameNetworkServer.sendMessageToClient(client, new AbilityUpdateMessage(true, this));
			} else {
				AbstractGameClient client = (AbstractGameClient)game;
				if (client.povWeapon != null) {
					client.povWeapon.startReloading(reloadInterval_secs);
				}
			}
		}
	}


	@Override
	public String getHudText() {
		if (this.getBulletsInMag() == this.magazineSize && this.timeUntilShoot_secs > shotInterval_secs) {
			return entityName + " RELOADING";
		} else {
			return entityName + " (" + this.getBulletsInMag() + "/" + this.magazineSize  +")";
		}
	}


	@Override
	public void encode(AbilityUpdateMessage aum) {
		aum.bulletsLeftInMag = this.bulletsInMag;
		aum.timeUntilShoot = timeUntilShoot_secs;

	}


	@Override
	public void decode(AbilityUpdateMessage aum) {
		this.bulletsInMag = aum.bulletsLeftInMag;
		timeUntilShoot_secs = aum.timeUntilShoot;
	}


	@Override
	public void setToBeReloaded() {
		if (this.bulletsInMag < this.magazineSize) {
			toBeReloaded = true;
		}
	}

}

package com.scs.stevetech1.weapons;

import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.netmessages.AbilityUpdateMessage;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.AbstractAbility;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.shared.IEntityController;

public abstract class AbstractMagazineGun extends AbstractAbility implements IAbility {

	protected float timeUntilShoot_secs = 0;
	protected int magazineSize;
	protected int bulletsLeftInMag;
	protected float shotInterval_secs, reloadInterval_secs;


	public AbstractMagazineGun(IEntityController _game, int id, int type, AbstractAvatar owner, int num, String _name, float shotInt, float reloadInt, int magSize) { // ICanShoot _shooter, 
		super(_game, id, type, owner, num, _name);

		this.shotInterval_secs = shotInt;
		this.reloadInterval_secs = reloadInt;
		this.magazineSize = magSize;
		this.bulletsLeftInMag = this.magazineSize;
	}


	public abstract void launchBullet();


	@Override
	public final boolean activate() {
		if (this.timeUntilShoot_secs <= 0 && bulletsLeftInMag > 0) {
			this.launchBullet();
			timeUntilShoot_secs = this.shotInterval_secs;
			bulletsLeftInMag--;
			return true;
		} else {
			if (bulletsLeftInMag <= 0) {
				Globals.p("No bullets");
			} else if (timeUntilShoot_secs > 0) {
				Globals.p("Shooting too soon");
			}
		}
		return false;
	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		super.processByServer(server, tpf_secs);

		if (game.isServer()) { // Only server can reload
			if (this.bulletsLeftInMag <= 0) {
				// Reload
				Globals.p("Reloading");
				this.bulletsLeftInMag = this.magazineSize;
				this.timeUntilShoot_secs += this.reloadInterval_secs;
				server.networkServer.sendMessageToAll(new AbilityUpdateMessage(true, this));
			}
		}
		timeUntilShoot_secs -= tpf_secs;
	}


	@Override
	public void processByClient(AbstractGameClient client, float tpf_secs) {
		timeUntilShoot_secs -= tpf_secs;
		
	}


	@Override
	public String getHudText() {
		if (this.bulletsLeftInMag == this.magazineSize && this.timeUntilShoot_secs > shotInterval_secs) {
			return name + " RELOADING";
		} else {
			return name + " (" + this.bulletsLeftInMag + "/" + this.magazineSize  +")";
		}
	}


	@Override
	public void encode(AbilityUpdateMessage aum) {
		aum.bulletsLeftInMag = bulletsLeftInMag;
		aum.timeUntilShoot = timeUntilShoot_secs;

	}


	@Override
	public void decode(AbilityUpdateMessage aum) {
		this.bulletsLeftInMag = aum.bulletsLeftInMag;
		timeUntilShoot_secs = aum.timeUntilShoot;
	}

}

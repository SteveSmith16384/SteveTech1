package com.scs.stetech1.weapons;

import com.scs.stetech1.components.ICanShoot;
import com.scs.stetech1.entities.AbstractAvatar;
import com.scs.stetech1.netmessages.AbilityUpdateMessage;
import com.scs.stetech1.server.AbstractGameServer;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.AbstractAbility;
import com.scs.stetech1.shared.IAbility;
import com.scs.stetech1.shared.IEntityController;

public abstract class AbstractMagazineGun extends AbstractAbility implements IAbility {

	protected ICanShoot shooter;
	protected float timeUntilShoot_secs = 0;
	protected int magazineSize;
	protected int bulletsLeftInMag;
	protected float shotInterval_secs, reloadInterval_secs;


	public AbstractMagazineGun(IEntityController _game, int _num, String _name, ICanShoot _shooter, float shotInt, float reloadInt, int magSize) {
		super(_game, (AbstractAvatar)_shooter, _num, _name);

		shooter = _shooter;
		this.shotInterval_secs = shotInt;
		this.reloadInterval_secs = reloadInt;
		this.magazineSize = magSize;
		this.bulletsLeftInMag = this.magazineSize;
	}


	public abstract void launchBullet();


	@Override
	public final boolean activate(float interpol) {
		if (this.timeUntilShoot_secs <= 0 && bulletsLeftInMag > 0) {
			this.launchBullet();
			timeUntilShoot_secs = this.shotInterval_secs;
			bulletsLeftInMag--;
			return true;
		}
		return false;
	}


	@Override
	public void process(float tpf_secs) {
		super.process(tpf_secs);
		
		if (game.isServer()) { // Only server can reload
			if (this.bulletsLeftInMag <= 0) {
				// Reload
				Settings.p("Reloading");
				this.bulletsLeftInMag = this.magazineSize;
				this.timeUntilShoot_secs += this.reloadInterval_secs;
				AbstractGameServer server = (AbstractGameServer)game;
				server.networkServer.sendMessageToAll(new AbilityUpdateMessage(true, (AbstractAvatar)this.shooter, num));
			}
		}
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

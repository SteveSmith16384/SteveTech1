package com.scs.stetech1.weapons;

import com.scs.stetech1.IAbility;
import com.scs.stetech1.components.ICanShoot;
import com.scs.stetech1.netmessages.AbilityUpdateMessage;
import com.scs.stetech1.server.AbstractGameServer;
import com.scs.stetech1.shared.IEntityController;

public abstract class AbstractMagazineGun implements IAbility {

	protected IEntityController game;

	protected ICanShoot shooter;
	protected String name;

	protected float timeUntilShoot_secs = 0;
	protected int magazineSize;
	protected int bulletsLeftInMag;
	protected float shotInterval_secs, reloadInterval_secs;


	public AbstractMagazineGun(IEntityController _game, String _name, ICanShoot _shooter, float shotInt, float reloadInt, int magSize) {
		super();

		game = _game;
		name = _name;
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
	public void process(AbstractGameServer server, float tpf_secs) {
		if (game.isServer()) { // Only server can reload
			if (this.bulletsLeftInMag <= 0) {
				// Reload
				this.bulletsLeftInMag = this.magazineSize;
				this.timeUntilShoot_secs += this.reloadInterval_secs;
				// todo - send msg
				//server.broadcast(msg);
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

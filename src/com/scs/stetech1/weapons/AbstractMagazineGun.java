package com.scs.stetech1.weapons;

import com.scs.stetech1.IAbility;
import com.scs.stetech1.client.GenericClient;
import com.scs.stetech1.components.ICanShoot;
import com.scs.stetech1.netmessages.AbilityUpdateMessage;
import com.scs.stetech1.server.ServerMain;
import com.scs.stetech1.shared.IEntityController;

public abstract class AbstractMagazineGun implements IAbility {

	protected IEntityController game;

	protected ICanShoot shooter;
	protected String name;

	protected float timeUntilShoot = 0;
	protected int magazineSize;
	protected int bulletsLeftInMag;
	protected float shotInterval, reloadInterval;


	public AbstractMagazineGun(IEntityController _game, String _name, ICanShoot _shooter, float shotInt, float reloadInt, int magSize) {
		super();

		game = _game;
		name = _name;
		shooter = _shooter;
		this.shotInterval = shotInt;
		this.reloadInterval = reloadInt;
		this.magazineSize = magSize;

		this.bulletsLeftInMag = this.magazineSize;
	}


	public abstract void launchBullet();


	@Override
	public final boolean activate(float interpol) {
		if (this.timeUntilShoot <= 0 && bulletsLeftInMag > 0) {
			this.launchBullet();
			timeUntilShoot = this.shotInterval;
			bulletsLeftInMag--;
			return true;
		}
		return false;
	}


	@Override
	public void process(ServerMain server, float interpol) {
		if (game.isServer()) { // Only server can reload
			if (this.bulletsLeftInMag <= 0) {
				// Reload
				this.bulletsLeftInMag = this.magazineSize;
				this.timeUntilShoot += this.reloadInterval;
				// todo - send msg
			}
		}
		timeUntilShoot -= interpol;
	}


	@Override
	public String getHudText() {
		if (this.bulletsLeftInMag == this.magazineSize && this.timeUntilShoot > shotInterval) {
			return name + " RELOADING";
		} else {
			return name + " (" + this.bulletsLeftInMag + "/" + this.magazineSize  +")";
		}
	}


	@Override
	public void encode(AbilityUpdateMessage aum) {
		aum.bulletsLeftInMag = bulletsLeftInMag;
		aum.timeUntilShoot = timeUntilShoot;
		
	}


	@Override
	public void decode(AbilityUpdateMessage aum) {
		this.bulletsLeftInMag = aum.bulletsLeftInMag;
		timeUntilShoot = aum.timeUntilShoot;
	}

}

package com.scs.stetech1.weapons;

import java.util.LinkedList;
import java.util.List;

import com.scs.stetech1.abilities.IAbility;
import com.scs.stetech1.components.IBullet;
import com.scs.stetech1.components.ICanShoot;
import com.scs.stetech1.shared.IEntityController;

public abstract class AbstractMagazineGun implements IAbility {

	protected IEntityController game;
	protected ICanShoot shooter;
	protected String name;

	protected float timeUntilShoot = 0;
	protected int magazineSize;
	protected int bulletsLeftInMag;
	protected float shotInterval, reloadInterval; 
	protected List<IBullet> awaitingBullets = new LinkedList<>();

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


	public abstract void launchBullet();//IEntityController _game, ICanShoot _shooter);


	@Override
	public final boolean activate(float interpol) {
		if (this.timeUntilShoot <= 0 && bulletsLeftInMag > 0) {
			this.launchBullet();//game, shooter);
			timeUntilShoot = this.shotInterval;
			bulletsLeftInMag--;
			return true;
		}
		return false;
	}


	@Override
	public void process(float interpol) {
		if (this.bulletsLeftInMag <= 0) {
			// Reload
			this.bulletsLeftInMag = this.magazineSize;
			this.timeUntilShoot += this.reloadInterval;
		}
		timeUntilShoot -= interpol;
		//return false;
	}


	@Override
	public String getHudText() {
		if (this.bulletsLeftInMag == this.magazineSize && this.timeUntilShoot > shotInterval) {
			return name + " RELOADING";
		} else {
			return name + " (" + this.bulletsLeftInMag + "/" + this.magazineSize  +")";
		}
	}

}

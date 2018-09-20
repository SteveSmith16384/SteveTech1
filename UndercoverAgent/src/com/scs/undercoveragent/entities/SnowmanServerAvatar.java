package com.scs.undercoveragent.entities;

import com.scs.stevetech1.avatartypes.PersonAvatar;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IDebrisTexture;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.INotifiedOfCollision;
import com.scs.stevetech1.entities.AbstractBullet;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.input.IInputDevice;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.undercoveragent.UASimplePlayerData;
import com.scs.undercoveragent.UASounds;
import com.scs.undercoveragent.UAStaticData;
import com.scs.undercoveragent.UndercoverAgentClientEntityCreator;
import com.scs.undercoveragent.models.SnowmanModel;

public class SnowmanServerAvatar extends AbstractServerAvatar implements INotifiedOfCollision, IDebrisTexture {

	public SnowmanServerAvatar(IEntityController _module, ClientData client, IInputDevice _input, int eid) {
		super(_module, UndercoverAgentClientEntityCreator.AVATAR, client, _input, eid, new SnowmanModel(_module.getAssetManager()), UAStaticData.START_HEALTH, 0, new PersonAvatar(_module, _input, UAStaticData.MOVE_SPEED, UAStaticData.JUMP_FORCE));
	}


	@Override
	public void getReadyForGame() {
		super.getReadyForGame();

		UASimplePlayerData data = (UASimplePlayerData)this.client.playerData;
		data.score = 0;
	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf) {
		super.processByServer(server, tpf);

		// Force player to jump if they haven't moved
		if (this.alive) {// && server.gameData.isInGame()) {
			long timeSinceMove = System.currentTimeMillis() - super.avatarControl.getLastMoveTime();
			if (timeSinceMove > UAStaticData.AUTO_JUMP_INTERVAL_SECS * 1000) {
				if (this.avatarControl.jump()) { // Sto sound getting repeated if we can't jump for whatever reason
					server.playSound(this.playerID, UASounds.NOTMOVE, this.getID(), this.getWorldTranslation(), Globals.DEFAULT_VOLUME, false);
				}
			}
		}
	}


	@Override
	public void damaged(float amt, IEntity collider, String reason) {
		super.damaged(amt, collider, reason);

		if (collider instanceof SnowballBullet) {
			SnowballBullet sb = (SnowballBullet)collider;
			AbstractGameServer server = (AbstractGameServer)game;
			server.sendExplosionShards(sb.getWorldTranslation(), 12, .8f, 1.2f, .005f, .02f, "Textures/snow.jpg");
			this.avatarControl.jump(); // Also make them jump

			server.playSound(-1, UASounds.BIG_SPLAT, this.getID(), this.getWorldTranslation(), Globals.DEFAULT_VOLUME, false);
		}
	}


	@Override
	protected void setDied(IEntity killer, String reason) {
		super.setDied(killer, reason);

		if (killer != null && killer instanceof SnowmanServerAvatar) {
			SnowmanServerAvatar csp = (SnowmanServerAvatar)killer;
			csp.incScore(1);
		}
		
		AbstractGameServer server = (AbstractGameServer)game;
		server.playSound(-1, UASounds.DIED, this.getID(), this.getWorldTranslation(), Globals.DEFAULT_VOLUME, false);
	}


	private void incScore(int i) {
		UASimplePlayerData data = (UASimplePlayerData)this.client.playerData;
		data.score += i;
		AbstractGameServer server = (AbstractGameServer)game;
		server.sendSimpleGameDataToClients();
	}


	@Override
	public String getDebrisTexture() {
		return "Textures/snow.jpg";
	}


	@Override
	public float getMinDebrisSize() {
		return 0.001f;
	}


	@Override
	public float getMaxDebrisSize() {
		return 0.004f;
	}


	@Override
	public void updateClientSideHealth(int amt) {

	}


	@Override
	public void notifiedOfCollision(PhysicalEntity pe) {
		if (game.isServer()) {
			if (pe instanceof AbstractBullet) {
				if (!this.canBeDamaged()) {
					AbstractGameServer server = (AbstractGameServer)game;
					AbstractBullet bullet = (AbstractBullet)pe;
					server.playSound(bullet.playerID, UASounds.HAHA, this.getID(), this.getWorldTranslation(), Globals.DEFAULT_VOLUME, false);
				}
			}
		}
	}


}

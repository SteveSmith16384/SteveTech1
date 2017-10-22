package com.scs.stetech1.client;

import com.jme3.math.Vector3f;
import com.scs.stetech1.client.entities.ClientPlayersAvatar;
import com.scs.stetech1.client.entities.Crate;
import com.scs.stetech1.client.entities.Floor;
import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.netmessages.NewEntityMessage;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.AbstractPlayersAvatar;
import com.scs.stetech1.shared.EntityTypes;

public class EntityCreator {

	private EntityCreator() {

	}

	/**
	 * @param args
	 */
	public static IEntity createEntity(SorcerersClient game, NewEntityMessage msg) {
		Settings.p("Creating " + EntityTypes.getName(msg.type));
		
		switch (msg.type) {
		case EntityTypes.AVATAR:
		{
			AbstractPlayersAvatar avatar = new ClientPlayersAvatar(game, msg.entityID, game.input, game.getCamera(), game.hud);
			avatar.playerControl.warp(msg.pos);
			if (game.avatar == null && msg.entityID == game.playersAvatarID) {
				game.avatar = avatar;
			}
			return avatar;
		}
		
		case EntityTypes.FLOOR:
		{
			Vector3f size = (Vector3f)msg.data.get("size");
			String tex = (String)msg.data.get("tex");
			Floor floor = new Floor(game, msg.pos.x, msg.pos.y, msg.pos.z, size.x, size.y, size.z, tex, null);
			return floor;
		}
		
		case EntityTypes.CRATE:
		{
			Vector3f size = (Vector3f)msg.data.get("size");
			String tex = (String)msg.data.get("tex");
			float rot = (Float)msg.data.get("rot");
			Crate floor = new Crate(game, msg.pos.x, msg.pos.y, msg.pos.z, size.x, size.y, size.z, tex, rot);
			return floor;
		}
		
		default:
			throw new RuntimeException("Unknown entity type: " + EntityTypes.getName(msg.type));
		}
	}
}

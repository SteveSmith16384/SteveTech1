package com.scs.stetech1.client;

import com.jme3.math.Vector3f;
import com.scs.stetech1.client.entities.ClientPlayersAvatar;
import com.scs.stetech1.client.entities.Crate;
import com.scs.stetech1.client.entities.EnemyPlayersAvatar;
import com.scs.stetech1.client.entities.Floor;
import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.netmessages.NewEntityMessage;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.EntityTypes;

public class EntityCreator {

	private EntityCreator() {

	}

	/**
	 * @param args
	 */
	public static IEntity createEntity(SorcerersClient game, NewEntityMessage msg) {
		Settings.p("Creating " + EntityTypes.getName(msg.type));
		int id = (Integer)msg.data.get("id");

		switch (msg.type) {
		case EntityTypes.AVATAR:
		{
			int playerID = (int)msg.data.get("playerID");
			if (playerID == game.playerID) {
				ClientPlayersAvatar avatar = new ClientPlayersAvatar(game, msg.entityID, game.input, game.getCamera(), game.hud, id, msg.pos.x, msg.pos.y, msg.pos.z);
				game.avatar = avatar;
				return avatar;
			} else {
				// Create a simple avatar since we don't control these
				EnemyPlayersAvatar avatar = new EnemyPlayersAvatar(game, playerID, id);
				return avatar;
			}
		}

		case EntityTypes.FLOOR:
		{
			Vector3f size = (Vector3f)msg.data.get("size");
			String tex = (String)msg.data.get("tex");
			Floor floor = new Floor(game, id, msg.pos.x, msg.pos.y, msg.pos.z, size.x, size.y, size.z, tex, null);
			return floor;
		}

		case EntityTypes.CRATE:
		{
			Vector3f size = (Vector3f)msg.data.get("size");
			String tex = (String)msg.data.get("tex");
			//float rot = (Float)msg.data.get("rot");
			Crate crate = new Crate(game, id, msg.pos.x, msg.pos.y, msg.pos.z, size.x, size.y, size.z, tex);
			return crate;  //crate.getMainNode().getWorldTranslation();
		}

		default:
			throw new RuntimeException("Unknown entity type: " + EntityTypes.getName(msg.type));
		}
	}
}

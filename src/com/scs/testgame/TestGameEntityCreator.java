package com.scs.testgame;

import com.jme3.math.Vector3f;
import com.scs.stetech1.components.IEntity;
import com.scs.stetech1.entities.ClientPlayersAvatar;
import com.scs.stetech1.entities.DebuggingSphere;
import com.scs.stetech1.entities.EnemyPlayersAvatar;
import com.scs.stetech1.netmessages.NewEntityMessage;
import com.scs.stetech1.server.Settings;
import com.scs.stetech1.shared.EntityTypes;
import com.scs.testgame.entities.TestGameClientPlayersAvatar;
import com.scs.testgame.entities.Crate;
import com.scs.testgame.entities.Floor;
import com.scs.testgame.entities.Grenade;
import com.scs.testgame.entities.TestGameEnemyPlayersAvatar;
import com.scs.testgame.entities.Wall;

/*
 * This is only used client-side.
 */
public class TestGameEntityCreator {

	private TestGameClient game;
	
	public TestGameEntityCreator(TestGameClient _game) {
		game =_game;
	}


	public IEntity createEntity(NewEntityMessage msg) {
		Settings.p("Creating " + EntityTypes.getName(msg.type));
		int id = msg.entityID;// (Integer)msg.data.get("id");

		switch (msg.type) {
		case EntityTypes.AVATAR:
		{
			int playerID = (int)msg.data.get("playerID");
			if (playerID == game.playerID) {
				ClientPlayersAvatar avatar = new TestGameClientPlayersAvatar(game, msg.entityID, game.input, game.getCamera(), game.hud, id, msg.pos.x, msg.pos.y, msg.pos.z);
				game.avatar = avatar;
				return avatar;
			} else {
				// Create a simple avatar since we don't control these
				EnemyPlayersAvatar avatar = new TestGameEnemyPlayersAvatar(game, playerID, id, msg.pos.x, msg.pos.y, msg.pos.z);
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
			Crate crate = new Crate(game, id, msg.pos.x, msg.pos.y, msg.pos.z, size.x, size.y, size.z, tex, 0); // Give def rotation of 0, since it will get rotated anyway
			return crate;  //crate.getMainNode().getWorldTranslation();
		}

		case EntityTypes.WALL:
		{
			float w = (float)msg.data.get("w");
			float h = (float)msg.data.get("h");
			String tex = (String)msg.data.get("tex");
			float rot = (Float)msg.data.get("rot");
			Wall wall = new Wall(game, id, msg.pos.x, msg.pos.y, msg.pos.z, w, h, tex, rot);
			return wall;
		}

		/*case EntityTypes.LASER_BULLET:
		{
			String tex = (String)msg.data.get("tex");
			float rot = (Float)msg.data.get("rot");
			LaserBullet laser = new LaserBullet(game, id, msg.pos.x, msg.pos.y, msg.pos.z, w, h, tex, rot);
			return laser;
		}*/

		case EntityTypes.GRENADE:
		{
			//String tex = (String)msg.data.get("tex");
			//float rot = (Float)msg.data.get("rot");
			Grenade grenade = new Grenade(game, id, new Vector3f(msg.pos.x, msg.pos.y, msg.pos.z));
			return grenade;
		}

		case EntityTypes.DEBUGGING_SPHERE:
		{
			DebuggingSphere laser = new DebuggingSphere(game, id, msg.pos.x, msg.pos.y, msg.pos.z);
			return laser;
		}

		default:
			throw new RuntimeException("Unknown entity type: " + EntityTypes.getName(msg.type));
		}
	}
}

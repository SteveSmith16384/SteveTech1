package com.scs.stevetech1.entities;

import java.util.HashMap;

import com.jme3.math.Vector3f;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.ICausesHarmOnContact;
import com.scs.stevetech1.components.IClientControlled;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IEntityContainer;
import com.scs.stevetech1.components.ILaunchable;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.components.IRemoveOnContact;
import com.scs.stevetech1.netmessages.EntityLaunchedMessage;
import com.scs.stevetech1.server.AbstractEntityServer;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.stevetech1.systems.client.LaunchData;

public abstract class AbstractBullet extends PhysicalEntity implements IProcessByClient, ILaunchable, IRemoveOnContact, ICausesHarmOnContact, IClientControlled {

	protected boolean launched = false;
	public IEntity shooter; // So we know who not to collide with
	private int side;
	private ClientData client; // Only used server-side

	public AbstractBullet(IEntityController _game, int id, int type, String name, IEntityContainer owner, int _side, ClientData _client) {
		super(_game, id, type, name, true);

		client = _client;
		
		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("side", side);
			creationData.put("containerID", owner.getID());
		}

		if (owner != null) { // Only snowball fired by us have an owner
			/*if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
				Globals.p("Adding snowball entity " + id + " to owner " + owner.getID());
			}*/
			owner.addToCache(this);
		}

		side = _side;

		/*
		Sphere sphere = new Sphere(8, 8, 0.1f, true, false);
		sphere.setTextureMode(TextureMode.Projected);
		Geometry ball_geo = new Geometry("grenade", sphere);

		if (!_game.isServer()) { // Not running in server
			TextureKey key3 = new TextureKey( "Textures/snow.jpg");
			Texture tex3 = game.getAssetManager().loadTexture(key3);
			Material floor_mat = null;
			if (Globals.LIGHTING) {
				floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");
				floor_mat.setTexture("DiffuseMap", tex3);
			} else {
				floor_mat = new Material(game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
				floor_mat.setTexture("ColorMap", tex3);
			}
			ball_geo.setMaterial(floor_mat);
		}

		ball_geo.setModelBound(new BoundingSphere());
		this.mainNode.attachChild(ball_geo); //ball_geo.getModelBound();

		this.getMainNode().setUserData(Globals.ENTITY, this);
*/
		this.collideable = false;

	}


	@Override
	public void launch(IEntity _shooter, Vector3f startPos, Vector3f dir) {
		if (launched) { // We might be the client that fired the bullet, which we've already launched
			//Globals.p("Snowball already launched.  This may be a good sign.");
			return;
		}

		if (_shooter == null) {
			throw new RuntimeException("Null launcher");
		}

		/*if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
			Globals.p("Launching entity " + this.getID());
		}*/

		launched = true;
		shooter = _shooter;

		this.createSimpleRigidBody(dir);

		game.getGameNode().attachChild(this.mainNode);
		this.setWorldTranslation(startPos);
		this.mainNode.updateGeometricState();

		this.collideable = true;

		if (game.isServer()) {
			AbstractGameServer server = (AbstractGameServer)game;

			// fast forward it!
			float totalTimeToFFwd = server.clientRenderDelayMillis + (client.playerData.pingRTT/2);
			float tpf_secs = (float)server.tickrateMillis / 1000f;
			while (totalTimeToFFwd > 0) {
				totalTimeToFFwd -= server.tickrateMillis;
				super.processByServer(server, tpf_secs);
				if (this.removed) {
					break;
				}
			}

			// If server, send messages to clients to tell them it has been launched
			LaunchData ld = new LaunchData(startPos, dir, shooter.getID(), System.currentTimeMillis() - server.clientRenderDelayMillis); // "-Globals.CLIENT_RENDER_DELAY" so they render it immed.
			server.gameNetworkServer.sendMessageToAll(new EntityLaunchedMessage(this.getID(), ld));
		} else {
			// todo - client confirms that bullet launched
		}

	}

	
	protected abstract void createSimpleRigidBody(Vector3f dir);
	

	@Override
	public void processByServer(AbstractEntityServer server, float tpf_secs) {
		if (launched) {
			super.processByServer(server, tpf_secs);
		}
	}


	@Override
	public void processByClient(AbstractGameClient client, float tpf_secs) {
		if (launched) {
			//Globals.p("Moving snowball:" + this.simpleRigidBody.getLinearVelocity());
			simpleRigidBody.process(tpf_secs); //this.mainNode;
		}
	}


	@Override
	public void calcPosition(AbstractGameClient mainApp, long serverTimeToUse, float tpf_secs) {
		// Do nothing!
	}


	@Override
	public boolean sendUpdates() {
		return false; 
	}



	@Override
	public IEntity getLauncher() {
		return shooter;
	}


	@Override
	public int getSide() {
		return side;//shooter.getSide();
	}


	@Override
	public boolean isClientControlled() {
		return launched; // All launched snowballs are under client control
	}


	@Override
	public boolean hasBeenLaunched() {
		return this.launched;
	}


	@Override
	public IEntity getActualShooter() {
		return shooter;
	}

}

package com.scs.undercoveragent.entities;

import com.jme3.asset.TextureKey;
import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.texture.Texture;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.components.IEntityContainer;
import com.scs.stevetech1.components.INotifiedOfCollision;
import com.scs.stevetech1.entities.AbstractBullet;
import com.scs.stevetech1.entities.DebuggingSphere;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.undercoveragent.UndercoverAgentClientEntityCreator;

public class SnowballBullet extends AbstractBullet implements INotifiedOfCollision {//, IRemoveOnContact {

	public SnowballBullet(IEntityController _game, int id, IEntityContainer<SnowballBullet> owner, int _side, ClientData _client) {
		super(_game, id, UndercoverAgentClientEntityCreator.SNOWBALL_BULLET, "Snowball", owner, _side, _client);

		Sphere sphere = new Sphere(8, 8, 0.1f, true, false);
		sphere.setTextureMode(TextureMode.Projected);
		Geometry ball_geo = new Geometry("grenade", sphere);

		if (!_game.isServer()) { // Not running in server
				ball_geo.setShadowMode(ShadowMode.CastAndReceive);
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

		ball_geo.setModelBound(new BoundingBox());
		this.mainNode.attachChild(ball_geo); //ball_geo.getModelBound();
		this.getMainNode().setUserData(Globals.ENTITY, this);

	}

/*
	@Override
	public void launch(IEntity _shooter, Vector3f startPos, Vector3f dir) {
		if (launched) { // We might be the client that fired the bullet, which we've already launched
			//Globals.p("Snowball already launched.  This may be a good sign.");
			return;
		}

		if (_shooter == null) {
			throw new RuntimeException("Null launcher");
		}

		launched = true;
		shooter = _shooter;

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, game.getPhysicsController(), true, this);
		this.simpleRigidBody.setBounciness(0f);
		this.simpleRigidBody.setLinearVelocity(dir.normalize().mult(10));

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
*/

	@Override
	public float getDamageCaused() {
		return 1;
	}


	@Override
	public void collided(PhysicalEntity pe) {
		//Globals.p("Snowball hit something at " + System.currentTimeMillis());
		if (Globals.SHOW_BULLET_COLLISION_POS) {
			if (game.isServer()) {
				// Create debugging sphere
				Vector3f pos = this.getWorldTranslation();
				DebuggingSphere ds = new DebuggingSphere(game, UndercoverAgentClientEntityCreator.DEBUGGING_SPHERE, game.getNextEntityID(), pos.x, pos.y, pos.z, true);
				game.addEntity(ds);
			}
		}
		this.remove();
	}

	@Override
	protected void createSimpleRigidBody(Vector3f dir) {
		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), true, this);
		this.simpleRigidBody.setBounciness(0f);
		this.simpleRigidBody.setLinearVelocity(dir.normalize().mult(10));
		
	}

}

package boxwars.entities;

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
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.INotifiedOfCollision;
import com.scs.stevetech1.entities.AbstractBullet;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

import boxwars.BoxWarsServer;

public class PlayersBullet extends AbstractBullet implements INotifiedOfCollision {

	public PlayersBullet(IEntityController _game, int id, int playerOwnerId, IEntity _shooter, Vector3f startPos, Vector3f _dir, byte _side, ClientData _client) {
		super(_game, id, BoxWarsServer.BULLET, "Bullet", playerOwnerId, _shooter, startPos, _dir, _side, _client, false, 0f, 0f);

		Sphere sphere = new Sphere(8, 8, 0.1f, true, false);
		sphere.setTextureMode(TextureMode.Projected);
		Geometry ball_geo = new Geometry("bullet", sphere);

		if (!_game.isServer()) { // Not running on server
			ball_geo.setShadowMode(ShadowMode.CastAndReceive);
			TextureKey key3 = new TextureKey( "Textures/sun.jpg");
			Texture tex3 = game.getAssetManager().loadTexture(key3);
			Material floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");
			floor_mat.setTexture("DiffuseMap", tex3);
			ball_geo.setMaterial(floor_mat);
		}

		ball_geo.setModelBound(new BoundingBox());
		this.mainNode.attachChild(ball_geo);
		//this.getMainNode().setUserData(Globals.ENTITY, this);
	}


	@Override
	public float getDamageCaused() {
		return 1;
	}


	@Override
	public void collided(PhysicalEntity pe) {
		//this.remove();
		game.markForRemoval(this.getID());
	}


	@Override
	protected void createModelAndSimpleRigidBody(Vector3f dir) {
		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), true, this);
		this.simpleRigidBody.setBounciness(0f);
		this.simpleRigidBody.setLinearVelocity(dir.normalize().mult(10));

	}

}

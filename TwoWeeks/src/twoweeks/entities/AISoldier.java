package twoweeks.entities;

import java.util.HashMap;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.Camera.FrustumIntersect;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Box;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.IAffectedByPhysics;
import com.scs.stevetech1.components.IAnimatedClientSide;
import com.scs.stevetech1.components.IAnimatedServerSide;
import com.scs.stevetech1.components.ICausesHarmOnContact;
import com.scs.stevetech1.components.IDamagable;
import com.scs.stevetech1.components.IDrawOnHUD;
import com.scs.stevetech1.components.IGetRotation;
import com.scs.stevetech1.components.IKillable;
import com.scs.stevetech1.components.INotifiedOfCollision;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.components.IRewindable;
import com.scs.stevetech1.components.ISetRotation;
import com.scs.stevetech1.components.ITargetable;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.jme.JMEAngleFunctions;
import com.scs.stevetech1.netmessages.EntityKilledMessage;
import com.scs.stevetech1.server.AbstractEntityServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

import ssmith.util.RealtimeInterval;
import twoweeks.client.TwoWeeksClientEntityCreator;
import twoweeks.models.SoldierModel;
import twoweeks.server.ai.IArtificialIntelligence;
import twoweeks.server.ai.ShootingSoldierAI3;

public class AISoldier extends PhysicalEntity implements IAffectedByPhysics, IDamagable, INotifiedOfCollision, 
IRewindable, IAnimatedClientSide, IAnimatedServerSide, IDrawOnHUD, IProcessByClient, IGetRotation, ISetRotation, IKillable, ITargetable { //, ICanShoot {//, IUnit {

	public static final float START_HEALTH = 10f;
	public static final float SPEED = .53f;//.47f;

	private SoldierModel soldierModel; // Need this to animate the model
	private float health = START_HEALTH;
	public int side;
	private IArtificialIntelligence ai;
	private int serverSideCurrentAnimCode; // Server-side
	private RealtimeInterval shootInt = new RealtimeInterval(3000);

	// HUD
	private BitmapText hudNode;
	private static BitmapFont font_small;

	public AISoldier(IEntityController _game, int id, float x, float y, float z, int _side) {
		super(_game, id, TwoWeeksClientEntityCreator.AI_SOLDIER, "AISoldier", true);

		side = _side;
		soldierModel = new SoldierModel(game.getAssetManager()); // Need it for dimensions for bb


		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("side", side);

			ai = new ShootingSoldierAI3(this);
		} else {
			this.soldierModel.createAndGetModel(_side);
			game.getGameNode().attachChild(this.soldierModel.getModel());
		}

		// Create box for collisions
		Box box = new Box(soldierModel.getBoundingBox().getXExtent(), soldierModel.getBoundingBox().getYExtent(), soldierModel.getBoundingBox().getZExtent());
		Geometry bbGeom = new Geometry("bbGeom_" + name, box);
		bbGeom.setLocalTranslation(0, soldierModel.getBoundingBox().getYExtent(), 0); // origin is centre!
		bbGeom.setCullHint(CullHint.Always); // Don't draw the collision box
		bbGeom.setUserData(Globals.ENTITY, this);

		this.mainNode.attachChild(bbGeom);
		mainNode.setUserData(Globals.ENTITY, this);
		mainNode.setLocalTranslation(x, y, z);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), game.isServer(), this); // was false
		simpleRigidBody.canWalkUpSteps = true;
		simpleRigidBody.setBounciness(0); // todo - copy to MA

		font_small = _game.getAssetManager().loadFont("Interface/Fonts/Console.fnt");
		hudNode = new BitmapText(font_small);
		hudNode.setText("Cpl. Jonlan");

	}


	@Override
	public void processByServer(AbstractEntityServer server, float tpf_secs) {
		if (health > 0) {
			// Randomly change direction
			/*if (NumberFunctions.rnd(1, 200) == 1) {
				Vector3f newdir = this.getRandomDirection();
				this.changeDirection(newdir);
			}*/

			ai.process(server, tpf_secs);
			this.serverSideCurrentAnimCode = ai.getAnimCode(); // AbstractAvatar.ANIM_WALKING;

			if (Globals.DEBUG_JUMPING_SHOOTER) {
				Globals.p("Soldier: " + this.getWorldTranslation().y);
			}
		} else {
			this.simpleRigidBody.setAdditionalForce(Vector3f.ZERO); // Stop moving
		}

		super.processByServer(server, tpf_secs);
	}


	@Override
	public void processByClient(IClientApp client, float tpf_secs) {
		// Set position and direction of avatar model, which doesn't get moved automatically
		this.soldierModel.getModel().setLocalTranslation(this.getWorldTranslation());
	}


	@Override
	public void fallenOffEdge() {
		this.remove();
	}


	@Override
	public void damaged(float amt, ICausesHarmOnContact collider, String reason) {
		if (health > 0) {
			this.health -= amt;
			if (health <= 0) {
				AbstractEntityServer server = (AbstractEntityServer)game;
				server.gameNetworkServer.sendMessageToAll(new EntityKilledMessage(this, collider.getActualShooter()));
				this.serverSideCurrentAnimCode = AbstractAvatar.ANIM_DIED;
				this.sendUpdate = true; // Send new anim code

				this.game.getPhysicsController().removeSimpleRigidBody(this.simpleRigidBody); // Prevent us colliding
				this.simpleRigidBody.setMovedByForces(false);
			}
		}
	}


	@Override
	public void remove() {
		super.remove();

		this.soldierModel.getModel().removeFromParent();
		this.hudNode.removeFromParent();
	}


	@Override
	public int getSide() {
		return side;
	}


	@Override
	public void collided(PhysicalEntity pe) {
		if (health > 0) {
			if (game.isServer()) {
				ai.collided(pe);
			}
		}
	}


	@Override
	public void setAnimCode(int animCode) {
		if (soldierModel != null) {
			this.soldierModel.setAnim(animCode);
			if (Globals.DEBUG_DIE_ANIM) {
				if (animCode == AbstractAvatar.ANIM_DIED) {
					Globals.p("setAnimCode=" + animCode);
				}
			}
		}
	}


	@Override
	public void processManualAnimation(float tpf_secs) {
		// Do nothing, already handled
	}


	/**
	 * Called server-side only,
	 */
	@Override
	public int getCurrentAnimCode() {
		if (Globals.DEBUG_DIE_ANIM) {
			if (soldierModel.getCurrentAnimCode() == AbstractAvatar.ANIM_DIED) {
				Globals.p("getCurrentAnimCode=" + this.soldierModel.getCurrentAnimCode());
			}
		}
		return this.serverSideCurrentAnimCode;
	}


	@Override
	public void drawOnHud(Camera cam) {
		if (health > 0) {
			FrustumIntersect insideoutside = cam.contains(this.getMainNode().getWorldBound());
			if (insideoutside != FrustumIntersect.Outside) {
				Vector3f pos = this.getWorldTranslation().add(0, SoldierModel.MODEL_HEIGHT, 0);
				Vector3f screen_pos = cam.getScreenCoordinates(pos);
				this.hudNode.setLocalTranslation(screen_pos.x, screen_pos.y, 0);
			}
		}
	}


	@Override
	public Node getHUDItem() {
		return this.hudNode;
	}


	@Override
	public void setRotation(Vector3f dir) {
		Vector3f dir2 = new Vector3f(dir.x, 0, dir.z); 
		JMEAngleFunctions.rotateToDirection(this.soldierModel.getModel(), dir2);
	}


	@Override
	public Vector3f getRotation() {
		return ai.getDirection();
	}


	@Override
	public void handleKilledOnClientSide(PhysicalEntity killer) {
		this.hudNode.removeFromParent();
	}


	@Override
	public boolean isValidTargetForSide(int shootersSide) {
		return shootersSide != this.side;
	}

	/*
	@Override
	public Vector3f getShootDir() {
		return this.ai.getCurrentTarget().getWorldTranslation().subtract(this.getWorldTranslation()).normalizeLocal();
	}


	@Override
	public Vector3f getBulletStartPos() {
		return this.getWorldTranslation();
	}
	 */

	public void shoot(PhysicalEntity target) {
		if (this.shootInt.hitInterval()) {
			if (Globals.DEBUG_AI_SHOOTING) {
				Globals.p("AI shooting!" + this.getWorldTranslation().y);
			}
			Vector3f pos = this.getWorldTranslation().clone();
			pos.y += this.soldierModel.getBulletStartHeight();
			Vector3f dir = target.getMainNode().getWorldBound().getCenter().subtract(pos).normalizeLocal();
			AIBullet bullet = new AIBullet(game, game.getNextEntityID(), side, pos.x, pos.y, pos.z, this, dir);
			this.game.addEntity(bullet);
		}
	}


	@Override
	public boolean isAlive() {
		return this.health > 0;
	}

}

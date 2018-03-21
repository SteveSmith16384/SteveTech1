package com.scs.moonbaseassault.entities;

import java.util.HashMap;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.Camera.FrustumIntersect;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.moonbaseassault.models.SoldierModel;
import com.scs.moonbaseassault.server.MoonbaseAssaultServer;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.components.IAffectedByPhysics;
import com.scs.stevetech1.components.ICausesHarmOnContact;
import com.scs.stevetech1.components.IClientSideAnimated;
import com.scs.stevetech1.components.IDamagable;
import com.scs.stevetech1.components.IDrawOnHUD;
import com.scs.stevetech1.components.INotifiedOfCollision;
import com.scs.stevetech1.components.IRewindable;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractEntityServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.ChronologicalLookup;
import com.scs.stevetech1.shared.HistoricalAnimationData;
import com.scs.stevetech1.shared.IEntityController;

import ssmith.lang.NumberFunctions;

public class AISoldier extends PhysicalEntity implements IAffectedByPhysics, IDamagable, INotifiedOfCollision, 
IRewindable, IClientSideAnimated, IDrawOnHUD {//, IUnit {

	private static final float SPEED = .5f;//.47f;

	private SoldierModel soldierModel;
	private float health = 1f;
	private Vector3f currDir;// = new Vector3f(1f, 0, 0);
	private ChronologicalLookup<HistoricalAnimationData> animList = new ChronologicalLookup<HistoricalAnimationData>(true, -1);
	private int side;

	private BitmapText hudNode;
	private static BitmapFont font_small;

	public AISoldier(IEntityController _game, int id, float x, float y, float z, int _side) {
		super(_game, id, MoonbaseAssaultClientEntityCreator.AI_SOLDIER, "AISoldier", true);

		side = _side;
		currDir = this.getRandomDirection();

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("side", side);
		}

		Spatial spatial = null;
		//if (!_game.isServer()) { // Not running in server
		//spatial = game.getAssetManager().loadModel("Models/zombie/Zombie.blend");
		//JMEFunctions.SetTextureOnSpatial(game.getAssetManager(), spatial, "Models/zombie/ZombieTexture.png");
		soldierModel = new SoldierModel(game.getAssetManager());
		spatial = soldierModel.createAndGetModel(true, side);
		/*} else {
			// Server
			Box box1 = new Box(w/2, h/2, d/2);
			spatial = new Geometry("AISoldier", box1);
			spatial.setLocalTranslation(0, h/2, 0); // Box origin is the centre
			//zm = new ZombieModel(game.getAssetManager());
			//spatial = zm.getModel(false);

		}*/
		this.mainNode.attachChild(spatial);
		mainNode.setLocalTranslation(x, y, z);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), true, this);
		simpleRigidBody.canWalkUpSteps = true;

		spatial.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);

		font_small = _game.getAssetManager().loadFont("Interface/Fonts/Console.fnt");
		hudNode = new BitmapText(font_small);
		hudNode.setText("Cpl. Jonlan");

	}


	@Override
	public void processByServer(AbstractEntityServer server, float tpf_secs) {
		if (health > 0) {
			
			if (NumberFunctions.rnd(1, 200) == 1) {
				Globals.p("Changing direction");
				Vector3f newdir = this.getRandomDirection();
				this.changeDirection(newdir);
			}
			
			if (!Globals.DEBUG_CAN_SEE) {
				this.simpleRigidBody.setAdditionalForce(this.currDir.mult(SPEED)); // Walk forwards
			} else {
				/*if (MoonbaseAssaultServer.player != null) {
					boolean cansee = this.canSee(MoonbaseAssaultServer.player, 100f);
					if (cansee) {
						//Globals.p("Soldier can see player");
					}
				}*/
			}

			this.soldierModel.setAnim(AbstractAvatar.ANIM_WALKING);
		} else {
			this.simpleRigidBody.setAdditionalForce(Vector3f.ZERO); // Stop moving
		}
		//this.currentAnimCode = this.soldierModel.getCurrentAnimCode();// AbstractAvatar.ANIM_WALKING;

		super.processByServer(server, tpf_secs);
	}


	@Override
	public void fallenOffEdge() {
		//this.respawn();
		this.remove();
	}


	@Override
	public void damaged(float amt, ICausesHarmOnContact collider, String reason) {
		if (health > 0) {
			this.health -= amt;
			if (health <= 0) {
				this.soldierModel.setAnim(AbstractAvatar.ANIM_DIED);
				//this.simpleRigidBody.setMovable(false); // Stop it being pushed
				this.game.getPhysicsController().removeSimpleRigidBody(this.simpleRigidBody); // Prevent us colliding
			}
		}
	}


	@Override
	public int getSide() {
		return side;
	}


	@Override
	public void collided(PhysicalEntity pe) {
		if (health > 0) {
			if (pe instanceof Floor == false) {
				Globals.p("AISoldier has collided with " + pe);
				// turn around
				changeDirection(currDir.mult(-1));
			}
		}
	}


	@Override
	public ChronologicalLookup<HistoricalAnimationData> getAnimList() {
		return animList;
	}


	@Override
	public void setAnimCode(int animCode) {
		this.soldierModel.setAnim(animCode);

	}


	@Override
	public void processManualAnimation(float tpf_secs) {
		// Do nothing, already handled
	}


	@Override
	public int getCurrentAnimCode() {
		return this.soldierModel.getCurrentAnimCode();
	}


	@Override
	public void drawOnHud(Camera cam) {
		FrustumIntersect insideoutside = cam.contains(this.getMainNode().getWorldBound());
		if (insideoutside != FrustumIntersect.Outside) {
			Vector3f pos = this.getWorldTranslation().add(0, SoldierModel.MODEL_HEIGHT, 0);
			Vector3f screen_pos = cam.getScreenCoordinates(pos);
			this.hudNode.setLocalTranslation(screen_pos.x, screen_pos.y, 0);
		}

	}


	@Override
	public Node getHUDItem() {
		return this.hudNode;
	}


	private Vector3f getRandomDirection() {
		int i = NumberFunctions.rnd(0,  3);
		switch (i) {
		case 0: return new Vector3f(1f, 0, 0);
		case 1: return new Vector3f(-1f, 0, 0);
		case 2: return new Vector3f(0f, 0, 1f);
		case 3: return new Vector3f(0f, 0, -1f);
		}
		throw new RuntimeException("Invalid direction: " + i);
	}
	
	
	private void changeDirection(Vector3f dir) {
		this.currDir.set(dir);
		this.getMainNode().lookAt(this.getWorldTranslation().add(currDir), Vector3f.UNIT_Y); // Point us in the right direction
	}


}

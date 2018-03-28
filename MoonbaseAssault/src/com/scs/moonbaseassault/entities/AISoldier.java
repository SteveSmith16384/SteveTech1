package com.scs.moonbaseassault.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.bounding.BoundingBox;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.Camera.FrustumIntersect;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.moonbaseassault.models.SoldierModel;
import com.scs.moonbaseassault.server.ai.IArtificialIntelligence;
import com.scs.moonbaseassault.server.ai.SimpleSoldierAI;
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
import com.scs.stevetech1.netmessages.EntityKilledMessage;
import com.scs.stevetech1.server.AbstractEntityServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.ChronologicalLookup;
import com.scs.stevetech1.shared.HistoricalAnimationData;
import com.scs.stevetech1.shared.IEntityController;

import ssmith.lang.NumberFunctions;

public class AISoldier extends PhysicalEntity implements IAffectedByPhysics, IDamagable, INotifiedOfCollision, 
IRewindable, IClientSideAnimated, IDrawOnHUD {//, IUnit {

	public static final float SPEED = .5f;//.47f;

	private static final float w = 0.3f;
	private static final float d = 0.3f;
	private static final float h = SoldierModel.MODEL_HEIGHT;


	private SoldierModel soldierModel;
	private float health = 1f;
	private ChronologicalLookup<HistoricalAnimationData> animList = new ChronologicalLookup<HistoricalAnimationData>(true, -1);
	private int side;
	private IArtificialIntelligence ai;

	// HUD
	private BitmapText hudNode;
	private static BitmapFont font_small;

	public AISoldier(IEntityController _game, int id, float x, float y, float z, int _side) {
		super(_game, id, MoonbaseAssaultClientEntityCreator.AI_SOLDIER, "AISoldier", true);

		side = _side;

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("side", side);

			ai = new SimpleSoldierAI(this);
		}

		Spatial spatial = null;
		if (!Globals.USE_BOXES_FOR_AI_SOLDIER) {
			soldierModel = new SoldierModel(game.getAssetManager());
			spatial = soldierModel.createAndGetModel(true, side);
		} else {
			Box box1 = new Box(w/2, h/2, d/2);
			spatial = new Geometry("AISoldier", box1);
			spatial.setLocalTranslation(0, h/2, 0); // Box origin is the centre

			TextureKey key3 = new TextureKey("Textures/fence.png");
			key3.setGenerateMips(true);
			Texture tex3 = game.getAssetManager().loadTexture(key3);
			tex3.setWrap(WrapMode.Repeat);

			Material floor_mat = null;
			if (Globals.LIGHTING) {
				floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
				floor_mat.setTexture("DiffuseMap", tex3);
			} else {
				floor_mat = new Material(game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
				floor_mat.setTexture("ColorMap", tex3);
			}
			spatial.setMaterial(floor_mat);
		}
		this.mainNode.attachChild(spatial);
		mainNode.setLocalTranslation(x, y, z);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), game.isServer(), this); // was false
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
			// Randomly change direction
			/*if (NumberFunctions.rnd(1, 200) == 1) {
				Vector3f newdir = this.getRandomDirection();
				this.changeDirection(newdir);
			}*/

			ai.process(tpf_secs);

			if (soldierModel != null) {
				this.soldierModel.setAnim(AbstractAvatar.ANIM_WALKING);
			}
		} else {
			this.simpleRigidBody.setAdditionalForce(Vector3f.ZERO); // Stop moving
		}

		super.processByServer(server, tpf_secs);
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
				if (soldierModel != null) {
					this.soldierModel.setAnim(AbstractAvatar.ANIM_DIED);
				}
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
			if (game.isServer()) {
				ai.collided(pe);
			}
		}
	}


	@Override
	public ChronologicalLookup<HistoricalAnimationData> getAnimList() {
		return animList;
	}


	@Override
	public void setAnimCode(int animCode) {
		if (soldierModel != null) {
			this.soldierModel.setAnim(animCode);
			if (Globals.DEBUG_DIE_ANIM) {
				if (animCode == AbstractAvatar.ANIM_DIED) {
					//Globals.p("Die anim");
				}
			}
		}
	}


	@Override
	public void processManualAnimation(float tpf_secs) {
		// Do nothing, already handled
	}


	@Override
	public int getCurrentAnimCode() {
		if (soldierModel != null) {
			return this.soldierModel.getCurrentAnimCode();
		} else {
			return -1;
		}
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



}

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
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.ChronologicalLookup;
import com.scs.stevetech1.shared.HistoricalAnimationData;
import com.scs.stevetech1.shared.IEntityController;

public class AISoldier extends PhysicalEntity implements IAffectedByPhysics, IDamagable, INotifiedOfCollision, 
IRewindable, IClientSideAnimated, IDrawOnHUD {//, IUnit {

	/*private static final float w = .5f;
	private static final float d = .7f;
	private static final float h = .5f;
*/
	private static final float SPEED = .5f;//.47f;

	private SoldierModel soldierModel;
	private float health = 1f;
	private Vector3f currDir = new Vector3f(1f, 0, 0);
	private ChronologicalLookup<HistoricalAnimationData> animList = new ChronologicalLookup<HistoricalAnimationData>(true, -1);
	private int side;
	private int currentAnimCode = -1;
	
	private BitmapText hudNode;
	private static BitmapFont font_small;

	public AISoldier(IEntityController _game, int id, float x, float y, float z, int _side) {
		super(_game, id, MoonbaseAssaultClientEntityCreator.AI_SOLDIER, "AISoldier", true);

		side = _side;

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

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this.mainNode, game.getPhysicsController(), true, this);

		spatial.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);
		
		font_small = _game.getAssetManager().loadFont("Interface/Fonts/Console.fnt");
		hudNode = new BitmapText(font_small);
		hudNode.setText("Cpl. Jonlan");
		
	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		if (health > 0) {
			this.getMainNode().lookAt(this.getWorldTranslation().add(currDir), Vector3f.UNIT_Y); // Point us in the right direction
			this.simpleRigidBody.setAdditionalForce(this.currDir.mult(SPEED));

			this.soldierModel.setAnim(AbstractAvatar.ANIM_WALKING);
			this.currentAnimCode = this.soldierModel.getCurrentAnimCode();// AbstractAvatar.ANIM_WALKING;
		}

		super.processByServer(server, tpf_secs);
	}


	@Override
	public void fallenOffEdge() {
		//this.respawn();
	}

	/*
	private void respawn() {
		this.setWorldTranslation(new Vector3f(10, 10, 10));

		EntityUpdateMessage eum = new EntityUpdateMessage();
		eum.addEntityData(this, true);
		AbstractGameServer server = (AbstractGameServer)this.game;
		server.gameNetworkServer.sendMessageToAll(eum);

	}
	 */

	@Override
	public void damaged(float amt, ICausesHarmOnContact collider, String reason) {
		if (health > 0) {
			this.health -= amt;
			if (health <= 0) {
				this.soldierModel.setAnim(AbstractAvatar.ANIM_DIED);
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
				currDir.multLocal(-1);
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
	public void processAnimation(float tpf_secs) {
		// Do nothing, already handled
	}


	@Override
	public int getCurrentAnimCode() {
		return currentAnimCode;
	}


	@Override
	public void drawOnHud(Camera cam) {
		FrustumIntersect insideoutside = cam.contains(this.getMainNode().getWorldBound());
		if (insideoutside != FrustumIntersect.Outside) {
			Vector3f pos = this.getWorldTranslation().add(0, SoldierModel.MODEL_HEIGHT + 0.1f , 0);
			Vector3f screen_pos = cam.getScreenCoordinates(pos);
			this.hudNode.setLocalTranslation(screen_pos.x, screen_pos.y, 0);
		}
		
	}


	@Override
	public Node getHUDItem() {
		return this.hudNode;
	}

}

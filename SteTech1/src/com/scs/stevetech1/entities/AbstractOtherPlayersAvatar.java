package com.scs.stevetech1.entities;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Box;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.IAffectedByPhysics;
import com.scs.stevetech1.components.IAnimatedClientSide;
import com.scs.stevetech1.components.IAvatarModel;
import com.scs.stevetech1.components.IDontCollideWithComrades;
import com.scs.stevetech1.components.IDrawOnHUD;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.components.ISetRotation;
import com.scs.stevetech1.jme.JMEAngleFunctions;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

/*
 * This is only used client-side, and represents another player's avatar.
 */
public abstract class AbstractOtherPlayersAvatar extends PhysicalEntity implements IAffectedByPhysics, IAnimatedClientSide, IProcessByClient, 
ISetRotation, IDrawOnHUD, IDontCollideWithComrades { 

	protected IAvatarModel model;
	private String playersName;
	protected byte side;
	private Node container;

	// HUD
	protected BitmapText bmpText;
	private static BitmapFont font_small;

	public AbstractOtherPlayersAvatar(IEntityController game, int type, int eid, float x, float y, float z, IAvatarModel _model, byte _side, String _playersName) {
		super(game, eid, type, "EnemyAvatar", true, false, true);

		model = _model;
		playersName = _playersName;
		side = _side;

		// Create box for collisions
		Box box = new Box(model.getCollisionBoxSize().x/2, model.getCollisionBoxSize().y/2, model.getCollisionBoxSize().z/2);
		Geometry bbGeom = new Geometry("bbGeom_" + entityName, box);
		bbGeom.setLocalTranslation(0, box.getYExtent(), 0); // origin is centre!
		bbGeom.setCullHint(CullHint.Always); // Don't draw the collision box
		this.mainNode.attachChild(bbGeom);

		bbGeom.setUserData(Globals.ENTITY, this);

		// Create model to look good
		Spatial avatarModel = model.createAndGetModel();

		// Contain model in a separate node so we can rotate it without losing the models own rotation
		container = new Node();
		container.attachChild(avatarModel); 
		game.getGameNode().attachChild(container);

		this.setWorldTranslation(new Vector3f(x, y, z));

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), false, this);
		simpleRigidBody.setGravity(0); // So they move exactly where we want, even when client jumps

		font_small = game.getAssetManager().loadFont("Interface/Fonts/Console.fnt");
		bmpText = new BitmapText(font_small);
		bmpText.setText(playersName);
	}


	@Override
	public void processByClient(IClientApp client, float tpf_secs) {
		// Set position and direction of avatar model, which doesn't get moved automatically
		this.container.setLocalTranslation(this.getWorldTranslation());
	}


	@Override
	public void remove() {
		super.remove();

		this.container.removeFromParent();
	}


	@Override
	public void setRotation(Vector3f dir) {
		Vector3f dir2 = new Vector3f(dir.x, 0, dir.z); 
		JMEAngleFunctions.rotateToWorldDirection(container, dir2);
	}


	@Override
	public Node getHUDItem() {
		return this.bmpText;
	}


	@Override
	public void drawOnHud(Node hud, Camera cam) {
		super.checkHUDNode(hud, bmpText, cam, 2f, model.getCollisionBoxSize().y);
	}


	@Override
	public byte getSide() {
		return side;
	}


	@Override
	public void setAnimCode_ClientSide(int animCode) {
	}


	@Override
	public void processManualAnimation_ClientSide(float tpf_secs) {

	}


}

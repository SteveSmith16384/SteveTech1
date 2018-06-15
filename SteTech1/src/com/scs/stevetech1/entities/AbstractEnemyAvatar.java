package com.scs.stevetech1.entities;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.Camera.FrustumIntersect;
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
import com.scs.stevetech1.components.IDrawOnHUD;
import com.scs.stevetech1.components.IDontCollideWithComrades;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.components.ISetRotation;
import com.scs.stevetech1.jme.JMEAngleFunctions;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

/*
 * This is only used client-side.
 */
public abstract class AbstractEnemyAvatar extends PhysicalEntity implements IAffectedByPhysics, IAnimatedClientSide, IProcessByClient, 
ISetRotation, IDrawOnHUD, IDontCollideWithComrades { 

	protected IAvatarModel anim;
	private Spatial avatarModel;
	private String playersName;
	protected int side;
	Node container;

	// HUD
	protected BitmapText hudNode;
	private static BitmapFont font_small;

	public AbstractEnemyAvatar(IEntityController game, int type, int eid, float x, float y, float z, IAvatarModel _anim, int _side, String _playersName) {
		super(game, eid, type, "EnemyAvatar", true, false, true);

		anim = _anim;
		playersName = _playersName;
		side = _side;
		
		// Create box for collisions
		Box box = new Box(anim.getSize().x/2, anim.getSize().y/2, anim.getSize().z/2);
		Geometry bbGeom = new Geometry("bbGeom_" + name, box);
		bbGeom.setLocalTranslation(0, anim.getSize().y/2, 0); // origin is centre!
		bbGeom.setCullHint(CullHint.Always); // Don't draw the collision box
		this.mainNode.attachChild(bbGeom);

		bbGeom.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);

		// Create model to look good
		avatarModel = anim.createAndGetModel(side);
		
		// Contain model in a separate node so we can rotate it without losing the models own rotation
		container = new Node();
		container.attachChild(avatarModel); 
		game.getGameNode().attachChild(avatarModel);

		this.setWorldTranslation(new Vector3f(x, y, z));

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), false, this); // scs new - was NOT kinematic
		simpleRigidBody.setGravity(0); // So they move exactly where we want, even when client jumps

		font_small = game.getAssetManager().loadFont("Interface/Fonts/Console.fnt");
		hudNode = new BitmapText(font_small);
		hudNode.setText(name);

	}


	@Override
	public void processByClient(IClientApp client, float tpf_secs) {
		// Set position and direction of avatar model, which doesn't get moved automatically
		//this.avatarModel.setLocalTranslation(this.getWorldTranslation());
		this.container.setLocalTranslation(this.getWorldTranslation());
	}


	@Override
	public void remove() {
		super.remove();

		this.avatarModel.removeFromParent();
	}


	@Override
	public void setRotation(Vector3f dir) {
		Vector3f dir2 = new Vector3f(dir.x, 0, dir.z); 
		JMEAngleFunctions.rotateToDirection(avatarModel, dir2);
	}


	@Override
	public Node getHUDItem() {
		return this.hudNode;
	}


	@Override
	public void drawOnHud(Camera cam) {
		FrustumIntersect insideoutside = cam.contains(this.getMainNode().getWorldBound());
		if (insideoutside != FrustumIntersect.Outside) {
			if (this.hudNode.getText().length() == 0) {
				hudNode.setText(playersName);
			}
			Vector3f pos = this.getWorldTranslation().add(0, anim.getSize().y, 0); // todo - not this every time
			Vector3f screen_pos = cam.getScreenCoordinates(pos);
			this.hudNode.setLocalTranslation(screen_pos.x, screen_pos.y, 0);
		} else {
			this.hudNode.setText(""); // Hide it
		}
	}


	@Override
	public int getSide() {
		return side;
	}

}

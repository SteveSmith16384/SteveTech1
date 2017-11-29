package com.scs.simplephysics.tests;

import java.util.Collection;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.VideoRecorderAppState;
import com.jme3.asset.TextureKey;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.shadow.EdgeFilteringMode;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.simplephysics.ICollisionListener;
import com.scs.simplephysics.SimpleCharacterControl;
import com.scs.simplephysics.SimplePhysicsController;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stetech1.server.Settings;

public class HelloSimplePhysics extends SimpleApplication implements ActionListener, ICollisionListener<Spatial> {

	private SimplePhysicsController<Spatial> physicsController;
	private SimpleCharacterControl<Spatial> player;
	private Vector3f walkDirection = new Vector3f();

	private boolean left = false, right = false, up = false, down = false;
	private Geometry playerModel;
	private final float speed = 8f;
	private final float headHeight = 1f;

	//Temporary vectors used on each frame.
	private Vector3f camDir = new Vector3f();
	private Vector3f camLeft = new Vector3f();
	private DirectionalLight sun;

	public static void main(String[] args) {
		AppSettings settings = new AppSettings(true);
		settings.setSettingsDialogImage(null);
		HelloSimplePhysics app = new HelloSimplePhysics();
		app.settings = settings;
		app.start();
	}


	public void simpleInitApp() {
		physicsController = new SimplePhysicsController<Spatial>(this);
		physicsController.setEnabled(false);
		//physicsController.setBounds(new Vector3f(), new Vector3f(30, 10, 30));

		/** Create a box to use as our player model */
		Box box1 = new Box(.3f, .9f, .3f);
		playerModel = new Geometry("Player", box1);
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");  // create a simple material
		mat.setColor("Color", ColorRGBA.Blue);
		playerModel.setMaterial(mat);
		playerModel.setLocalTranslation(new Vector3f(0,6,0));
		playerModel.setCullHint(CullHint.Always);
		rootNode.attachChild(playerModel);

		cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 0.01f, 1000f);
		cam.setLocation(new Vector3f(0,6,0));

		viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));

		setUpKeys();
		setUpLight();

		player = new SimpleCharacterControl<Spatial>(playerModel, this.physicsController, this.playerModel);
		playerModel.setLocalTranslation(new Vector3f(0,4,0)); 

		this.addFloor();
		this.addWall();
		this.addBox(2f, 8f, 7f, 1f, 1f);
		this.addBox(2f, 6f, 7f, 1f, 1f);
		//this.addBall(10, 6, 10, .2f, new Vector3f(-3f, 0f, 0f), SimpleRigidBody.DEF_GRAVITY, SimpleRigidBody.DEF_AIR_FRICTION, 0.2f); // Bouncing ball
		//this.addBall(12, 6, 12, .2f, new Vector3f(0, -6f, -6f), 0, 1, 1); // Plasma ball

		final int SHADOWMAP_SIZE = 1024;
		DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(getAssetManager(), SHADOWMAP_SIZE, 2);
		//dlsr.setShadowIntensity(1f);
		//dlsr.setShadowZFadeLength(10f);
		dlsr.setLight(sun);
		this.viewPort.addProcessor(dlsr);

		/*DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(assetManager, SHADOWMAP_SIZE, 3);
        dlsr.setLight(sun);
        dlsr.setLambda(0.55f);
        dlsr.setShadowIntensity(0.8f);
        dlsr.setEdgeFilteringMode(EdgeFilteringMode.Nearest);
        //dlsr.displayDebug();
        viewPort.addProcessor(dlsr);

        /*DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(assetManager, SHADOWMAP_SIZE, 3);
        dlsf.setLight(sun);
        dlsf.setLambda(0.55f);
        dlsf.setShadowIntensity(0.8f);
        dlsf.setEdgeFilteringMode(EdgeFilteringMode.Nearest);
        dlsf.setEnabled(false);

        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        fpp.addFilter(dlsf);

        viewPort.addProcessor(fpp);*/

        /*Settings.p("Recording video");
		VideoRecorderAppState video_recorder = new VideoRecorderAppState();
		stateManager.attach(video_recorder);
*/
	}


	/** Make a solid floor and add it to the scene. */
	public void addFloor() {
		Box floor = new Box(30f, 0.1f, 30f);
		floor.scaleTextureCoordinates(new Vector2f(3, 6));

		Material floor_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		TextureKey key3 = new TextureKey("Textures/grass.jpg");
		key3.setGenerateMips(true);
		Texture tex3 = assetManager.loadTexture(key3);
		tex3.setWrap(WrapMode.Repeat);
		floor_mat.setTexture("ColorMap", tex3);

		Geometry floor_geo = new Geometry("Floor", floor);
		floor_geo.setMaterial(floor_mat);
		floor_geo.setLocalTranslation(0, -0.1f, 0);
		floor_geo.setShadowMode(ShadowMode.Receive);
		this.rootNode.attachChild(floor_geo);

		SimpleRigidBody<Spatial> srb = new SimpleRigidBody<Spatial>(floor_geo, physicsController, floor_geo);
		srb.setMovable(false);
	}


	public void addWall() {
		Box floor = new Box(5f, 5f, .1f);
		//floor.scaleTextureCoordinates(new Vector2f(3, 6));

		Material floor_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		TextureKey key3 = new TextureKey("Textures/seamless_bricks/bricks2.png");
		key3.setGenerateMips(true);
		Texture tex3 = assetManager.loadTexture(key3);
		tex3.setWrap(WrapMode.Repeat);
		floor_mat.setTexture("ColorMap", tex3);

		Geometry floor_geo = new Geometry("Wall", floor);
		floor_geo.setMaterial(floor_mat);
		floor_geo.setLocalTranslation(3, 2.5f, 20);
		floor_geo.setShadowMode(ShadowMode.CastAndReceive);
		this.rootNode.attachChild(floor_geo);

		SimpleRigidBody<Spatial> srb = new SimpleRigidBody<Spatial>(floor_geo, physicsController, floor_geo);
		srb.setMovable(false);
	}


	public void addBox(float x, float y, float z, float w, float h) {
		Box box = new Box(w/2, h/2, w/2);
		//box.scaleTextureCoordinates(new Vector2f(3, 6));

		Material floor_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		TextureKey key3 = new TextureKey("Textures/crate.png");
		//floor_mat.setTexture("ColorMap", assetManager.loadTexture("Textures/Terrain/BrickWall/BrickWall.jpg"));
		key3.setGenerateMips(true);
		Texture tex3 = assetManager.loadTexture(key3);
		tex3.setWrap(WrapMode.Repeat);
		floor_mat.setTexture("ColorMap", tex3);

		Geometry box_geo = new Geometry("Box", box);
		box_geo.setMaterial(floor_mat);
		box_geo.setLocalTranslation(x, y, z);
		box_geo.setShadowMode(ShadowMode.CastAndReceive);
		this.rootNode.attachChild(box_geo);

		SimpleRigidBody<Spatial> srb = new SimpleRigidBody<Spatial>(box_geo, physicsController, box_geo);
	}


	public void addBall(float x, float y, float z, float rad, Vector3f dir, float grav, float airRes, float bounce) {
		Sphere sphere = new Sphere(16, 16, rad);

		Material floor_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		TextureKey key3 = new TextureKey("Textures/football.jpg");
		key3.setGenerateMips(true);
		Texture tex3 = assetManager.loadTexture(key3);
		tex3.setWrap(WrapMode.Repeat);
		floor_mat.setTexture("ColorMap", tex3);

		Geometry ball_geo = new Geometry("Sphere", sphere);
		ball_geo.setMaterial(floor_mat);
		ball_geo.setLocalTranslation(x, y, z);
		ball_geo.setShadowMode(ShadowMode.Cast);
		this.rootNode.attachChild(ball_geo);

		SimpleRigidBody<Spatial> srb = new SimpleRigidBody<Spatial>(ball_geo, physicsController, ball_geo);
		srb.setLinearVelocity(dir);
		srb.setGravity(grav);
		srb.setAerodynamicness(airRes);
		srb.setBounciness(bounce);
	}


	private void setUpLight() {
		// Remove existing lights
		getRootNode().getWorldLightList().clear();
		getRootNode().getLocalLightList().clear();

		// We add light so we see the scene
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(.2f));
		rootNode.addLight(al);

		sun = new DirectionalLight();
		sun.setColor(ColorRGBA.Yellow);
		//sun.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
		sun.setDirection(new Vector3f(0, -1f, 0).normalizeLocal());
		rootNode.addLight(sun);
	}


	private void setUpKeys() {
		inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
		inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
		inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
		inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
		inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
		inputManager.addListener(this, "Left");
		inputManager.addListener(this, "Right");
		inputManager.addListener(this, "Up");
		inputManager.addListener(this, "Down");
		inputManager.addListener(this, "Jump");

		inputManager.addMapping("Shoot0", new MouseButtonTrigger(0));
		inputManager.addListener(this, "Shoot0");
		inputManager.addMapping("Shoot1", new MouseButtonTrigger(1));
		inputManager.addListener(this, "Shoot1");

		inputManager.addMapping("Blow", new KeyTrigger(KeyInput.KEY_B));
		inputManager.addListener(this, "Blow");

		inputManager.addMapping("Start", new KeyTrigger(KeyInput.KEY_T));
		inputManager.addListener(this, "Start");
	}


	/** These are our custom actions triggered by key presses.
	 * We do not walk yet, we just keep track of the direction the user pressed. */
	public void onAction(String binding, boolean isPressed, float tpf) {
		if (binding.equals("Left")) {
			left = isPressed;
		} else if (binding.equals("Right")) {
			right= isPressed;
		} else if (binding.equals("Up")) {
			up = isPressed;
		} else if (binding.equals("Down")) {
			down = isPressed;
		} else if (binding.equals("Jump")) {
			if (isPressed) { 
				player.jump(); 
			}
		} else if (binding.equals("Shoot0")) {
			if (isPressed) { 
				Vector3f startPos = new Vector3f(cam.getLocation());
				startPos.addLocal(cam.getDirection().mult(2));
				this.addBall(startPos.x, startPos.y, startPos.z, .2f, this.cam.getDirection().mult(25f), SimpleRigidBody.DEFAULT_GRAVITY, SimpleRigidBody.DEFAULT_AERODYNAMICNESS, 0.4f); // Bouncing ball
			}
		} else if (binding.equals("Shoot1")) {
			if (isPressed) { 
				Vector3f startPos = new Vector3f(cam.getLocation());
				startPos.addLocal(cam.getDirection().mult(4));
				this.addBall(startPos.x, startPos.y, startPos.z, .2f, this.cam.getDirection().mult(35f), 0, 1, 0.2f); // Laser ball
			}
		} else if (binding.equals("Start")) {
			if (isPressed) { 
				physicsController.setEnabled(true);
			}
		} else if (binding.equals("Blow")) {
			if (isPressed) {
				blow();
			}
		}
	}


	private void blow() {
		Collection<SimpleRigidBody<Spatial>> entities = physicsController.getEntities();
		synchronized (entities) {
			// Loop through the entities
			for(SimpleRigidBody<Spatial> e : entities) {
				e.getLinearVelocity().addLocal(5, 0, 0);
			}
		}

	}

	
	/**
	 * This is the main event loop--walking happens here.
	 * We check in which direction the player is walking by interpreting
	 * the camera direction forward (camDir) and to the side (camLeft).
	 * The setWalkDirection() command is what lets a physics-controlled player walk.
	 * We also make sure here that the camera moves with player.
	 */
	@Override
	public void simpleUpdate(float tpf_secs) {
		/*
		 * The direction of character is determined by the camera angle
		 * the Y direction is set to zero to keep our character from
		 * lifting of terrain. For free flying games simply add speed 
		 * to Y axis
		 */
		camDir.set(cam.getDirection()).multLocal(speed, 0.0f, speed);
		camLeft.set(cam.getLeft()).multLocal(speed);
		walkDirection.set(0, 0, 0);
		if (left) {
			walkDirection.addLocal(camLeft);
		}
		if (right) {
			walkDirection.addLocal(camLeft.negate());
		}
		if (up) {
			walkDirection.addLocal(camDir);
		}
		if (down) {
			walkDirection.addLocal(camDir.negate());
		} 
		walkDirection.y = 0; // Prevent us walking up or down
		player.getAdditionalForce().set(walkDirection);

		this.physicsController.update(tpf_secs);

		cam.setLocation(new Vector3f(playerModel.getLocalTranslation().x, playerModel.getLocalTranslation().y + headHeight, playerModel.getLocalTranslation().z));

		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	public static void p(String s) {
		//System.out.println(System.currentTimeMillis() + ": " + s);
		System.out.println(s);
	}


	@Override
	public void collisionOccurred(SimpleRigidBody<Spatial> a, SimpleRigidBody<Spatial> b, Vector3f point) {
		//System.out.println("Collision between " + a.userObject + " and " + b.userObject);

	}


	public boolean canCollide(SimpleRigidBody<Spatial> a, SimpleRigidBody<Spatial> b) {
		return true;
	}
}

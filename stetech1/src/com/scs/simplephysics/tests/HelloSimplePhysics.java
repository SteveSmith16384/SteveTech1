package com.scs.simplephysics.tests;

import java.util.Collection;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.asset.plugins.FileLocator;
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
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.simplephysics.ICollisionListener;
import com.scs.simplephysics.ISimpleEntity;
import com.scs.simplephysics.SimpleCharacterControl;
import com.scs.simplephysics.SimplePhysicsController;
import com.scs.simplephysics.SimpleRigidBody;

/*
 * An example of using Simple Physics.  
 * Walk around with WASD.  
 * Left mouse button launches a normal bouncing ball, right mouse button launches a "floating" ball unaffected by gravity or friction.
 * T to turn physics on/off
 * B to blow a wind.
 * 
 */
public class HelloSimplePhysics extends SimpleApplication implements ActionListener, ICollisionListener<Spatial> {

	private SimplePhysicsController<Spatial> physicsController;
	private SimpleCharacterControl<Spatial> player;
	private final Vector3f walkDirection = new Vector3f();

	private boolean left = false, right = false, up = false, down = false, jump = false;
	private Geometry playerModel;
	private final float playerSpeed = 8f;
	private final float headHeight = 1f;
	private DirectionalLight sun;

	private Vector3f camDir = new Vector3f();
	private Vector3f camLeft = new Vector3f();

	public static void main(String[] args) {
		AppSettings settings = new AppSettings(true);
		settings.setSettingsDialogImage(null);
		HelloSimplePhysics app = new HelloSimplePhysics();
		app.settings = settings;
		app.start();
	}


	public void simpleInitApp() {
		assetManager.registerLocator("assets/", FileLocator.class); // default
		assetManager.registerLocator("assets/", ClasspathLocator.class);

		cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 0.01f, 1000f);
		cam.lookAt(new Vector3f(3, 1f, 20f), Vector3f.UNIT_Y);
		viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));

		physicsController = new SimplePhysicsController<Spatial>(this, 5, 2);
		setUpKeys();


		Box playerBox = new Box(.3f, .9f, .3f);
		playerModel = new Geometry("Player", playerBox);
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.setColor("Color", ColorRGBA.Blue);
		playerModel.setMaterial(mat);
		playerModel.setLocalTranslation(new Vector3f(0,6,0));
		playerModel.setCullHint(CullHint.Always);
		rootNode.attachChild(playerModel);
		
		ISimpleEntity<Spatial> iePlayer = new ISimpleEntity<Spatial>() {
			@Override
			public Spatial getSpatial() {
				return playerModel;
			}

			@Override
			public void hasMoved() {
				// Do nothing
			}
		};

		// Setup the scene
		setUpLight();

		player = new SimpleCharacterControl<Spatial>(iePlayer, this.physicsController, this.playerModel);
		this.physicsController.addSimpleRigidBody(player);
		playerModel.setLocalTranslation(new Vector3f(1f, 4f, 1f)); 

		this.addFloor();
		this.addWall();
		this.addBox(2f, 8f, 7f, 1f, 1f, .1f);
		this.addBox(2f, 6f, 7f, 1f, 1f, .3f);

		// Add boxes with various states of bounciness
		for (int i=0 ; i<10 ; i++) {
			this.addBox(4f+(i*2), 7f, 9f, 1f, 1f, (i/10f));
		}

		addMountainModel();
		
		// Add shadows
		final int SHADOWMAP_SIZE = 1024;
		DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(getAssetManager(), SHADOWMAP_SIZE, 2);
		dlsr.setLight(sun);
		this.viewPort.addProcessor(dlsr);
		/*
		p("Recording video");
		VideoRecorderAppState video_recorder = new VideoRecorderAppState();
		stateManager.attach(video_recorder);
		 */
	}


	
	private void addMountainModel() {
		// Add a model
		final Spatial model = getAssetManager().loadModel("Models/Holiday/Terrain.blend");
		model.setLocalTranslation(13, 0, 13);
		model.setShadowMode(ShadowMode.CastAndReceive);
		this.rootNode.attachChild(model);

		ISimpleEntity<Spatial> hillEntity = new ISimpleEntity<Spatial>() {
			@Override
			public Spatial getSpatial() {
				return model;
			}

			@Override
			public void hasMoved() {
				// Do nothing
			}
		};

		SimpleRigidBody<Spatial> srb = new SimpleRigidBody<Spatial>(hillEntity, physicsController, false, model);		
		this.physicsController.addSimpleRigidBody(srb);
		srb.setModelComplexity(1);
	}
	
	
	private void addFloor() {
		Box floor = new Box(30f, 0.1f, 30f);
		floor.scaleTextureCoordinates(new Vector2f(3, 6));

		//Material floorMaterial = new Material(getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
		Material floorMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		TextureKey key3 = new TextureKey("Textures/grass.jpg");
		key3.setGenerateMips(true);
		Texture tex3 = assetManager.loadTexture(key3);
		tex3.setWrap(WrapMode.Repeat);
		floorMaterial.setTexture("ColorMap", tex3);

		final Geometry floorGeometry = new Geometry("Floor", floor);
		floorGeometry.setMaterial(floorMaterial);
		floorGeometry.setLocalTranslation(0, -0.1f, 0);
		floorGeometry.setShadowMode(ShadowMode.Receive);
		this.rootNode.attachChild(floorGeometry);

		ISimpleEntity<Spatial> floorEntity = new ISimpleEntity<Spatial>() {
			@Override
			public Spatial getSpatial() {
				return floorGeometry;
			}

			@Override
			public void hasMoved() {
				// Do nothing
			}
		};

		SimpleRigidBody<Spatial> srb = new SimpleRigidBody<Spatial>(floorEntity, physicsController, false, floorGeometry);
		this.physicsController.addSimpleRigidBody(srb);
	}


	private void addWall() {
		Box floor = new Box(5f, 5f, .1f);

		Material wallMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		TextureKey key3 = new TextureKey("Textures/bricks.jpg");
		key3.setGenerateMips(true);
		Texture tex3 = assetManager.loadTexture(key3);
		tex3.setWrap(WrapMode.Repeat);
		wallMaterial.setTexture("ColorMap", tex3);

		final Geometry wallGeometry = new Geometry("Wall", floor);
		wallGeometry.setMaterial(wallMaterial);
		wallGeometry.setLocalTranslation(3, 2.5f, 20);
		wallGeometry.setShadowMode(ShadowMode.CastAndReceive);
		this.rootNode.attachChild(wallGeometry);

		ISimpleEntity<Spatial> wallEntity = new ISimpleEntity<Spatial>() {
			@Override
			public Spatial getSpatial() {
				return wallGeometry;
			}

			@Override
			public void hasMoved() {
				// Do nothing
			}
		};

		SimpleRigidBody<Spatial> srb = new SimpleRigidBody<Spatial>(wallEntity, physicsController, false, wallGeometry);
		this.physicsController.addSimpleRigidBody(srb);
	}


	private void addBox(float x, float y, float z, float w, float h, float bounciness) {
		Box box = new Box(w/2, h/2, w/2);

		//Material material = new Material(getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
		Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		TextureKey key3 = new TextureKey("Textures/crate.png");
		key3.setGenerateMips(true);
		Texture tex3 = assetManager.loadTexture(key3);
		tex3.setWrap(WrapMode.Repeat);
		material.setTexture("ColorMap", tex3);

		final Geometry boxGeometry = new Geometry("Box", box);
		boxGeometry.setMaterial(material);
		boxGeometry.setLocalTranslation(x, y, z);
		boxGeometry.setShadowMode(ShadowMode.CastAndReceive);
		boxGeometry.lookAt(new Vector3f(0, y, 0), Vector3f.UNIT_Y); // Rotate them to different angles
		this.rootNode.attachChild(boxGeometry);

		ISimpleEntity<Spatial> boxEntity = new ISimpleEntity<Spatial>() {
			@Override
			public Spatial getSpatial() {
				return boxGeometry;
			}

			@Override
			public void hasMoved() {
				// Do nothing
			}
		};

		SimpleRigidBody<Spatial> srb = new SimpleRigidBody<Spatial>(boxEntity, physicsController, true, boxGeometry);
		this.physicsController.addSimpleRigidBody(srb);
		srb.setBounciness(bounciness);
	}


	private void addBall(float x, float y, float z, float rad, Vector3f dir, float grav, float airRes, float bounce) {
		Sphere sphere = new Sphere(16, 16, rad);

		Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		TextureKey key3 = new TextureKey("Textures/football.jpg");
		key3.setGenerateMips(true);
		Texture tex3 = assetManager.loadTexture(key3);
		tex3.setWrap(WrapMode.Repeat);
		material.setTexture("ColorMap", tex3);

		final Geometry ballGeometry = new Geometry("Sphere", sphere);
		ballGeometry.setMaterial(material);
		ballGeometry.setLocalTranslation(x, y, z);
		ballGeometry.setShadowMode(ShadowMode.Cast);
		this.rootNode.attachChild(ballGeometry);

		ISimpleEntity<Spatial> ballEntity = new ISimpleEntity<Spatial>() {
			@Override
			public Spatial getSpatial() {
				return ballGeometry;
			}

			@Override
			public void hasMoved() {
				// Do nothing
			}
		};

		SimpleRigidBody<Spatial> srb = new SimpleRigidBody<Spatial>(ballEntity, physicsController, true, ballGeometry);
		srb.setLinearVelocity(dir);
		srb.setGravity(grav);
		srb.setAerodynamicness(airRes);
		srb.setBounciness(bounce);
		this.physicsController.addSimpleRigidBody(srb);
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
		sun.setDirection(new Vector3f(.5f, -1f, .5f).normalizeLocal());
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

		inputManager.addMapping("TogglePhysics", new KeyTrigger(KeyInput.KEY_T));
		inputManager.addListener(this, "TogglePhysics");
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
			jump = isPressed;
		} else if (binding.equals("Shoot0")) {
			if (isPressed) { 
				// todo - don't do this is input thread
				Vector3f startPos = new Vector3f(cam.getLocation());
				startPos.addLocal(cam.getDirection().mult(2));
				this.addBall(startPos.x, startPos.y, startPos.z, .2f, this.cam.getDirection().mult(25f), physicsController.getGravity(), physicsController.getAerodynamicness(), 0.4f); // Bouncing ball
			}
		} else if (binding.equals("Shoot1")) {
			if (isPressed) {
				// todo - don't do this is input thread
				Vector3f startPos = new Vector3f(cam.getLocation());
				startPos.addLocal(cam.getDirection().mult(4));
				this.addBall(startPos.x, startPos.y, startPos.z, .2f, this.cam.getDirection().mult(35f), 0, 1, 0.2f); // "Laser" ball
			}
		} else if (binding.equals("TogglePhysics")) {
			if (isPressed) { 
				// Toggle enabled
				physicsController.setEnabled(!physicsController.getEnabled());
			}
		} else if (binding.equals("Blow")) {
			if (isPressed) {
				blowEntities();
			}
		}
	}


	private void blowEntities() {
		Collection<SimpleRigidBody<Spatial>> entities = physicsController.getEntities();
		synchronized (entities) {
			// Loop through the entities
			for(SimpleRigidBody<Spatial> e : entities) {
				e.getLinearVelocity().addLocal(4, .5f, 0);
			}
		}
	}


	@Override
	public void simpleUpdate(float tpf_secs) {
		camDir.set(cam.getDirection()).multLocal(playerSpeed, 0.0f, playerSpeed);
		camLeft.set(cam.getLeft()).multLocal(playerSpeed);
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
		if (jump) {
			player.jump();
		}
		walkDirection.y = 0; // Prevent us walking up or down
		player.getAdditionalForce().set(walkDirection);
		//p("Walk dir:" + walkDirection);

		this.physicsController.update(tpf_secs);
		
		// Position the camera
		cam.setLocation(new Vector3f(playerModel.getLocalTranslation().x, playerModel.getLocalTranslation().y + headHeight, playerModel.getLocalTranslation().z));

		//--------------------------------------------

		try {
			Thread.sleep(5); // If the FPS is waaayyy to high (i.e. > 1000 FPS), things get a bit crazy, caused by floating point rounding
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}


	@Override
	public void collisionOccurred(SimpleRigidBody<Spatial> a, SimpleRigidBody<Spatial> b, Vector3f point) {
		//p("Collision between " + a.userObject + " and " + b.userObject);

	}


	public boolean canCollide(SimpleRigidBody<Spatial> a, SimpleRigidBody<Spatial> b) {
		return true;
	}


	public static void p(String s) {
		System.out.println(s);
	}


}

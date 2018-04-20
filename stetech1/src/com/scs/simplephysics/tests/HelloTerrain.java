package com.scs.simplephysics.tests;

import java.util.Collection;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.collision.CollisionResults;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.system.AppSettings;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.simplephysics.ICollisionListener;
import com.scs.simplephysics.ISimpleEntity;
import com.scs.simplephysics.SimpleCharacterControl;
import com.scs.simplephysics.SimplePhysicsController;
import com.scs.simplephysics.SimpleRigidBody;

public class HelloTerrain extends SimpleApplication implements ActionListener, ICollisionListener<Spatial> {

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
		HelloTerrain app = new HelloTerrain();
		app.settings = settings;
		app.start();
	}


	public void simpleInitApp() {
		assetManager.registerLocator("assets/", FileLocator.class); // default
		assetManager.registerLocator("assets/", ClasspathLocator.class);

		cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 0.01f, 1000f);
		cam.lookAt(new Vector3f(3, 1f, 20f), Vector3f.UNIT_Y);
		viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));

		physicsController = new SimplePhysicsController<Spatial>(this, 5);
		setUpKeys();


		Box playerBox = new Box(.3f, .9f, .3f);
		playerModel = new Geometry("Player", playerBox);
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.setColor("Color", ColorRGBA.Blue);
		playerModel.setMaterial(mat);
		playerModel.setLocalTranslation(new Vector3f(0,6,0));
		playerModel.setCullHint(CullHint.Always); // Don't draw ourselves
		rootNode.attachChild(playerModel);
		
		ISimpleEntity<Spatial> iePlayer = new SimpleEntityHelper<Spatial>(playerModel);

		// Setup the scene
		setUpLight();

		TerrainQuad t = this.addTerrain();

		// Get player start pos
		Ray r = new Ray(new Vector3f(100, 255, 100), new Vector3f(0, -1, 0));
		CollisionResults crs = new CollisionResults();
		t.collideWith(r, crs);
		Vector3f pos = crs.getClosestCollision().getContactPoint();

		player = new SimpleCharacterControl<Spatial>(iePlayer, this.physicsController, this.playerModel);
		this.physicsController.addSimpleRigidBody(player);
		playerModel.setLocalTranslation(pos.addLocal(0,  10,  0)); 

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


	
	private TerrainQuad addTerrain() {
		/** 1. Create terrain material and load four textures into it. */
		Material mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");

		/** 1.1) Add ALPHA map (for red-blue-green coded splat textures) */
		mat_terrain.setTexture("Alpha", assetManager.loadTexture("Textures/Terrain/splat/alphamap.png"));

		/** 1.2) Add GRASS texture into the red layer (Tex1). */
		//Texture grass = game.getAssetManager().loadTexture("Textures/Terrain/splat/grass.jpg");
		Texture grass = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
		//Texture grass = game.getAssetManager().loadTexture("Textures/Terrain/splat/grass16x16.png");
		grass.setWrap(WrapMode.Repeat);
		mat_terrain.setTexture("Tex1", grass);
		mat_terrain.setFloat("Tex1Scale", 64f);
		//mat_terrain.setFloat("Tex1Scale", 2f);

		/** 1.3) Add DIRT texture into the green layer (Tex2) */
		//Texture dirt = game.getAssetManager().loadTexture("Textures/Terrain/splat/dirt.jpg");
		Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/grass_new.png");
		dirt.setWrap(WrapMode.Repeat);
		mat_terrain.setTexture("Tex2", dirt);
		//mat_terrain.setFloat("Tex2Scale", 32f);
		//mat_terrain.setFloat("Tex2Scale", 2f);
		mat_terrain.setFloat("Tex2Scale", 256f);

		/** 1.4) Add ROAD texture into the blue layer (Tex3) */
		Texture rock = assetManager.loadTexture("Textures/Terrain/splat/road.jpg");
		rock.setWrap(WrapMode.Repeat);
		mat_terrain.setTexture("Tex3", rock);
		mat_terrain.setFloat("Tex3Scale", 128f);

		/** 2. Create the height map */
		Texture heightMapImage = assetManager.loadTexture("Textures/Terrain/splat/mountains512.png");
		AbstractHeightMap heightmap = new ImageBasedHeightMap(heightMapImage.getImage());
		heightmap.load();

		int initialSize = 513;
		// raise edges
		/*for (int i=0 ; i<initialSize ; i++) {
			heightmap.setHeightAtPoint(255, 0,  i);
			heightmap.setHeightAtPoint(255, i,  0);
			heightmap.setHeightAtPoint(255, initialSize-1,  i);
			heightmap.setHeightAtPoint(255, i, initialSize-1);
		}*/
		
		/** 3. We have prepared material and heightmap.
		 * Now we create the actual terrain:
		 * 3.1) Create a TerrainQuad and name it "my terrain".
		 * 3.2) A good value for terrain tiles is 64x64 -- so we supply 64+1=65.
		 * 3.3) We prepared a heightmap of size 512x512 -- so we supply 512+1=513.
		 * 3.4) As LOD step scale we supply Vector3f(1,1,1).
		 * 3.5) We supply the prepared heightmap itself.
		 */
		int patchSize = 65;
		TerrainQuad terrain = new TerrainQuad("my terrain", patchSize, initialSize, heightmap.getHeightMap());

		/** 4. We give the terrain its material, position & scale it, and attach it. */
		terrain.setMaterial(mat_terrain);
		ISimpleEntity<Spatial> floorEntity = new SimpleEntityHelper<Spatial>(terrain);

		SimpleRigidBody<Spatial> srb = new SimpleRigidBody<Spatial>(floorEntity, physicsController, false, terrain);
		this.physicsController.addSimpleRigidBody(srb);

		terrain.setShadowMode(ShadowMode.CastAndReceive);
		this.rootNode.attachChild(terrain);

		return terrain;
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

		ISimpleEntity<Spatial> ballEntity = new SimpleEntityHelper<Spatial>(ballGeometry);

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
				this.addBall(startPos.x, startPos.y, startPos.z, .1f, this.cam.getDirection().mult(25f), physicsController.getGravity(), physicsController.getAerodynamicness(), 0.4f); // Bouncing ball
			}
		} else if (binding.equals("Shoot1")) {
			if (isPressed) {
				// todo - don't do this is input thread
				Vector3f startPos = new Vector3f(cam.getLocation());
				startPos.addLocal(cam.getDirection().mult(4));
				this.addBall(startPos.x, startPos.y, startPos.z, .3f, this.cam.getDirection().mult(35f), 0, 1, 0.2f); // "Laser" ball
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
		
		// todo - remove balls that fall off edge

		//--------------------------------------------

		try {
			Thread.sleep(5); // If the FPS is waaayyy to high (i.e. > 1000 FPS), things get a bit crazy, caused by floating point rounding
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}


	@Override
	public void collisionOccurred(SimpleRigidBody<Spatial> a, SimpleRigidBody<Spatial> b) {
		//p("Collision between " + a.userObject + " and " + b.userObject);
	}


	public boolean canCollide(SimpleRigidBody<Spatial> a, SimpleRigidBody<Spatial> b) {
		return true;
	}


	public static void p(String s) {
		System.out.println(s);
	}


}

package com.scs.simplephysics.tests;

// THIS CLASS DOES WORK - But requires the Blocky voxel library.  
// However, so does any project which uses voxels and SteveTech, which creates a circular reference.

/*
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.collision.Collidable;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.system.AppSettings;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.scs.simplephysics.ICollisionListener;
import com.scs.simplephysics.ISimpleEntity;
import com.scs.simplephysics.SimpleCharacterControl;
import com.scs.simplephysics.SimplePhysicsController;
import com.scs.simplephysics.SimpleRigidBody;

import mygame.BlockSettings;
import mygame.blocks.BlockTerrainControl;
import mygame.blocks.ChunkControl;
import mygame.blocks.IBlockTerrainListener;
import mygame.blocktypes.DirtBlock;
import mygame.blocktypes.StoneBlock;
import mygame.util.Vector3Int;

public class HelloVoxels extends SimpleApplication implements ActionListener, ICollisionListener<Spatial> {

	private static final boolean FLYING = false;
	
	private static final String SRB_KEY = "srb";
	
	private SimplePhysicsController<Spatial> physicsController;
	private SimpleCharacterControl<Spatial> player;
	private final Vector3f walkDirection = new Vector3f();

	private boolean left = false, right = false, up = false, down = false, jump = false;
	private Geometry playerModel;
	private final float playerSpeed = 8f;
	private final float headHeight = 1f;
	private DirectionalLight sun;

	private BlockTerrainControl blocks;

	private Vector3f camDir = new Vector3f();
	private Vector3f camLeft = new Vector3f();

	public static void main(String[] args) {
		AppSettings settings = new AppSettings(true);
		settings.setSettingsDialogImage(null);
		HelloVoxels app = new HelloVoxels();
		app.settings = settings;
		app.start();
	}


	public void simpleInitApp() {
		cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 0.01f, 1000f);
		cam.lookAt(new Vector3f(3, 1f, 20f), Vector3f.UNIT_Y);
		viewPort.setBackgroundColor(ColorRGBA.Black);

		setUpKeys();

		physicsController = new SimplePhysicsController<Spatial>(this, 5);

		Box playerBox = new Box(.3f, .9f, .3f);
		playerModel = new Geometry("Player", playerBox);
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.setColor("Color", ColorRGBA.Blue);
		playerModel.setMaterial(mat);
		playerModel.setLocalTranslation(new Vector3f(10, 60, 10));
		playerModel.setCullHint(CullHint.Always); // Don't draw ourselves
		rootNode.attachChild(playerModel);
		
		ISimpleEntity<Spatial> iePlayer = new SimpleEntityHelper<Spatial>(playerModel);
		player = new SimpleCharacterControl<Spatial>(iePlayer, this.physicsController, this.playerModel);
		if (FLYING) {
			player.setGravity(0f);
		}
		this.physicsController.addSimpleRigidBody(player);


		// Voxels!
		final BlockSettings blockSettings = new BlockSettings();
		blockSettings.setChunkSize(new Vector3Int(16, 16, 16));
		blockSettings.setBlockSize(2);
		blockSettings.setMaterial(assetManager.loadMaterial("Materials/BlockyTexture.j3m"));
		blockSettings.setWorldSize(new Vector3Int(50, 10, 50));
		blockSettings.setViewDistance(200f);

		blocks = new BlockTerrainControl(blockSettings);
		final Node terrain = new Node();
		terrain.addControl(blocks);

		blocks.registerBlock(new StoneBlock());
		blocks.registerBlock(new DirtBlock());

		ImageBasedHeightMap heightmap = new ImageBasedHeightMap(assetManager.loadTexture("Textures/test500x500.jpg").getImage(), .5f);
		heightmap.load();
		blocks.loadFromHeightMap(new Vector3Int(0, 0, 0), heightmap, StoneBlock.class);		
		
		final SimplePhysicsController controller = this.physicsController;

		blocks.addListener(new IBlockTerrainListener() {

			@Override
			public void onChunkUpdated(ChunkControl c) {
				System.out.println("Chunk updated: " + c);
				Geometry geom = c.getGeometry();
				SimpleRigidBody srb = geom.getUserData(SRB_KEY);
				if (srb == null) {
					ISimpleEntity<Spatial> entity = new ISimpleEntity<Spatial>() {

						@Override
						public void moveEntity(Vector3f pos) {
							// Do nothing
							
						}

						@Override
						public Collidable getCollidable() {
							return geom; // Don't just return a bounding box!
						}

					};

					srb = new SimpleRigidBody(entity, controller, false, geom);
					geom.setUserData(SRB_KEY, srb);
					controller.addSimpleRigidBody(srb);
				}
			}

		});
		
		rootNode.attachChild(terrain);
		
		// End of voxel stuff
		

		setUpLight();

		// Add shadows
		final int SHADOWMAP_SIZE = 1024;
		DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(getAssetManager(), SHADOWMAP_SIZE, 2);
		dlsr.setLight(sun);
		this.viewPort.addProcessor(dlsr);

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
			}
		} else if (binding.equals("Shoot1")) {
			if (isPressed) {
			}
		} else if (binding.equals("TogglePhysics")) {
			if (isPressed) { 
				// Toggle enabled
				physicsController.setEnabled(!physicsController.getEnabled());
			}
		}
	}


	@Override
	public void simpleUpdate(float tpf_secs) {
		camDir.set(cam.getDirection()).multLocal(playerSpeed, 0.0f, playerSpeed);
		//camDir.set(cam.getDirection()).multLocal(playerSpeed, playerSpeed, playerSpeed);
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
		if (!FLYING) {
		walkDirection.y = 0; // Prevent us walking up or down
		}
		player.getAdditionalForce().set(walkDirection);
		//p("Walk dir:" + walkDirection);

		this.physicsController.update(tpf_secs);

		// Position the camera
		cam.setLocation(new Vector3f(playerModel.getLocalTranslation().x, playerModel.getLocalTranslation().y + headHeight, playerModel.getLocalTranslation().z));

		try {
			Thread.sleep(5); // If the FPS is waaayyy too high (i.e. > 1000 FPS), things get a bit crazy, caused by floating point rounding
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}


	@Override
	public void collisionOccurred(SimpleRigidBody<Spatial> a, SimpleRigidBody<Spatial> b) {
		p("Collision between " + a.userObject + " and " + b.userObject);
	}


	public boolean canCollide(SimpleRigidBody<Spatial> a, SimpleRigidBody<Spatial> b) {
		return true;
	}


	public static void p(String s) {
		System.out.println(s);
	}


}
*/
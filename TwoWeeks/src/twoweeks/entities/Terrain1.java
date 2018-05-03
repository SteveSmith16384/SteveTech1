package twoweeks.entities;

import java.util.HashMap;

import com.jme3.bounding.BoundingBox;
import com.jme3.collision.Collidable;
import com.jme3.material.Material;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

import twoweeks.client.TwoWeeksClientEntityCreator;

public class Terrain1 extends PhysicalEntity {

	private TerrainQuad terrain;

	public Terrain1(IEntityController _game, int id, float x, float yTop, float z) {
		super(_game, id, TwoWeeksClientEntityCreator.TERRAIN1, "Terrain1", false, true);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			//creationData.put("pos", new Vector3f(x, yTop, z));
			//creationData.put("size", new Vector3f(w, 0, d));
		}

		/** 1. Create terrain material and load four textures into it. */
		Material mat_terrain = new Material(game.getAssetManager(), "Common/MatDefs/Terrain/Terrain.j3md");

		/** 1.1) Add ALPHA map (for red-blue-green coded splat textures) */
		mat_terrain.setTexture("Alpha", game.getAssetManager().loadTexture("Textures/Terrain/splat/alphamap.png"));

		/** 1.2) Add GRASS texture into the red layer (Tex1). */
		//Texture grass = game.getAssetManager().loadTexture("Textures/Terrain/splat/grass.jpg");
		Texture grass = game.getAssetManager().loadTexture("Textures/Terrain/splat/dirt.jpg");
		//Texture grass = game.getAssetManager().loadTexture("Textures/Terrain/splat/grass16x16.png");
		grass.setWrap(WrapMode.Repeat);
		mat_terrain.setTexture("Tex1", grass);
		mat_terrain.setFloat("Tex1Scale", 64f);
		//mat_terrain.setFloat("Tex1Scale", 2f);

		/** 1.3) Add DIRT texture into the green layer (Tex2) */
		//Texture dirt = game.getAssetManager().loadTexture("Textures/Terrain/splat/dirt.jpg");
		Texture dirt = game.getAssetManager().loadTexture("Textures/Terrain/splat/grass_new.png");
		dirt.setWrap(WrapMode.Repeat);
		mat_terrain.setTexture("Tex2", dirt);
		//mat_terrain.setFloat("Tex2Scale", 32f);
		//mat_terrain.setFloat("Tex2Scale", 2f);
		mat_terrain.setFloat("Tex2Scale", 256f);

		/** 1.4) Add ROAD texture into the blue layer (Tex3) */
		Texture rock = game.getAssetManager().loadTexture("Textures/Terrain/splat/road.jpg");
		rock.setWrap(WrapMode.Repeat);
		mat_terrain.setTexture("Tex3", rock);
		mat_terrain.setFloat("Tex3Scale", 128f);

		/** 2. Create the height map */
		Texture heightMapImage = game.getAssetManager().loadTexture("Textures/Terrain/splat/mountains512.png");
		AbstractHeightMap heightmap = new ImageBasedHeightMap(heightMapImage.getImage());
		heightmap.load();

		int initialSize = 513;
		// raise edges
		for (int i=0 ; i<initialSize ; i++) {
			heightmap.setHeightAtPoint(255, 0,  i);
			heightmap.setHeightAtPoint(255, i,  0);
			heightmap.setHeightAtPoint(255, initialSize-1,  i);
			heightmap.setHeightAtPoint(255, i, initialSize-1);
		}
		
		/** 3. We have prepared material and heightmap.
		 * Now we create the actual terrain:
		 * 3.1) Create a TerrainQuad and name it "my terrain".
		 * 3.2) A good value for terrain tiles is 64x64 -- so we supply 64+1=65.
		 * 3.3) We prepared a heightmap of size 512x512 -- so we supply 512+1=513.
		 * 3.4) As LOD step scale we supply Vector3f(1,1,1).
		 * 3.5) We supply the prepared heightmap itself.
		 */
		int patchSize = 65;
		terrain = new TerrainQuad("my terrain", patchSize, initialSize, heightmap.getHeightMap());

		/** 4. We give the terrain its material, position & scale it, and attach it. */
		terrain.setMaterial(mat_terrain);
		//terrain.setLocalScale(2f, 1f, 2f);
		//terrain.setLocalScale(.5f, .1f, .5f);
		//terrain.setLocalScale(1f, .05f, 1f);
		//terrain.setLocalScale(2f, .2f, 2f);
		//terrain.setLocalScale(5f, .2f, 5f);
		terrain.setLocalScale(5f, .5f, 5f);

		if (!game.isServer()) {
			AbstractGameClient client = (AbstractGameClient)game;
			/** 5. The LOD (level of detail) depends on were the camera is: */
			TerrainLodControl control = new TerrainLodControl(terrain, client.getCamera());
			terrain.addControl(control);
			terrain.setShadowMode(ShadowMode.Receive);

		}
		
		BoundingBox bb = (BoundingBox)terrain.getWorldBound();
		terrain.move(bb.getXExtent(), bb.getYExtent(), bb.getZExtent()); // Move so 0,0,0 is corner

		this.mainNode.attachChild(terrain);
		//terrain.setLocalTranslation((w/2), 0, (d/2)); // Move it into position
		mainNode.setLocalTranslation(x, yTop, z); // Move it into position

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), false, this);
		simpleRigidBody.setNeverMoves(true);

		terrain.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);

	}


	@Override
	public Collidable getCollidable() {
		return this.terrain;
	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		// Do nothing

	}


}

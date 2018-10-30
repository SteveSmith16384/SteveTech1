package com.scs.stevetech1.entities;

import java.util.HashMap;

import com.jme3.collision.Collidable;
import com.jme3.scene.Geometry;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

import mygame.BlockSettings;
import mygame.blocks.BlockTerrainControl;
import mygame.blocks.ChunkControl;
import mygame.blocks.IBlockTerrainListener;
import mygame.blocktypes.DirtBlock;
import mygame.blocktypes.StoneBlock;
import mygame.util.Vector3Int;

public class VoxelTerrainEntity extends PhysicalEntity {

	private BlockTerrainControl blocks;

	public VoxelTerrainEntity(IEntityController _game, int id, int type) {
		super(_game, id, type, "VoxelTerrainEntity", true, true, false);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
		}

		final BlockSettings blockSettings = new BlockSettings();
		blockSettings.setChunkSize(new Vector3Int(16, 16, 16));
		blockSettings.setBlockSize(2);
		blockSettings.setMaterial(game.getAssetManager().loadMaterial("Materials/BlockyTexture.j3m"));
		blockSettings.setWorldSize(new Vector3Int(50, 10, 50));
		blockSettings.setViewDistance(200f);
		
		blocks = new BlockTerrainControl(blockSettings);
		blocks.registerBlock(new StoneBlock());
		blocks.registerBlock(new DirtBlock());

		this.getMainNode().addControl(blocks);

		ImageBasedHeightMap heightmap = new ImageBasedHeightMap(game.getAssetManager().loadTexture("Textures/test500x500.jpg").getImage(), .5f);
		heightmap.load();
		blocks.loadFromHeightMap(new Vector3Int(0, 0, 0), heightmap, StoneBlock.class);		

		final VoxelTerrainEntity vte = this;
		

		blocks.addListener(new IBlockTerrainListener() {

			@Override
			public void onChunkUpdated(ChunkControl c) {
				Globals.p("Chunk added");
				Geometry geom = c.getGeometry();
				SimpleRigidBody<PhysicalEntity> srb = new SimpleRigidBody<PhysicalEntity>(vte, game.getPhysicsController(), false, vte);
				srb.setNeverMoves(true);
				game.getPhysicsController().addSimpleRigidBody(srb); // Todo - don't add immed on client!
			}
		});


		//this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), false, this);
		//simpleRigidBody.setNeverMoves(true);

		this.mainNode.attachChild(blocks.getSpatial());
		//this.getMainNode().setModelBound(new BoundingBox());
	}

	
	@Override
	public Collidable getCollidable() {
		return this.blocks.getSpatial();
	}




}

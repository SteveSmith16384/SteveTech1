package com.scs.moonbaseassault.models;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.control.Control;
import com.jme3.util.BufferUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import java.util.Random;


/**

 *

 * @author Chris

 */

public class StarDust extends Node implements Control
{
	private static final float COLOUR = .6f;
	
	private static final boolean doAlphaBlending = false; // make this false if you dont want alpha blending
	private ArrayList<Vector3f> dustLocations3f; // this houses the locations of all bits of star dust
	private float dustRGBAs[]; // this houses the specific colour information for each bit of dust
	private Mesh dustMesh; // the mesh of vertex data
	private Geometry dustGeometry; // Geometry (the mesh, material, colouring, options)

	private int numDusts;
	private Camera cam;
	private Vector3f currentCamLocation;
	private Vector3f previousCamLocation;
	private float cubedSize;

	private Random random;

	/* StarDust()
	 * Name - of the Node (this)
	 * numDusts - that will appear in the 'cubedSize' surrounding the camera
	 * cubedSize- the length of the virtual cube (in any direction) that the dust will be located in.
	 * cam - the camera on which the cube (and so dust) will always "follow".
	 */
	public StarDust(String name, int numDusts, float cubedSize, Camera cam, AssetManager assetManager)
	{
		// Insert the name of the Node
		super(name);

		// reference some info!
		this.numDusts = numDusts;
		this.cam = cam;
		this.cubedSize = cubedSize / 2; // we divide this figure here to avoid division in the update loop

		// Init the pseudo random number generator

		random = new Random();





		// Since we cant seem to attach this node to the Camera, lets do it another way

		// Grab the current camera location

		currentCamLocation = cam.getLocation();

		previousCamLocation = currentCamLocation.clone();

		// Now set the StarDust node to the Cam location

		this.setLocalTranslation(currentCamLocation);



		// Init the dust locations and colour arrays

		dustLocations3f = new ArrayList<Vector3f>(numDusts);

		dustRGBAs = new float[numDusts * 4]; // * 4 because we need to store 4 values for each star dust





		// Init an initial random distribution of star dust through the virtual cube based on inputs

		for(int dustCount = 0; dustCount < numDusts; dustCount++)

		{

			// Set the inital locations of each bit of dust

			dustLocations3f.add(dustCount, new Vector3f((random.nextFloat() * (this.cubedSize * 2)) + (currentCamLocation.x - this.cubedSize),

					(random.nextFloat() * (this.cubedSize * 2)) + (currentCamLocation.y - this.cubedSize),

					(random.nextFloat() * (this.cubedSize * 2)) + (currentCamLocation.z - this.cubedSize)));

			// Set the initial Colour values (*4 and + to offset 'dustCount' at the correct location in the array)

			dustRGBAs[dustCount*4] = COLOUR; // Red

			dustRGBAs[dustCount*4+1] = COLOUR; // Green

			dustRGBAs[dustCount*4+2] = COLOUR; // Blue

			dustRGBAs[dustCount*4+3] = 1f; // Alpha

		}







		// create the mesh

		dustMesh = new Mesh();



		// Ladies and gentlemen, Positions please!

		FloatBuffer vertices = BufferUtils.createFloatBuffer(dustLocations3f.toArray(new Vector3f[numDusts]));

		dustMesh.setBuffer(VertexBuffer.Type.Position, 3, vertices);



		// Now colours...

		dustMesh.setBuffer(VertexBuffer.Type.Color, 4, dustRGBAs);



		// Set some options

		dustMesh.setMode(Mesh.Mode.Points);

		dustMesh.setPointSize(3f);



		// Dont forget this! ... this solves everything... computer doesnt work, just restart it!

		dustMesh.updateBound();



		// Use the unshaded material type, and we need to define vertex colouring here.

		Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

		material.setBoolean("VertexColor", true);



		// Set alpha blending to use transparent dust when its far away

		material.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);













		dustGeometry = new Geometry("StarDust");

		dustGeometry.setMesh(dustMesh);







		dustGeometry.setMaterial(material);

		dustGeometry.setQueueBucket(Bucket.Sky);







		// attach the spatial to the node

		this.attachChild(dustGeometry);





		// Add the control to the node

		this.addControl(this); // dont know if "Leaking 'this' in constructor" matters.


	}

	public void update(float tpf)

	{



		// House keeping!

		previousCamLocation = currentCamLocation;

		currentCamLocation = cam.getLocation().clone();



		Vector3f differenceVector = currentCamLocation.subtract(previousCamLocation);



		// Move all dust particles based on their current location, and the difference

		// in movement of the camera position.

		for(int dustCount = 0; dustCount < numDusts; dustCount++)

		{



			Vector3f d = dustLocations3f.get(dustCount);



			// calculate where the dust should now be along the X axis

			d.x = d.x - differenceVector.x;

			// Make minor modifications if d.X is now out of bounds

			if(d.x < currentCamLocation.x - cubedSize)

				d.x = d.x + cubedSize * 2;

			else if(d.x > currentCamLocation.x + cubedSize)

				d.x = d.x - cubedSize * 2;



			// calculate where the dust should now be along the Y axis

			d.y = d.y - differenceVector.y;

			// Make minor modifications if d.Y is now out of bounds

			if(d.y < currentCamLocation.y - cubedSize)

				d.y = d.y + cubedSize * 2;

			else if(d.y > currentCamLocation.y + cubedSize)

				d.y = d.y - cubedSize * 2;





			// calculate where the dust should now be along the Z axis

			d.z = d.z - differenceVector.z;

			// Make minor modifications if d.Z is now out of bounds

			if(d.z < currentCamLocation.z - cubedSize)

				d.z = d.z + cubedSize * 2;

			else if(d.z > currentCamLocation.z + cubedSize)

				d.z = d.z - cubedSize * 2;



		}



		dustMesh.clearBuffer(VertexBuffer.Type.Position);

		dustMesh.clearBuffer(VertexBuffer.Type.Color);

		FloatBuffer vertices = BufferUtils.createFloatBuffer(dustLocations3f.toArray(new Vector3f[dustLocations3f.size()]));

		dustMesh.setBuffer(VertexBuffer.Type.Position, 3, vertices);

		dustMesh.setBuffer(VertexBuffer.Type.Color, 4, dustRGBAs);

		dustMesh.updateBound();



		dustGeometry.updateModelBound();











		if(doAlphaBlending)

			updateAlpha(); // update the alpha on all bits of dust..









	}





















	public void updateAlpha()

	{

		// Get the current location of the camera

		Vector3f viewLocation = cam.getLocation();





		// loop through all the points

		for (int i = 0; i < dustLocations3f.size(); i++)

		{

			float distance = viewLocation.distance(dustLocations3f.get(i));



			// once we have the distance to the viewer, we can calculate the new alpha

			float alpha = 0;

			if(distance > (cubedSize * 2)-0.001f) // anything thats a tiny bit less than the distance between the camera and the edge of the star field (or further)

				alpha = 0f; // ... and so now we cant see this one

			else

			{

				alpha = 1f - (distance / cubedSize);

			}



			// only update the alpha value...

			dustRGBAs[i*4+3] = alpha; // Alpha (i*4 value offset, then add another 3 value offset to get to the correct alpha)

		}



		// re-enter the colours for the mesh so they can be displayed.

		dustMesh.clearBuffer(VertexBuffer.Type.Color);

		dustMesh.setBuffer(VertexBuffer.Type.Color, 4, dustRGBAs);



	}





	public Control cloneForSpatial(Spatial spatial) {

		return (Control) spatial;

	}



	public void setSpatial(Spatial spatial) {

	}



	public void setEnabled(boolean enabled) {

	}



	public boolean isEnabled() {

		return true;

	}



	public void render(RenderManager rm, ViewPort vp) {

	}


}








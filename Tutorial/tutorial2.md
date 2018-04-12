## Java Multiplayer FPS Engine Tutorial using SteveTech1

# PART 2

## You want to create your own Realtime Multiplayer FPS?
If you've got here, then you should have an empty project with references to SteveTech1 and jMonkeyEngine.  To demonstrate a simple multiplayer realtime FPS, we're going to create a simple game called "Box Wars".  With SteveTech1, you can create any game you want to; the only limit is your imagination!  With Box Wars, a good imagination will be a bonus since it will consist of just boxes moving around shooting boxes at each other.  Later, we might even add spheres or even some genuine 3D models.

## Boilerplate Stuff
Okay, the first thing to do is create our main client and server classes:-

* Create a class called BoxWarsGameServer, and extend AbstractGameServer
* Create a class called BoxWarsGameClient, and extend AbstractGameClient

Once you've done this, your IDE will probably be creaking under the weight of compile errors, as there will be parameters required for the superconstructors, and lots of abstract methods that require implementing.

(Don't forget that all the source code for this game is included with SteveTech1, so feel free to copy and paste that.)


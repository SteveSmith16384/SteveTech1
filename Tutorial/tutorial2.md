## Java Multiplayer FPS Engine Tutorial using SteveTech1

# PART 2

## What was I doing again?
If you've got here, then you should have an empty project with references to SteveTech1 and jMonkeyEngine, hopefully with no compilationo errors.  To demonstrate a simple multiplayer realtime FPS, we're going to create a simple game called "Box Wars".  With SteveTech1, you can create any game you want to; <i>the only limit is your imagination!</i>  Actually a good imagination will be a real bonus here, since the game will consist of just boxes moving around on a big box shooting box-shaped bullets at each other.  It probably won't win you any Ludum Dare competitions, but it will demonstrate how to get a multiplayer FPS working.  Later, we might even add spheres or even some genuine 3D models.


## Boilerplate Stuff
Okay, the first thing to do is create our main client and server classes:-

* Create a class called BoxWarsServer, and extend AbstractGameServer
* Create a class called BoxWarsClient, and extend AbstractGameClient

Once you've done this, your IDE will probably be creaking under the weight of compile errors, as there will be parameters required for the superconstructors, and lots of abstract methods that require implementing.

Don't forget that all the source code for this game is included with SteveTech1, so feel free to copy and paste that.  In fact, probably the best idea is to copy the whole Tutorial project, and this tutorial will now go through the major aspects of the source code to explain what is happening and why:-

<i>Apologies if the source code has changed since this tutorial was written.  If it has changed and this tutorial doesn't match up, please let me know.</i>


## The Main Classes

### BoxWarsServer
All SteveTech1 servers extend AbstractGameServer, and then implement the abstract methods.  Most of the constructor parameters should be self-explanatory, but here are a few of note:-

* tickrateMillis - This is the interval between each frame of the game, and must be the same for the client and server.  The smaller it is, the faster your game will run, but make it too small and your hardware may struggle to keep up.
* sendUpdateIntervalMillis - How often the server sends a game update to the client.
* clientRenderDelayMillis - How far behind "reality" (i.e. the server's version of events) the client should show.  The reason for this is to take into account client lag, meaning the time it takes for the server to get data to the clients.  The client shows a delayed version of reality; that way, it (hopefully) always has data telling it what is going to happen.  If the client tried to show the current state, the game would be "stuttery", since if there was no new game data to show (maybe due to network latency), it would have to freeze everything until it got something.

#### Overridden Methods

* boolean canCollide(PhysicalEntity a, PhysicalEntity b); - Determines if two entities can collide.  You don't always want this (for example, a bullet shouldn't collide with the shooter).  Also, the same result should occur on both the client and the server, otherwise they will quickly get out of sync.

* void createGame() - This is called at the start of each game in order to create the game entities.  More on this later.

* AbstractServerAvatar createPlayersAvatarEntity(ClientData client, int entityid) - This bit of code must create an instance of the player's avatar.  This is usually called when a player joins the game, and also at the start of each game.


## Avatars

Since we're on the subject of avatars, now is a good time to discuss them.  An "avatar"


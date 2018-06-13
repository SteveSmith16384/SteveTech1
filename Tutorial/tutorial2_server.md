[Intro](tutorial1_intro.md) - [Creating Server](tutorial2_server.md) - [Creating Client](tutorial3_client.md) - [Shooting](tutorial4_shooting.md)

## Java Multiplayer FPS Engine Tutorial using SteveTech1

# PART 2

### What was I doing again?
If you've got here, then you should have an empty project with references to SteveTech1 and jMonkeyEngine, hopefully with no compilationo errors.  To demonstrate a simple multiplayer realtime FPS, we're going to create a simple game called "Box Wars".  With SteveTech1, you can create any game you want to; <i>the only limit is your imagination!</i>  Actually a good imagination will be a real bonus here, since the game will consist of just boxes moving around on a big box shooting box-shaped bullets at each other.  It probably won't win you any Ludum Dare competitions, but it will demonstrate how to get a multiplayer FPS working.  Later, we might even add spheres or even some genuine 3D models.


### Creating the Server
Okay, the first thing to do is create our main server class, so create a class called BoxWarsServer, and extend AbstractGameServer.  Once you've done this, your IDE will probably be creaking under the weight of compile errors, as there will be parameters required for the superconstructors, and lots of abstract methods that require implementing.

Don't forget that all the source code for this game is included with SteveTech1, so feel free to copy and paste that.  In fact, probably the best idea is to copy the whole Tutorial project, and this tutorial will now go through the major aspects of the source code to explain what is happening and why:-

<i>Apologies if the source code has changed since this tutorial was written.  If it has changed and this tutorial doesn't match up, please let me know.</i>


#### BoxWarsServer
All SteveTech1 servers extend AbstractGameServer, and then implement the abstract methods.  Most of the constructor parameters should be self-explanatory, but here are a few of note:-

* tickrateMillis - This is the interval between each frame of the game, and must be the same for the client and server.  The smaller it is, the faster your game will run, but make it too small and your hardware may struggle to keep up.
* sendUpdateIntervalMillis - How often the server sends a game update to the client.
* clientRenderDelayMillis - How far behind "reality" (i.e. the server's version of events) the client should show.  The reason for this is to take into account client lag, meaning the time it takes for the server to get data to the clients.  The client shows a delayed version of reality; that way, it (hopefully) always has data telling it what is going to happen.  If the client tried to show the current state, the game would be "stuttery", since if there was no new game data to show (maybe due to network latency), it would have to freeze everything until it got something.

#### Overridden Methods
* boolean canCollide(PhysicalEntity a, PhysicalEntity b); - Determines if two entities can collide.  You don't always want this (for example, a bullet shouldn't collide with the shooter).  Also, the same result should occur on both the client and the server, otherwise they will quickly get out of sync.

* void createGame() - This is called at the start of each game in order to create the game entities.  More on this later.

* AbstractServerAvatar createPlayersAvatarEntity(ClientData client, int entityid) - This bit of code must create an instance of the player's avatar.  This is usually called when a player joins the game, and also at the start of each game.


### Avatars
Since we're on the subject of avatars, now is a good time to discuss them.  An "avatar" is the player's representation in the game.  In SteveTech1, for every player there are 3 avatar entities:-

1 - The avatar entity running on the server.  Does not require a 3D model, and is controlled indirectly (the client sends inputs to the server).
2 - The avatar on the client that the player directly controls.  Does not require a 3D model, and its position is adjusted whenever the client and server get out of sync.
3 - The avatars on the client representing other players.  Requires a 3d model, and is directly controlled by commands from the server.

You will notice in the source code for this tutorial that there are 3 avatar classes: BoxWarsServerAvatar.java, BoxWarsClientAvatar.java and BoxWarsEnemyAvatar.java, which map respectively to the 3 different types listed above.  Only the first one is required for the server, and we create an instance of it when createPlayersAvatarEntity() is called.


### Creating the Game
Each time a game starts, the createGame() method is called, which is where all the entities required for the game should be created (with the exception of the player's avatars, which are created with calls to createPlayersAvatarEntity()).  In this game, we will only have a floor, since otherwise our physics engine would force is to drop into an infinite abyss (or until the y-co-ord becomes less than float.MINIMUM, pedants).


### Entities
This is the generic name for any game object.  Most, but not all, entities will by "physical", i.e. have dimensions and be drawable (on the client at least).  In this game, our first entity is an instance of Floor.

All entities have a "type" code, which is basically an integer which determines it's type.  This is so that a client knows what kind of game object to create.  You can create your own "positive codes" for your game.  There are some pre-defined entity codes, e.g. explosion, but these are all negative numbers to ensure you don't redefine them.


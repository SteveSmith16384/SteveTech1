[Intro](tutorial1_intro.md) - [Creating Server](tutorial2_server.md) - [Creating Client](tutorial3_client.md) - [Shooting](tutorial4_shooting.md)

## Java Multiplayer FPS Engine Tutorial using SteveTech1

# PART 3 - Creating the client

The client is created by subclassing AbstractGameClient.  As with the server, the constructor will need some settings passing to it, and a few methods will need overriding.

Probably the only method of note is "actuallyCreateEntity(AbstractGameClient game, NewEntityData msg)", which is called whenever the server sends a "create entity" message to the client.  This method needs to instanciate the relevant entity based on the data in NewEntityData.  This is relatively straightforward; however, bear in mind that when creating an avatar, you need to check if the playerID matches our own playerID, and if so, create a subclass of AbstractClientAvatar, otherwise create a subclass of AbstractEnemyAvatar.  See the source code of BoxWarsClient.java to see this in practise.


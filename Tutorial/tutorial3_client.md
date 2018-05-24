## Java Multiplayer FPS Engine Tutorial using SteveTech1

# PART 3 - Creating the client

## Creating the Client
The client is created by subclassing AbstractGameClient.  As with the server, the constructor will need some settings passing to it, and a few methods will need overriding.

Probably the only method of note is "actuallyCreateEntity(AbstractGameClient game, NewEntityData msg)", which is called whenever the server sends a "create entity" message to the client.  This method needs to instansiate the relevant entity based on the data in NewEntityData.  This is relatively straightforward; however, bear in mind that when creating an avatar, you need to check if the playerID matches our own playerID, and if so, create a subclass of AbstractClientAvatar, otherwise create a subclass of AbstractEnemyAvatar.  See the source code of BoxWarsClient.java to see this in practise.


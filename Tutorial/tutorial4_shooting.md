[Intro](tutorial1_intro.md) - [Creating Server](tutorial2_server.md) - [Creating Client](tutorial3_client.md) - [Shooting](tutorial4_shooting.md)

## Java Multiplayer FPS Engine Tutorial using SteveTech1

# PART 4 - Shooting

Shooting is a particularly complicated since, just like when a player moves, when a player shoots they usually want the projectile to appear instantly.  With a projectile, the client can't simply launch its own since the server needs to know and keep track of all entities, so it can't allow clients to create their own.

SteveTech1 gets around this problem by giving the clients a batch of "unfired" bullets, which the client can launch if required.  The client must also confirm with the server that the bullets can be fired, but at least until that confirmation comes back, the player will see a projectile flying off into the distance.


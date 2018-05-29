[Intro](tutorial1_intro.md) - [Creating Server](tutorial2_server.md) - [Creating Client](tutorial3_client.md) - [Shooting](tutorial4_shooting.md)

# Java Multiplayer FPS Engine Tutorial using SteveTech1

### You want to create your own Realtime Multiplayer FPS?
Are you mad?  Maybe you don't realise how incredibly complicated it is!

### Why is it so complicated?  I've written a single-player FPS before!
It's more complicated than a non-networked FPS for essentially one reason: so that when a player pressed "move forward" on their keyboard, their avatar moves forward instantly.  

Because this needs to be instant, it means the client software must process the keypress itself, before it has had any time to confirm any details with the server (like "is there anything in the way?".  This means it has to essentially run the same game as the server.  However, this then causes two further problems: the client could potentially cheat, and also what happens if the client determines that one outcome happened (e.g. moves the player's avatar forwards), but the server (or another client) determines another (e.g. there's a wall in the way!)?

To avoid this kind of problem, the server is treated as authoritative.  This  means that it has final say on the state of the game, and if any client thinks differently, they had better get back in sync as quickly as possible (while still maintaining a seamless experience for the player).  So, the bulk of the additional code over-and-above a simple FPS is keeping clients and server in sync.


### Is there any good news?
Yes!  SteveTech1 is designed exactly to solve all of these problems for you.  It still won't be that easy (as writing anything in 3D never is), but it will be a lot easier than it would be without it.


### Okay, I'm up for a challenge.  What do I need to do?
Firstly you must prove yourself by completing the following: set up a basic Java project in your favourite IDE, which includes both the jMonkeyEngine and SteveTech1 libraries.  Everything required is included with this project.  To get full marks, get one of the SteveTech1 example games running, such as Undercover Agent.  Once you have something that compiles, go onto the next stage of the tutorial.

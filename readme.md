# SteveTech1

Complete open source multiplayer FPS Engine for Java

Designed and Developed by Stephen Carlyle-Smith (stephen.carlylesmith@googlemail.com)


SteveTech1 is a 3D multiplayer FPS framework and engine, designed to help you create games such as Team Fortress 2, Overwatch, PUBG etc.. in Java.  It includes almost all the code you'll need to create your own multiplayer FPS, and uses JMonkeyEngine for the 3D.  It is designed to be easily modifiable and extendable (as the example games will show).  It is intended to be the Java equivalent of something like the Source engine.  You will need to provide your own assets of course.



### FEATURES:
* Client prediction & lag-compensation
* TCP and UDP for networking
* Collision detection& simple physics
* Projectile and hitscan weapons
* Open source


### GAMES MADE WITH STEVETECH1
I'm currently working on two games:

* Moonbase Assault (https://bitbucket.org/SteveSmith16384/moonbaseassault/src/master/)
* Two Weeks in the Pub (Fortnite clone) (https://bitbucket.org/SteveSmith16384/twoweeksinthepub/src/master/)

Here's a video of Moonbase Assault:-

[![Moonbase Assault Gameplay](http://img.youtube.com/vi/E38SdsO-nEI/0.jpg)](http://www.youtube.com/watch?v=E38SdsO-nEI)

Or check out this channel of other vidoes of stuff I've done with this engine: https://www.youtube.com/watch?v=NVcFt4ehz4o&list=PLbGkfhhJ5G3_pH9tp2lH1zeAJ9Y35rQnm


## GETTING STARTED
* You are advised to look at the example game Tutorial to see how to create your own simple multiplayer FPS game using SteveTech1.  There is also the game Undercover Agent included, which contains more advanced features.
* See the SteveTech1 Tutorial at https://multiplayertowerdefence.blogspot.com/p/home.html
* If you've never used JMonkeyEngine before, the excellent documentation can be found here: https://jmonkeyengine.github.io/wiki/jme3.html#tutorials-for-beginners 


### TECHNICAL OVERVIEW
* To create the server, extend AbstractGameServer.  Various methods require overriding that will define how your game works. 
* To create the client, extend AbstractGameClient.  Various methods require overriding that will define how your client works.  The client needs access to all the same data as the server; it currently doesn't download anything from the server.
* The game primarily works around "entities", which can be anything, e.g. a player, a wall etc...  Entities are defined mainly by implementing interfaces.
* You will need to obtain your own assets of course!
* The code is designed so that as much identical code as possible is run on both the client and the server.  The client handles its own collision as far as preventing physical entities from moving inside each other, but only the server handles collision effects, e.g. players getting killed, doors being opened.
* Have a look in the /docs folder for various notes on specific aspects of the game.
* Network communication (which uses Kryonet) works on Messages, which are serialized classes (POJOs) containing the required information.  One of the parameters in the constructor the messages is whether to use TCP or UDP.  Use TCP for any message that needs to guarantee delivery (e.g. a Game Over message), and UDP for any messages where speed is most important, e.g. An entities updated position.


## DEBUGGING
* The file Globals.java contains lots of boolean for helping to debug certain aspects of the game.
* On the server command console, you can send the command "mb", which will make the client draw an outline of all the entities as the server sees them.  This can be useful, since otherwise there is no easy way of knowing what the world looks like on the server.
* Enter "help" on the server console for a list of other commands.


### CREDITS
* Designed and developed by Stephen Carlyle-Smith (http://twitter.com/stephencsmith, stephen.carlylesmith@googlemail.com)
* Uses JMonkeyEngine for the 3D
* Uses Kryonet for networking.
* TTF font loader by Adam T. Ryder (https://1337atr.weebly.com/jttf.html)
* For model credits, see the relevant folder containing the model.


### LICENCES
* The SteveTech code is licenced under MIT: https://bitbucket.org/SteveSmith16384/stetech1/raw/5d3d7eb190119e4b77e4a3f5042a59906653b7a2/stevetech1_mit_licence.txt
* JMonkeyEngine licence: [https://jmonkeyengine.github.io/wiki/bsd_license.html#jme-s-bsd-license]
* Kryonet Licence: [https://raw.githubusercontent.com/EsotericSoftware/kryonet/master/license.txt]
* Simple Physics for JME [https://bitbucket.org/SteveSmith16384/simplephysicsforjme]
* For the asset licences, see any file in the relevant assets folder.
 


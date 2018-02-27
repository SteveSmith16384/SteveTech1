# SteveTech1

Complete open source multiplayer FPS Engine for Java

SteveTech1 is a 3D multiplayer FPS framework and engine, designed to help you create games such as Team Fortress 2, Overwatch, PUBG etc.. in Java (aim high!).  It includes almost all the code you'll need to create your own multiplayer FPS, and uses JMonkeyEngine for the 3D.  It is designed to be easily modifiable extendable (as the example games will show).  It is intended to be the Java equivalent of the Source engine (or whatever the latest-and-greatest FPS engine is these days).  You will need to provide your own assets of course.


## CURRENT STATUS
All the basic features are complete but not thoroughly stress-tested yet. 


## FEATURES:
* Fully open source
* Authoritative server
* Client prediction
* Lag-compensation
* TCP and UDP for networking
* Time "rewinding" for accurate shooting
* Simple physics
* Includes simple example games
* Simulate packet delay/loss


## GETTING STARTED
You are advised to look at the example game Undercover Agent to see how to create your own multiplayer FPS game using SteveTech1.  Feel free to contact me (probably via Twitter though I may set up a forum if it becomes popular enough) with questions.


## TECHNICAL OVERVIEW
* The code is designed so that as much identical code as possible is run on both the client and the server.
* Read this excellent article about why an FPS needs lag compensation, position interpolation and client prediction: https://developer.valvesoftware.com/wiki/Source_Multiplayer_Networking
* Have a look in the /docs folder for various notes on specific aspects of the game.
* Network communication (which uses Kryonet) works on Messages, which are serialized classes (POJOs) containing the required information.


## DEBUGGING
* The file Globals.java contains lots of boolean for helping to debug certain aspects of the game.
* On the server command console, you can send the command "mb", which will make the client draw an outline of all the entities as the server sees them.  This can be useful, since otherwise there is no easy way of knowing what the world looks like on the server.


## CREDITS
* Designed and developed by Stephen Carlyle-Smith (http://twitter.com/stephencsmith/, stephen.carlylesmith@googlemail.com)
* Use JMonkeyEngine for the 3D
* Uses Kryonet for networking


## LICENCE
* The SteveTech code is licenced under MIT: https://bitbucket.org/SteveSmith16384/stetech1/raw/5d3d7eb190119e4b77e4a3f5042a59906653b7a2/stevetech1_mit_licence.txt
* JMonkeyEngine licence: [https://jmonkeyengine.github.io/wiki/bsd_license.html#jme-s-bsd-license]
* Kryonet Licence: [https://raw.githubusercontent.com/EsotericSoftware/kryonet/master/license.txt]
* For the asset licences, see any file in the relevant assets folder.
 


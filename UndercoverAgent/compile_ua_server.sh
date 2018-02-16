find -name "*.java" > sources.txt
javac -d bin -cp ../stetech1/libs/jme/gluegen-rt.jar:../stetech1/libs/jme/jME3-blender.jar:../stetech1/libs/jme/jME3-core.jar:../stetech1/libs/jme/jME3-desktop.jar:../stetech1/libs/jme/jME3-effects.jar:../stetech1/libs/jme/jME3-jogg.jar:../stetech1/libs/jme/jME3-jogl.jar:../stetech1/libs/jme/jME3-lwjgl.jar:../stetech1/libs/jme/jME3-networking.jar:../stetech1/libs/jme/jME3-plugins.jar:../stetech1/libs/jme/jME3-terrain.jar @sources.txt $*
echo Finished.

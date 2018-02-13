find -name "*.java" > sources.txt
javac -d bin -cp libs/jme/lib/gluegen-rt.jar:libs/jme/lib/jinput.jar:libs/jme/lib/lwjgl.jar:libs/jme/lib/jogl.jar:libs/jme/lib/lwjgl_util.jar:libs/jme/lib/swt.jar @sources.txt $*
echo Finished.


find -name "*.java" > sources.txt
javac -d bin -cp ../stetech1/libs/jme/gluegen-rt.jar:../stetech1/libs/jme/jME3-core.jar @sources.txt $*
echo Finished.


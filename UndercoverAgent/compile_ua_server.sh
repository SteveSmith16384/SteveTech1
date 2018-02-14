find -name "*.java" > sources.txt
javac -d bin -cp ../stetech1/libs/jme/lib/gluegen-rt.jar @sources.txt $*
echo Finished.


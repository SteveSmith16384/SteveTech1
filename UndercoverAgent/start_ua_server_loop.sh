#!/bin/sh
#while true; do
java -classpath ./bin -XX:GCTimeRatio=19 -XX:MinHeapFreeRatio=20 -XX:MaxHeapFreeRatio=30 com.scs.undercoveragent.UndercoverAgentServer $*
echo Restarting
#sleep 60s
#done


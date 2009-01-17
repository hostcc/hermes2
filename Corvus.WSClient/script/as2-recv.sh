#!/bin/sh

LIB_PATH=./lib
ARGS=$*

for i in `ls -1 $LIB_PATH`
do
       WSC_CLASSPATH=$WSC_CLASSPATH$LIB_PATH"/"$i":"
done

if [ $# -lt 5 ]; then
	if [ "$1" = "" ]; then
		ARGS="./config/as2-partnership.xml"
	fi
	if [ "$2" = "" ]; then
		ARGS="$ARGS ./config/as2-recv/as2-request.xml"	
	fi
	if [ "$3" = "" ]; then
		ARGS="$ARGS ./logs/as2-recv.log"
	fi
	if [ "$4" = "" ]; then
		ARGS="$ARGS ./output/as2-recv"
	fi
fi

echo $ARGS

EXEC="$JAVA_HOME/bin/java -cp $WSC_CLASSPATH hk.hku.cecid.corvus.ws.AS2ReceiverSender $ARGS"
echo $EXEC
exec $EXEC

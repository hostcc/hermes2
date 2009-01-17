#!/bin/sh

LIB_PATH=./lib
ARGS=$*

for i in `ls -1 $LIB_PATH`
do
       WSC_CLASSPATH=$WSC_CLASSPATH$LIB_PATH"/"$i":"
done

if [ $# -lt 4 ]; then
	if [ "$1" = "" ]; then
		ARGS="./config/ebms-partnership.xml"
	fi
	if [ "$2" = "" ]; then
		ARGS="$ARGS ./config/ebms-send/ebms-request.xml"	
	fi
	if [ "$3" = "" ]; then
		ARGS="$ARGS ./logs/ebms-send.log"
	fi
	if [ "$4" = "" ]; then
		ARGS="$ARGS ./config/ebms-send/testpayload"
	fi
fi

echo $ARGS

EXEC="$JAVA_HOME/bin/java -cp $WSC_CLASSPATH hk.hku.cecid.corvus.ws.EBMSMessageSender $ARGS"
echo $EXEC
exec $EXEC

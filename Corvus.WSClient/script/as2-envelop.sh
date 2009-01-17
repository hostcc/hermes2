#!/bin/sh

LIB_PATH=./lib
ARGS=$*

for i in `ls -1 $LIB_PATH`
do
       WSC_CLASSPATH=$WSC_CLASSPATH$LIB_PATH"/"$i":"
done

if [ $# -lt 2 ]; then	
	if [ "$1" = "" ]; then
		ARGS="./config/as2-envelop/as2-request.xml"	
	fi
	if [ "$2" = "" ]; then
		ARGS="$ARGS ./logs/as2-envelop.log"
	fi	
fi

echo $ARGS

EXEC="$JAVA_HOME/bin/java -cp $WSC_CLASSPATH hk.hku.cecid.corvus.http.AS2EnvelopQuerySender $ARGS"
echo $EXEC
exec $EXEC

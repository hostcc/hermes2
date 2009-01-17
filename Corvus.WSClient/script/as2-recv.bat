@echo off

set LIB_PATH=./lib
set ARGS=%1 %2 %3 %4

set WSC_CLASSPATH=.

for %%j in (%LIB_PATH%/*.jar) do (call  set-classpath.bat %LIB_PATH%/%%j )

:checkPartnershipFile
if not ""%1"" == """" goto checkConfigFile
set ARGS=
set ARGS=%ARGS% ./config/as2-partnership.xml

:checkConfigFile
if not ""%2"" == """" goto checkLogFile
set ARGS=%ARGS% ./config/as2-recv/as2-request.xml

:checkLogFile
if not ""%3"" == """" goto checkOutputDir
set ARGS=%ARGS% ./logs/as2-recv.log

:checkOutputDir
if not ""%4"" == """" goto checkOutputPattern
set ARGS=%ARGS% ./output/as2-recv/
:execCmd
@echo on

"%JAVA_HOME%\bin\java" -cp "%WSC_CLASSPATH%" hk.hku.cecid.corvus.ws.AS2ReceiverSender %ARGS%

PAUSE
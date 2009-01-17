@echo off

"%JAVA_HOME%\bin\java" -cp "%ANT_HOME%\lib\ant-launcher.jar" -Dant.home=%ANT_HOME% org.apache.tools.ant.launch.Launcher distribute

PAUSE


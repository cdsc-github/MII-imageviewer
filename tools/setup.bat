@echo off
set TEMP_CP=.;lib\utility-1.5.jar

:: debug
::echo TEMP_CP is %TEMP_CP%
::echo What is the current directory?
::cd

java -classpath  %TEMP_CP% utility.tools.UserInputFrame %1 %2

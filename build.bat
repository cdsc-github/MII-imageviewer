@echo off
cls
:: -----------------------------------------------------------------------------
:: build.bat - Win32 Build Script for ImageViewer
::
:: -----------------------------------------------------------------------------
 
:: ----- Verify and Set Required Environment Variables -------------------------
if not "%JAVA_HOME%"=="" goto GOT_JAVA_HOME
echo You must set JAVA_HOME to point at your Java Development Kit installation
goto CLEANUP

:GOT_JAVA_HOME

set TEMPCLASS=%CLASSPATH%
set ANT_HOME=./lib
set THREED=./lib/3d/j3dcore.jar;./lib/3d/j3dutils.jar;./lib/3d/vecmath.jar;./lib/flamingo/flamingo.jar
set ANTLIBS=%ANT_HOME%/ant-launcher.jar;%ANT_HOME%/ant.jar;%ANT_HOME%/ant-apache-bsf.jar;%ANT_HOME%/bsf-2.3.0.jar;%ANT_HOME%/bsh-2.0b4.jar
set CLASSPATH=%CLASSPATH%;"%JAVA_HOME%"\lib\tools.jar;%ANTLIBS%;%THREED%

"%JAVA_HOME%\bin\java.exe" %ANT_OPTS% -classpath %CLASSPATH% org.apache.tools.ant.launch.Launcher -Dant.home=.\lib -emacs %1 %2 %3 %4 %5 %6 %7 %8 %9

:CLEANUP
@echo off
set CLASSPATH=%TEMPCLASS%

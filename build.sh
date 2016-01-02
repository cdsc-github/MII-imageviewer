#!/bin/sh
# ----------------------------------------------
needJavaHome() {
    echo **** Set JAVA_HOME to point to your Java installation.
    exit 1
}

empty_string=""

if [ "$JAVA_HOME" = "$empty_string" ]; then
   needJavaHome
fi 

ANT_HOME=./lib
ANTLIBS=$ANT_HOME/ant-launcher.jar:$ANT_HOME/ant.jar:$ANT_HOME/ant-apache-bsf.jar:$ANT_HOME/bsf-2.3.0.jar:$ANT_HOME/bsh-2.0b4.jar

export TEMP_CP=.:"$JAVA_HOME/lib/tools.jar":$ANTLIBS:./lib/utility-1.5.jar:./lib/3d/vecmath.jar:./lib/3d/j3dutils.jar:./lib/3d/j3dcore.jar

echo $TEMP_CP
$JAVA_HOME/bin/java $ANT_OPTS -classpath $TEMP_CP org.apache.tools.ant.Main -Dant.home=./lib $1 $2 $3 $4 $5 $6 $7 $8 $9

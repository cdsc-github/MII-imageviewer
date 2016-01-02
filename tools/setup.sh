#!/bin/sh
# --------------------------------------------------------
needJavaHome() {
    echo **** Set JAVA_HOME to point to your Java installation.
    exit 1
} 

empty_string=""

if [ "$JAVA_HOME" = "$empty_string" ]; then
   needJavaHome
fi

export TEMP_CP=.:"$JAVA_HOME/lib/tools.jar":./lib/utility-1.5.jar

echo $TEMP_CP
$JAVA_HOME/bin/java -classpath $TEMP_CP utility.tools.UserInputFrame $1 $2

#!/bin/sh
# Maven Wrapper script
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.

set -e
MAVEN_WRAPPER_JAR="$( cd "$( dirname "$0" )/.mvn/wrapper" && pwd )/maven-wrapper.jar"
MAVEN_WRAPPER_PROPERTIES="$( cd "$( dirname "$0" )/.mvn/wrapper" && pwd )/maven-wrapper.properties"
MAVEN_WRAPPER_URL=""

if [ -r "$MAVEN_WRAPPER_PROPERTIES" ]; then
    MAVEN_WRAPPER_URL=$(grep "wrapperUrl" "$MAVEN_WRAPPER_PROPERTIES" | sed 's/.*=//;s/\r//')
fi

if [ -f "$MAVEN_WRAPPER_JAR" ]; then
    :
else
    if command -v curl > /dev/null; then
        curl -o "$MAVEN_WRAPPER_JAR" "$MAVEN_WRAPPER_URL" -s
    elif command -v wget > /dev/null; then
        wget -o "$MAVEN_WRAPPER_JAR" "$MAVEN_WRAPPER_URL"
    fi
fi

exec "$JAVA_HOME/bin/java" -jar "$MAVEN_WRAPPER_JAR" "$@"

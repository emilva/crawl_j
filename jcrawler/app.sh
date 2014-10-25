#!/bin/bash

case "$1" in
        update)
                git checkout src/main/resources/mongo.properties
                git pull origin master
                cp ../mongo.properties src/main/resources/mongo.properties
                mvn clean
                mvn compile
                mvn package -Dmaven.test.skip=true
                ;;
        *)
                echo "Usage: $0 {update}"
                ;;
esac

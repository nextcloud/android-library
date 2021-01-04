#!/bin/bash

count=$(./gradlew dependencies | grep SNAPSHOT -c)

if [ $count -eq 0 ] ; then
    exit 0
else
    exit 1
fi


#!/usr/bin/env bash
export EXCLUDE_GOOGLE_SERVICES_API_KEY=true
echo Releasing with version: $1
git tag -a $1 -m $1
git push --tags
./gradlew clean build lint -x test release

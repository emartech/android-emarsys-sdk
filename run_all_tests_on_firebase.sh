#!/bin/bash

PIDS=""
RESULT=0
MODULES="
core-api
core-java
core
mobile-engage-api
mobile-engage
predict-api
predict
emarsys
emarsys-sdk
"
set -ex

curl -o /tmp/sacc_key.json $BITRISEIO_SERVICE_ACCOUNT_KEY_URL

#Activate cloud client with the service account
gcloud auth activate-service-account -q --key-file /tmp/sacc_key.json
#Set the project's id used on Google Cloud Platform
gcloud config set project $GCP_PROJECT

for i in $MODULES; do
   gcloud firebase test android run --type instrumentation --test "$i/build/outputs/apk/androidTest/debug/$i-debug-androidTest.apk" $APK $FIREBASE_DEVICES --timeout 10m --quiet --project ems-mobile-sdk &
   PIDS="$PIDS $!"
done

for PID in $PIDS; do
    wait $PID || let "RESULT=1"
done

if [[ "$RESULT" == "1" ]];
    then
       exit 1
fi
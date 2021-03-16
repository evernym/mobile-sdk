#!/bin/bash

set -e

if [ "$1" = "--help" ] ; then
  echo "Usage: ./run-appium-tests.sh <app_file_path> <device_type=awsiOS/awsAndroid> <device=android_top5/galaxyS10/ios_top5/iphone11> <isMR=true/false>"
  return
fi

app_file_path="$1"
device_type="$2"
device="$3"
isMR="$4"
ngrokToken="$5"

TESTS_CONFIG_PATH="src/test/java/utility/Config.java"

cd e2e-automation

# update testng.xml
if [ "$isMR" = true ] ; then
    sed -i -e '10,15d' src/test/resources/testng.xml
fi

# setup VAS server
if [[ -v $ngrokToken ]] ; then
    ngrok authtoken $ngrokToken
fi
ngrok http 1338 >> /dev/null &
sleep 5
VAS_ENDPOINT=`curl -s localhost:4040/api/tunnels | jq -r .tunnels[0].public_url`
sed -ri "s|VAS_Server_Link = \".*\"|VAS_Server_Link = \"${VAS_ENDPOINT}\"|" ${TESTS_CONFIG_PATH}
python appium-launcher/vas-server.py &

# run Appium tests
sed -ri "s|Device_Type = \".*\"|Device_Type = \"${device_type}\"|" ${TESTS_CONFIG_PATH}
mvn install -DskipTests
python appium-launcher/script.py --test_file_path target/zip-with-dependencies.zip --app_file_path "../${app_file_path}" --device_type ${device_type} --device ${device}

# stop VAS server
pkill -9 -f vas-server.py
pkill -9 -f ngrok

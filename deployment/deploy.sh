#!/usr/bin/env bash

# replace dots with hyphens in APP_NAME
IMAGE_PREFIX=mastodonc
APP_NAME=$1
SEBASTOPOL_IP=$2
ENVIRONMENT=$3
INSTANCE_COUNT=$4
TAG=$5

# using deployment service sebastopol
VPC=sandpit
sed -e "s/@@TAG@@/$TAG/" -e "s/@@ENVIRONMENT@@/$ENVIRONMENT/" -e "s/@@VPC@@/$VPC/" -e "s/@@CANARY@@/$CANARY/" -e "s/@@APP_NAME@@/$APP_NAME/" -e "s/@@IMAGE_PREFIX@@/$IMAGE_PREFIX/"  -e "s/@@INSTANCE_COUNT@@/$INSTANCE_COUNT/" ./deployment/marathon-config.json.template > $APP_NAME.json

# we want curl to output something we can use to indicate success/failure
STATUS=$(curl -s -w "%{http_code}" -X POST http://$SEBASTOPOL_IP:9501/marathon/$APP_NAME -H "Content-Type: application/json" -H "$SEKRIT_HEADER: 123" --data-binary "@$APP_NAME.json" | sed 's/.*\(...\)/\1/')

echo "HTTP code " $STATUS
if [ $STATUS -eq 201 ]
then exit 0
else exit 1
fi

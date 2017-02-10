#!/bin/bash

AUTH=`cat .auth`
API_KEY=`cat .api_key`
BOTNAME="lazycoderbot"

if [ -e ".environment" ]; then
ENV=`cat .environment`
else
ENV="prod"
fi

/usr/bin/java -Denv="${ENV}" -Ddw.auth="${AUTH}" -Ddw.api_key="${API_KEY}" -jar ${BOTNAME}.jar server ${BOTNAME}.yaml

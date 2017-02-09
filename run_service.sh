#!/bin/bash

AUTH=`cat .auth`
BOTNAME="lazycoderbot"

if [ -e ".environment" ]; then
ENV=`cat .environment`
else
ENV="prod"
fi

/usr/bin/java -Denv="${ENV}" -Ddw.auth="${AUTH}" -jar ${BOTNAME}.jar server ${BOTNAME}.yaml

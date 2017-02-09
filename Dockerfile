FROM wire/wbots.runtime:latest

COPY target/lazycoderbot.jar /opt/lazycoderbot/lazycoderbot.jar
COPY lazycoderbot.yaml /opt/lazycoderbot/lazycoderbot.yaml
COPY run_service.sh /opt/lazycoderbot/run_service.sh
COPY .auth /opt/lazycoderbot/.auth
COPY .environment /opt/lazycoderbot/.environment
COPY certs/keystore.jks /opt/lazycoderbot/certs/keystore.jks
COPY images/ /opt/lazycoderbot/images/
COPY audios/ /opt/lazycoderbot/audios/
COPY videos/ /opt/lazycoderbot/videos/

WORKDIR /opt/lazycoderbot

EXPOSE 8060

ENTRYPOINT [ "/opt/lazycoderbot/run_service.sh"  ]

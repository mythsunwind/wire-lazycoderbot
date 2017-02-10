FROM wire/wbots.runtime:latest

COPY target/lazycoderbot.jar /opt/lazycoderbot/lazycoderbot.jar
COPY lazycoderbot.yaml /opt/lazycoderbot/lazycoderbot.yaml
COPY run_service.sh /opt/lazycoderbot/run_service.sh
COPY .auth /opt/lazycoderbot/.auth
COPY .api_key /opt/lazycoderbot/.api_key
COPY .environment /opt/lazycoderbot/.environment
COPY certs/keystore.jks /opt/lazycoderbot/certs/keystore.jks

WORKDIR /opt/lazycoderbot

EXPOSE 8070

ENTRYPOINT [ "/opt/lazycoderbot/run_service.sh"  ]

FROM alpine:3.14

RUN apk update \
    && apk upgrade \
    && apk add ca-certificates \
    && update-ca-certificates \
    && apk add --update coreutils && rm -rf /var/cache/apk/* \
    && apk add --update openjdk11 tzdata curl unzip bash \
    && apk add --no-cache nss \
    && rm -rf /var/cache/apk/*

WORKDIR /app
COPY target/myasset-1.0-SNAPSHOT-jar-with-dependencies.jar ./chaincode.jar
COPY crypto/extcc.key keypath/extcc.key
COPY crypto/extcc.pem certpath/extcc.pem
COPY crypto/extcc64.key keypath/extcc64.key
COPY crypto/extcc64.pem certpath/extcc64.pem
COPY crypto/CA/CA.pem CA/CA.pem

ARG CCID
ENV CORE_CHAINCODE_ID_NAME=${CCID}
ENV CHAINCODE_SERVER_ADDRESS=0.0.0.0:9001
ENV CORE_PEER_TLS_ENABLED=true
# Intent is to have path suffixed variable to contain base64 encoded key values
ENV CORE_TLS_CLIENT_KEY_PATH=/app/keypath/extcc64.key
ENV CORE_TLS_CLIENT_KEY_FILE=/app/keypath/extcc.key
# Intent is to have path suffixed variable to contain base64 encoded cert values
ENV CORE_TLS_CLIENT_CERT_PATH=/app/certpath/extcc64.pem
ENV CORE_TLS_CLIENT_CERT_FILE=/app/certpath/extcc.pem

ENV CORE_PEER_TLS_ROOTCERT_FILE=/app/CA/CA.pem

ENTRYPOINT [ "java", "-jar", "chaincode.jar" ]
USER 65534

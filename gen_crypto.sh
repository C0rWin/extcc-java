#! /bin/bash


ARTIFICATS_OUT=$1

echo "Create PKIs folders $ARTIFICATS_OUT"
rm -rf $ARTIFICATS_OUT/crypto
mkdir -p $ARTIFICATS_OUT/crypto/CA
cat > $ARTIFICATS_OUT/crypto/csr.conf << CSREOF
[v3_req]
keyUsage = keyEncipherment, dataEncipherment
extendedKeyUsage = serverAuth
subjectAltName = @alt_names
[alt_names]
DNS.1 = extcc
DNS.2 = localhost
CSREOF

echo "Generate root CA private keys"
docker run --rm -v $ARTIFICATS_OUT/crypto:/export nginx openssl ecparam -name prime256v1 -genkey -noout -out "/export/CA/cap1.key"
docker run -it --rm -v $ARTIFICATS_OUT/crypto:/export nginx chmod ga+r "/export/CA/cap1.key"
docker run --rm -v $ARTIFICATS_OUT/crypto:/export nginx openssl pkcs8 -topk8 -nocrypt -in "/export/CA/cap1.key" -out "/export/CA/ca.key"
docker run -it --rm -v $ARTIFICATS_OUT/crypto:/export nginx chmod ga+r "/export/CA/ca.key"

echo "Generating self-signed root CA certificate"
docker run --rm -v $ARTIFICATS_OUT/crypto:/export nginx openssl req -new -x509 -nodes -key "/export/CA/ca.key" -sha256 -days 365 -out "/export/CA/CA.pem" -subj "/C=IL/ST=Haifa/O=extcc/CN=ca.extcc" -extensions v3_ca -addext "subjectAltName=DNS:ca.extcc" -addext "keyUsage = digitalSignature, keyEncipherment, dataEncipherment, cRLSign, keyCertSign" -addext "extendedKeyUsage = serverAuth, clientAuth"

echo "Generate private key for external chaincode"
docker run --rm -v $ARTIFICATS_OUT/crypto:/export nginx openssl ecparam -name prime256v1 -genkey -noout -out "/export/extcp1.key"
docker run -it --rm -v $ARTIFICATS_OUT/crypto:/export nginx chmod ga+r "/export/extcp1.key"
docker run --rm -v $ARTIFICATS_OUT/crypto:/export nginx openssl pkcs8 -topk8 -nocrypt -in "/export/extcp1.key" -out "/export/extcc.key"
docker run --rm -v $ARTIFICATS_OUT/crypto:/export nginx chmod ga+r "/export/extcc.key" 

echo "Generate chaincode CSR"
docker run --rm -v $ARTIFICATS_OUT/crypto:/export nginx openssl req -new -key "/export/extcc.key" -out "/export/extcc.csr" -subj "/C=IL/ST=Haifa/CN=extcc/O=extcc" -addext "extendedKeyUsage = serverAuth, clientAuth" -addext "subjectAltName=DNS:extcc"
echo "Generate chaincode x509 certificate"
docker run --rm -v $ARTIFICATS_OUT/crypto:/export nginx openssl x509 -req -in "/export/extcc.csr" -CA "/export/CA/ca.pem" -CAkey "/export/CA/ca.key" -CAcreateserial -out "/export/extcc.pem" -days 365 -sha256 -extensions v3_req -extfile /export/csr.conf


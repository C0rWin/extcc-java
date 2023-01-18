## Instruction how-to run java based external chaincode

1. Get Fabric Samples project:

```
git clone https://github.com/hyperledger/fabric-samples.git
```

2. Get into test-network subfolder:

```
cd test-network
```

3. Run Fabric test network:

```
./network.sh createChannel -c mychannel
```

4. Compile JAR artifact:

```
mvn clean install -DskipTests
```

5. Package external chaincode:

```
make package
```

6. Copy external chaincode into `cli` container:

```
docker cp extcc.tar.gz cli:/opt/gopath/src/github.com/hyperledger/fabric/peer/.
```

7. Login into `cli` container:

```
docker exec -it cli sh
```

8. Setup environmental variables:

```
export CORE_PEER_TLS_ENABLED=true
export ORDERER_CA=${PWD}/organizations/ordererOrganizations/example.com/tlsca/tlsca.example.com-cert.pem
export PEER0_ORG1_CA=${PWD}/organizations/peerOrganizations/org1.example.com/tlsca/tlsca.org1.example.com-cert.pem
export PEER0_ORG2_CA=${PWD}/organizations/peerOrganizations/org2.example.com/tlsca/tlsca.org2.example.com-cert.pem
export PEER0_ORG3_CA=${PWD}/organizations/peerOrganizations/org3.example.com/tlsca/tlsca.org3.example.com-cert.pem
export ORDERER_ADMIN_TLS_SIGN_CERT=${PWD}/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/tls/server.crt
export ORDERER_ADMIN_TLS_PRIVATE_KEY=${PWD}/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/tls/server.key
```

9. Add to the variables setup for Org1:

```
export CORE_PEER_LOCALMSPID="Org1MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=$PEER0_ORG1_CA
export CORE_PEER_MSPCONFIGPATH=${PWD}/organizations/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
export CORE_PEER_ADDRESS=peer0.org1.example.com:7051
```

10. Install chaincode:

```
peer lifecycle chaincode install extcc.tar.gz
```

11. Switch to the Org2:

```
export CORE_PEER_LOCALMSPID="Org2MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=$PEER0_ORG2_CA
export CORE_PEER_MSPCONFIGPATH=${PWD}/organizations/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp
export CORE_PEER_ADDRESS=peer0.org2.example.com:9051
```

12. Install chaincode:

```
peer lifecycle chaincode install extcc.tar.gz
```

13. Switch back to Org1:

```
export CORE_PEER_LOCALMSPID="Org1MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=$PEER0_ORG1_CA
export CORE_PEER_MSPCONFIGPATH=${PWD}/organizations/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
export CORE_PEER_ADDRESS=peer0.org1.example.com:7051
```

14. Approve chaincode for Org1:

```
peer lifecycle chaincode approveformyorg \
        -o orderer.example.com:7050 --tls --cafile $ORDERER_CA \
        --channelID mychannel --name extcc \
        --version v1.0 --sequence 1 \
        --signature-policy 'OR("org1.member", "org2.member")' \
        --package-id $(peer lifecycle chaincode calculatepackageid extcc.tar.gz)
```

15. Switch to Org2:

```
export CORE_PEER_LOCALMSPID="Org2MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=$PEER0_ORG2_CA
export CORE_PEER_MSPCONFIGPATH=${PWD}/organizations/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp
export CORE_PEER_ADDRESS=peer0.org2.example.com:9051
```

16. Approve chaincode for Org2:

```
peer lifecycle chaincode approveformyorg \
        -o orderer.example.com:7050 --tls --cafile $ORDERER_CA \
        --channelID mychannel --name extcc \
        --version v1.0 --sequence 1 \
        --signature-policy 'OR("org1.member", "org2.member")' \
        --package-id $(peer lifecycle chaincode calculatepackageid extcc.tar.gz)
```

17. Switch back to Org1:

```
export CORE_PEER_LOCALMSPID="Org1MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=$PEER0_ORG1_CA
export CORE_PEER_MSPCONFIGPATH=${PWD}/organizations/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
export CORE_PEER_ADDRESS=peer0.org1.example.com:7051
```

18. Commit chaincode definition:

```
peer lifecycle chaincode commit \
-o orderer.example.com:7050 --tls --cafile $ORDERER_CA \
        --channelID mychannel --name extcc \
        --peerAddresses peer0.org1.example.com:7051 --tlsRootCertFiles $PEER0_ORG1_CA \
        --peerAddresses peer0.org2.example.com:9051 --tlsRootCertFiles $PEER0_ORG2_CA \
        --version v1.0 --sequence 1 --signature-policy 'OR("org1.member", "org2.member")'
```

19. Get chaincode package-id:

```
peer lifecycle chaincode calculatepackageid extcc.tar.gz
```

and copy the results from the output.

20. Run chaincode container:

```
docker run -it --rm -p 9001:9001 \
        -e CCID=extcc:40a2e99ced596fde7618962afcd40d0e56a702aeff0556989b2643c98eea5757 \
        --name extcc --net fabric_test extcc_java
```

21. Query chaincode metadata JSON:

```
peer chaincode query -o orderer.example.com:7050 --tls --cafile $ORDERER_CA \
        -C mychannel -n extcc \
        -c '{"Args":["org.hyperledger.fabric:GetMetadata"]}'
```
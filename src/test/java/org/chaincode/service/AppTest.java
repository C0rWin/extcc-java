package org.chaincode.service;

import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.netty.handler.ssl.ClientAuth;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() throws IOException {
        final File keyFile = new File("/Users/c0rwin/workspace/myasset-java/crypto/extcc.key");
        if (!keyFile.exists()) {
            new FileNotFoundException("key file was not found");
        }
        final File keyCertChainFile = new File("/Users/c0rwin/workspace/myasset-java/crypto/extcc.pem");
        if (!keyCertChainFile.exists()) {
            new FileNotFoundException("certificate file was not found");
        }
        SslContextBuilder sslContextBuilder = SslContextBuilder.forServer(keyCertChainFile, keyFile);

        sslContextBuilder.clientAuth(ClientAuth.REQUIRE);
        sslContextBuilder.trustManager(new File("/Users/c0rwin/workspace/myasset-java/crypto/CA/CA.pem"));


        final byte[] ckb = Files.readAllBytes(Paths.get("/Users/c0rwin/workspace/myasset-java/crypto/extcc64.key"));
        final byte[] ccb = Files.readAllBytes(Paths.get("/Users/c0rwin/workspace/myasset-java/crypto/extcc64.pem"));
        System.out.println(new String(ccb));
        GrpcSslContexts.forClient().trustManager(new File("/Users/c0rwin/workspace/myasset-java/crypto/CA/CA.pem"))
                .keyManager(new ByteArrayInputStream(Base64.getMimeDecoder().decode(ccb)),
                        new ByteArrayInputStream(Base64.getMimeDecoder().decode(ckb)))
                .build();
    }
}

package org.chaincode.externalserver;

import org.hyperledger.fabric.contract.ContractRouter;
import org.hyperledger.fabric.shim.ChaincodeServerProperties;
import org.hyperledger.fabric.shim.NettyChaincodeServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

public class ExternalChaincode {
    private ContractRouter router;

    /**
     * Take the arguments from the cli, and initiate processing of cli options and
     * environment variables.
     * <p>
     * Create the Contract scanner, and the Execution service
     *
     * @param args
     */
    public ExternalChaincode(String[] args) {
        this.router = new ContractRouter(args);
    }

    public ContractRouter getRouter() {
        return router;
    }

    public SocketAddress parseHostPort(final String hostAddrStr) throws URISyntaxException {

        // WORKAROUND: add any scheme to make the resulting URI valid.
        URI uri = new URI("my://" + hostAddrStr); // may throw URISyntaxException
        String host = uri.getHost();
        int port = uri.getPort();

        if (uri.getHost() == null || uri.getPort() == -1) {
            throw new URISyntaxException(uri.toString(),
                    "URI must have host and port parts");
        }

        // validation succeeded
        return new InetSocketAddress(host, port);
    }

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        final ExternalChaincode externalChaincode = new ExternalChaincode(args);
        final ChaincodeServerProperties chaincodeServerProperties = new ChaincodeServerProperties();
        chaincodeServerProperties.setServerAddress(externalChaincode.parseHostPort("extcc:9001"));
        chaincodeServerProperties.setTlsEnabled(true);

//        String peerTLSRootCertFile = System.getenv("CORE_PEER_TLS_ROOTCERT_FILE");
//        chaincodeServerProperties.setTrustCertCollectionFile(peerTLSRootCertFile);

        String tlsClientKeyPath = System.getenv("CORE_TLS_CLIENT_KEY_FILE");
        chaincodeServerProperties.setKeyFile(tlsClientKeyPath);

        String tlsClientCertPath = System.getenv("CORE_TLS_CLIENT_CERT_FILE");
        chaincodeServerProperties.setKeyCertChainFile(tlsClientCertPath);

        final NettyChaincodeServer chaincodeServer = new NettyChaincodeServer(externalChaincode.getRouter(),
                chaincodeServerProperties);

        externalChaincode.getRouter().startRouterWithChaincodeServer(chaincodeServer);
    }
}

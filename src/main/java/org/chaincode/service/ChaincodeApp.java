package org.chaincode.service;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Transaction;

/**
 * Hello world!
 */
@Contact
public class ChaincodeApp implements ContractInterface {

    public ChaincodeApp() {
    }

    @Transaction
    public String sayPing(Context ctx) {
        return "Ping";
    }
}

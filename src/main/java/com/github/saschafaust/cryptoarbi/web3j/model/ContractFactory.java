package com.github.saschafaust.cryptoarbi.web3j.model;

import java.lang.reflect.Method;

import com.github.saschafaust.cryptoarbi.web3j.Web3Adress;
import com.github.saschafaust.cryptoarbi.web3j.Web3Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.tx.Contract;

/**
 * Factory to instantiate ContractWrapper classes
 */
public class ContractFactory {

    private static Logger logger = LoggerFactory.getLogger(ContractFactory.class);

    private final Web3Context ctx;

    public static ContractFactory get() {
        return ContractFactory.get(new Web3Context());
    }

    public static ContractFactory get(Web3Context ctx) {
        return new ContractFactory(ctx);
    }

    @SuppressWarnings("unchecked")
    public <T extends Contract> T contract(Class<T> clazz, Web3Adress contractAdress) {
        try {
            Method loadMethod = clazz.getMethod("load", String.class, org.web3j.protocol.Web3j.class,
                    org.web3j.crypto.Credentials.class, org.web3j.tx.gas.ContractGasProvider.class);
            return (T) loadMethod.invoke(null, contractAdress.get(), ctx.getWeb3j(), ctx.getCredentials(),
                    ctx.getContractGasProvider());
        } catch (Exception e) {
            logger.error("unhandled exception.", e);
            throw new RuntimeException(e);
        }
    }

    private ContractFactory(Web3Context ctx) {
        this.ctx = ctx;
    }

}

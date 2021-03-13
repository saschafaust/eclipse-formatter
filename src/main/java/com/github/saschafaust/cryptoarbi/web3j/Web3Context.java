package com.github.saschafaust.cryptoarbi.web3j;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

/**
 * Conext Data for requests to web3
 */
public class Web3Context {

    private final HttpService httpService;
    private final Web3j web3j;
    private final Credentials credentials;
    private final ContractGasProvider contractGasProvider;

    public Web3Context() {
        this.httpService = new HttpService(Web3Constants.Url.HTTP_SERVICE);
        this.web3j = Web3j.build(httpService);
        this.credentials = Credentials.create(Web3Constants.PRIVATE_KEY);
        this.contractGasProvider = new DefaultGasProvider();
    }

    public Web3j getWeb3j() {
        return this.web3j;
    }

    public Credentials getCredentials() {
        return this.credentials;
    }

    public ContractGasProvider getContractGasProvider() {
        return this.contractGasProvider;
    }

}

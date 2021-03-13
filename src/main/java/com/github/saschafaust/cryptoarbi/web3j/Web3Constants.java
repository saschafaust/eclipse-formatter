package com.github.saschafaust.cryptoarbi.web3j;

public interface Web3Constants {

    /** a private key */
    public String PRIVATE_KEY = "8aabe7a9d98ec04b75b3bb336a73cd189d3069520283ed81c006cfbe8344d29d";

    public interface Adress {

        /** adress of the WETH contract */
        public Web3Adress WETH = Web3Adress.from("0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2");

        /** adress of the uniswap factory contract */
        public Web3Adress UNISWAP_FACTORY = Web3Adress.from("0x5C69bEe701ef814a2B6a3EDD4B1652CB9cc5aA6f");

        /** adress of the sushiswap factory contract */
        public Web3Adress SUSHISWAP_FACTORY = Web3Adress.from("0xC0AEe478e3658e2610c5F7A4A2E1777cE9e4f2Ac");

    }

    public interface Url {

        /** url of the htts service */
        public String HTTP_SERVICE = "https://mainnet.infura.io/v3/6e895b6edaef4801a3d2aa10781527ff";

    }

}

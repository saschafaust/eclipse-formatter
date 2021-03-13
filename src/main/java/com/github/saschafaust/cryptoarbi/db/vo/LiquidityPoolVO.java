package com.github.saschafaust.cryptoarbi.db.vo;

import com.github.saschafaust.cryptoarbi.web3j.DexEnum;

import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;

@Document(collection = "liquidityPools", schemaVersion = "1.0")
public class LiquidityPoolVO {

    @Id
    private String adress;

    private String tokenA;
    private String tokenB;
    private DexEnum dex;

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

    public String getTokenA() {
        return tokenA;
    }

    public void setTokenA(String tokenA) {
        this.tokenA = tokenA;
    }

    public String getTokenB() {
        return tokenB;
    }

    public void setTokenB(String tokenB) {
        this.tokenB = tokenB;
    }

    public DexEnum getDex() {
        return dex;
    }

    public void setDex(DexEnum dex) {
        this.dex = dex;
    }

}

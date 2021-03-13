package com.github.saschafaust.cryptoarbi.db.vo;

import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;

/**
 * Pairs two liquidity pools together
 */
@Document(collection = "arbitragePairs", schemaVersion = "1.0")
public class ArbitragePairVO {

    @Id
    private String token;

    private String poolA;
    private String poolB;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPoolA() {
        return poolA;
    }

    public void setPoolA(String poolA) {
        this.poolA = poolA;
    }

    public String getPoolB() {
        return poolB;
    }

    public void setPoolB(String poolB) {
        this.poolB = poolB;
    }

}

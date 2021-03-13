package com.github.saschafaust.cryptoarbi.db.generator;

import java.util.Collection;
import java.util.List;

import com.github.saschafaust.cryptoarbi.db.JsonDB;
import com.github.saschafaust.cryptoarbi.db.validator.ArbitragePairValidator;
import com.github.saschafaust.cryptoarbi.db.validator.ValidationException;
import com.github.saschafaust.cryptoarbi.db.vo.ArbitragePairVO;
import com.github.saschafaust.cryptoarbi.db.vo.LiquidityPoolVO;
import com.github.saschafaust.cryptoarbi.web3j.Web3Adress;
import com.github.saschafaust.cryptoarbi.web3j.Web3Constants;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jsondb.JsonDBTemplate;

/**
 * Generates ArbitragePairs and persits them in the db
 */
public class ArbitragePairGenerator {

    public static Logger logger = LoggerFactory.getLogger(ArbitragePairGenerator.class);

    public static void main(String[] args) {

        new ArbitragePairGenerator().run();

    }

    private final JsonDBTemplate db;

    public ArbitragePairGenerator() {
        this.db = JsonDB.instance();
        if (!db.collectionExists(ArbitragePairVO.class)) {
            db.createCollection(ArbitragePairVO.class);
        }
    }

    public void run() {
        try {
            generateArbitragePairs();
        } catch (Exception e) {
            logger.error("unhandled excection.", e);
            throw new RuntimeException(e);
        }
    }

    private void generateArbitragePairs() throws Exception {

        MultiValuedMap<Web3Adress, Web3Adress> poolMap = new HashSetValuedHashMap<>();

        // Map all pools together
        List<LiquidityPoolVO> allLpVOs = db.findAll(LiquidityPoolVO.class);
        for (LiquidityPoolVO vo : allLpVOs) {

            Web3Adress tokenAdress = Web3Constants.Adress.WETH.equals(Web3Adress.from(vo.getTokenA())) //
                    ? Web3Adress.from(vo.getTokenB()) //
                    : Web3Adress.from(vo.getTokenA());
            Web3Adress poolAdress = Web3Adress.from(vo.getAdress());
            poolMap.put(tokenAdress, poolAdress);
        }

        // Create ArbitragePairVOs
        for (Web3Adress token : poolMap.keySet()) {
            Collection<Web3Adress> tokenPools = poolMap.get(token);
            if (CollectionUtils.size(tokenPools) > 1) {
                createArbitrageVOs(token, tokenPools);
            }
        }
    }

    private void createArbitrageVOs(Web3Adress token, Collection<Web3Adress> tokenPools) {
        for (Web3Adress pool : tokenPools) {
            for (Web3Adress otherPool : tokenPools) {
                if (!pool.equals(otherPool)) {
                    ArbitragePairVO vo = new ArbitragePairVO();
                    vo.setPoolA(pool.get());
                    vo.setPoolB(otherPool.get());
                    vo.setToken(token.get());
                    try {
                        ArbitragePairValidator.validate(vo);
                        db.upsert(vo);
                    } catch (ValidationException e) {
                        logger.error("Error validating ArbitragePairVO ", e);
                    }
                }
            }
        }
    }

}

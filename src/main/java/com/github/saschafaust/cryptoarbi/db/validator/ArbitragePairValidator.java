package com.github.saschafaust.cryptoarbi.db.validator;

import com.github.saschafaust.cryptoarbi.db.JsonDB;
import com.github.saschafaust.cryptoarbi.db.vo.ArbitragePairVO;
import com.github.saschafaust.cryptoarbi.db.vo.LiquidityPoolVO;
import com.github.saschafaust.cryptoarbi.web3j.Web3Adress;
import com.github.saschafaust.cryptoarbi.web3j.Web3Constants;

/**
 * Logic to validate a ArbitragePairVO
 */
public class ArbitragePairValidator {

    public static void validate(ArbitragePairVO arbitragePair) {

        boolean isValid = true;
        Web3Adress eth_adress = Web3Constants.Adress.WETH;
        Web3Adress token_Adress = Web3Adress.from(arbitragePair.getToken());

        // is poolA a eth/token pair
        Web3Adress poolA_adress = Web3Adress.from(arbitragePair.getPoolA());
        LiquidityPoolVO poolA_LiquidityPool = JsonDB.instance().findById(poolA_adress.get(), LiquidityPoolVO.class);
        Web3Adress poolA_tokenA = Web3Adress.from(poolA_LiquidityPool.getTokenA());
        Web3Adress poolA_tokenB = Web3Adress.from(poolA_LiquidityPool.getTokenB());

        isValid = (poolA_tokenA.equals(eth_adress) && poolA_tokenB.equals(token_Adress)) //
                || (poolA_tokenB.equals(eth_adress) && poolA_tokenA.equals(token_Adress));

        if (!isValid) {
            throw new ValidationException(arbitragePair, "PoolA is not a eth/token pair.");
        }

        // is poolB a eth/token pair
        Web3Adress poolB_adress = Web3Adress.from(arbitragePair.getPoolB());
        LiquidityPoolVO poolB_VO = JsonDB.instance().findById(poolB_adress.get(), LiquidityPoolVO.class);
        Web3Adress poolB_tokenA = Web3Adress.from(poolB_VO.getTokenA());
        Web3Adress poolB_tokenB = Web3Adress.from(poolB_VO.getTokenB());

        isValid = (poolB_tokenA.equals(eth_adress) && poolB_tokenB.equals(token_Adress)) //
                || (poolB_tokenB.equals(eth_adress) && poolB_tokenA.equals(token_Adress));

        if (!isValid) {
            throw new ValidationException(arbitragePair, "PoolB is not a eth/token pair.");
        }

        // are poolA and poolB different pools
        isValid = !poolA_adress.equals(poolB_adress);

        if (!isValid) {
            throw new ValidationException(arbitragePair, "PoolA and PoolB are the same pool.");
        }

    }

}

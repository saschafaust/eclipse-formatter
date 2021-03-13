package com.github.saschafaust.cryptoarbi.db.generator;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.github.saschafaust.cryptoarbi.db.JsonDB;
import com.github.saschafaust.cryptoarbi.db.vo.LiquidityPoolVO;
import com.github.saschafaust.cryptoarbi.web3j.DexEnum;
import com.github.saschafaust.cryptoarbi.web3j.Web3Adress;
import com.github.saschafaust.cryptoarbi.web3j.Web3Constants;
import com.github.saschafaust.cryptoarbi.web3j.model.ContractFactory;
import com.github.saschafaust.cryptoarbi.web3j.model.IUniswapV2Factory;
import com.github.saschafaust.cryptoarbi.web3j.model.IUniswapV2Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.utils.Convert;
import org.web3j.utils.Convert.Unit;

import io.jsondb.JsonDBTemplate;

/**
 * Generates all available token pairs and writes them into the DB
 */
public class LiquidityPoolGenerator {

    public static Logger logger = LoggerFactory.getLogger(LiquidityPoolGenerator.class);

    public static void main(String[] args) {

        new LiquidityPoolGenerator().run();

    }

    private final JsonDBTemplate db;

    public LiquidityPoolGenerator() {
        this.db = JsonDB.instance();
        if (!db.collectionExists(LiquidityPoolVO.class)) {
            db.createCollection(LiquidityPoolVO.class);
        }
    }

    public void run() {
        try {
            generateIUniswapV2Pairs(DexEnum.UNISWAP, Web3Constants.Adress.UNISWAP_FACTORY);
        } catch (Exception e) {
            logger.error("unhandled excection.", e);
            throw new RuntimeException(e);
        }
    }

    private void generateIUniswapV2Pairs(DexEnum dex, Web3Adress factoryContractAdress) throws Exception {

        IUniswapV2Factory uniswapFactoryContract =
                ContractFactory.get().contract(IUniswapV2Factory.class, factoryContractAdress);

        BigInteger allPairsLength = uniswapFactoryContract.allPairsLength().send();
        logger.info("found " + allPairsLength.toString(10) + "for " + dex);
        for (BigInteger bi = BigInteger.ZERO; bi.compareTo(allPairsLength) < 0; bi = bi.add(BigInteger.ONE)) {

            Web3Adress pairAdress = Web3Adress.from(uniswapFactoryContract.allPairs(bi).send());
            IUniswapV2Pair uniswapPairContract = ContractFactory.get().contract(IUniswapV2Pair.class, pairAdress);

            LiquidityPoolVO vo = db.findById(pairAdress.get(), LiquidityPoolVO.class);
            if (vo != null) {
                updateIUniswapV2Pair(uniswapPairContract, vo);
            } else { // insert
                insertIUniswapV2Pair(dex, uniswapPairContract, pairAdress);
            }
        }
    }

    private void updateIUniswapV2Pair(IUniswapV2Pair uniswapPairContract, LiquidityPoolVO vo) throws Exception {
        Web3Adress tokenA = Web3Adress.from(vo.getTokenA());
        Web3Adress tokenB = Web3Adress.from(vo.getTokenB());

        BigInteger ethReserve = BigInteger.ZERO;
        if (Web3Constants.Adress.WETH.equals(tokenA)) {
            ethReserve = uniswapPairContract.getReserves().send().component1();
        } else if (Web3Constants.Adress.WETH.equals(tokenB)) {
            ethReserve = uniswapPairContract.getReserves().send().component2();
        }

        // if reserve gets to low, drop pair from db
        if (Convert.toWei("70", Unit.ETHER).compareTo(new BigDecimal(ethReserve)) > 0) {
            db.remove(vo, LiquidityPoolVO.class);
        }
    }

    private void insertIUniswapV2Pair(DexEnum dex, IUniswapV2Pair uniswapPairContract, Web3Adress pairAdress)
            throws Exception {
        Web3Adress tokenA = Web3Adress.from(uniswapPairContract.token0().send());
        Web3Adress tokenB = Web3Adress.from(uniswapPairContract.token1().send());

        // Only persist pairs, where one of the tokens is WETH and the reserve is high
        // enough
        BigInteger ethReserve = BigInteger.ZERO;
        if (Web3Constants.Adress.WETH.equals(tokenA)) {
            ethReserve = uniswapPairContract.getReserves().send().component1();
        } else if (Web3Constants.Adress.WETH.equals(tokenB)) {
            ethReserve = uniswapPairContract.getReserves().send().component2();
        }
        if (Convert.toWei("100", Unit.ETHER).compareTo(new BigDecimal(ethReserve)) > 0) {
            return;
        }

        // Persist token pair
        LiquidityPoolVO vo = new LiquidityPoolVO();
        vo.setAdress(pairAdress.get());
        vo.setTokenA(tokenA.get());
        vo.setTokenB(tokenB.get());
        vo.setDex(dex);

        db.insert(vo);
    }

}

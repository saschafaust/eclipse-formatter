package com.github.saschafaust.cryptoarbi;

import java.math.BigInteger;
import java.util.concurrent.BlockingQueue;

import com.github.saschafaust.cryptoarbi.db.JsonDB;
import com.github.saschafaust.cryptoarbi.db.vo.ArbitragePairVO;
import com.github.saschafaust.cryptoarbi.db.vo.LiquidityPoolVO;
import com.github.saschafaust.cryptoarbi.simulation.ArbitrageSimulator;
import com.github.saschafaust.cryptoarbi.simulation.ArbitrageSimulator.SimulationResult;
import com.github.saschafaust.cryptoarbi.web3j.Web3Adress;
import com.github.saschafaust.cryptoarbi.web3j.Web3Constants;
import com.github.saschafaust.cryptoarbi.web3j.model.ContractFactory;
import com.github.saschafaust.cryptoarbi.web3j.model.IUniswapV2Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.utils.Convert;
import org.web3j.utils.Convert.Unit;

/**
 * Checks
 */
public class ArbitrageBot implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(ArbitrageBot.class);

    private final BlockingQueue<ArbitragePairVO> queue;

    /**
     * constructor
     * 
     * @param queue BlockingQueue
     */
    public ArbitrageBot(BlockingQueue<ArbitragePairVO> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // iterate over queue
                // queue.take() waits until a new Element is available
                ArbitragePairVO nextVO = queue.take();
                processArbitragePairVO(nextVO);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // set interrupt flag
                logger.error("Thread got interrupted. Stop working.");
            }
        }
    }

    private void processArbitragePairVO(ArbitragePairVO vo) {

        BigInteger poolA_ethReserve = null;
        BigInteger poolA_tokenReserve = null;
        BigInteger poolB_ethReserve = null;
        BigInteger poolB_tokenReserve = null;

        // Get reserves in poolA
        Web3Adress poolA_adress = Web3Adress.from(vo.getPoolA());
        LiquidityPoolVO poolA_VO = JsonDB.instance().findById(poolA_adress.get(), LiquidityPoolVO.class);
        IUniswapV2Pair poolA_contract = ContractFactory.get().contract(IUniswapV2Pair.class, poolA_adress);
        Tuple3<BigInteger, BigInteger, BigInteger> poolA_reserves;
        try {
            poolA_reserves = poolA_contract.getReserves().send();
        } catch (Exception e) {
            logger.error(vo.getToken() + "Error while fetching pool reserves for pool " + poolA_adress.get(), e);
            return;
        }
        if (Web3Constants.Adress.WETH.equals(Web3Adress.from(poolA_VO.getTokenA()))) {
            poolA_ethReserve = poolA_reserves.component1();
            poolA_tokenReserve = poolA_reserves.component2();
        } else {
            poolA_ethReserve = poolA_reserves.component2();
            poolA_tokenReserve = poolA_reserves.component1();
        }

        // Get reserves in poolB
        Web3Adress poolB_adress = Web3Adress.from(vo.getPoolB());
        LiquidityPoolVO poolB_VO = JsonDB.instance().findById(poolB_adress.get(), LiquidityPoolVO.class);
        IUniswapV2Pair poolB_contract = ContractFactory.get().contract(IUniswapV2Pair.class, poolB_adress);
        Tuple3<BigInteger, BigInteger, BigInteger> poolB_reserves;
        try {
            poolB_reserves = poolB_contract.getReserves().send();
        } catch (Exception e) {
            logger.error(vo.getToken() + "Error while fetching pool reserves for pool " + poolB_adress.get(), e);
            return;
        }
        if (Web3Constants.Adress.WETH.equals(Web3Adress.from(poolB_VO.getTokenA()))) {
            poolB_ethReserve = poolB_reserves.component1();
            poolB_tokenReserve = poolB_reserves.component2();
        } else {
            poolB_ethReserve = poolB_reserves.component2();
            poolB_tokenReserve = poolB_reserves.component1();
        }

        // simulate arbitrage trade
        SimulationResult simulationResult = new ArbitrageSimulator().simulate(poolA_ethReserve, poolA_tokenReserve,
                poolB_ethReserve, poolB_tokenReserve);

        logger.info(vo.getToken() + ";Simulation Result for " + simulationResult.toString());

        if (simulationResult.surplus().compareTo(BigInteger.ZERO) > 0) {
            // call arbitrage contract
            System.err.println(vo.getToken() + ";" + simulationResult.toString());
        }

        // Check if pools still have enough ETH
        BigInteger threshold = Convert.toWei("70", Unit.ETHER).toBigInteger();
        if (poolA_ethReserve.compareTo(threshold) < 0 || poolB_ethReserve.compareTo(threshold) < 0) {
            // if not, remove it from DB
            JsonDB.instance().remove(vo, ArbitragePairVO.class);
            logger.info(vo.getToken() + "Removed pool, because its ETH reserve is to low.");
        } else { // add it to the end of the queue
            queue.add(vo);
        }

    }

}

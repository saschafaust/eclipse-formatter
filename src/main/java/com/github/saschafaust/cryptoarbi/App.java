package com.github.saschafaust.cryptoarbi;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.github.saschafaust.cryptoarbi.db.JsonDB;
import com.github.saschafaust.cryptoarbi.db.vo.ArbitragePairVO;

/**
 * Hello world!
 *
 */
public class App {

    public static void main(String[] args) throws Exception {
        boolean isRunning = true;

        BlockingQueue<ArbitragePairVO> queue = new LinkedBlockingQueue<>();

        // load all ArbitragePairVOs from db and add them to the queue
        List<ArbitragePairVO> allArbitragePairVOs = JsonDB.instance().findAll(ArbitragePairVO.class);
        queue.addAll(allArbitragePairVOs);

        // start arbitrage bot
        Thread arbitrageBotThread = new Thread(new ArbitrageBot(queue));
        arbitrageBotThread.start();

        // wait for shutdown
        while (isRunning) {
            isRunning = arbitrageBotThread.isAlive();
            Thread.sleep(1000);
        }

    }

}

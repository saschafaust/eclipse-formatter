package com.github.saschafaust.cryptoarbi.simulation;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import org.web3j.utils.Convert;
import org.web3j.utils.Convert.Unit;

/**
 * Can simulate a arbitrage trade to get an estimate for the result.
 */
public class ArbitrageSimulator {

    private static final BigInteger TX_FEE = Convert.toWei("0.01", Unit.ETHER).toBigInteger();

    /**
     * Simulates a trade in the
     */
    public SimulationResult simulate(BigInteger poolA_ethReserve, BigInteger poolA_tokenReserve,
            BigInteger poolB_ethReserve, BigInteger poolB_tokenReserve) {
        SimulationResult result = new SimulationResult();

        // calculate invariant
        BigDecimal invariant = new BigDecimal(poolB_ethReserve.multiply(poolA_tokenReserve)) //
                .divide(new BigDecimal(poolB_tokenReserve.multiply(poolA_ethReserve)), 18, RoundingMode.DOWN);
        result.invariant = invariant;

        // swap from poolA to poolB or vice versa ?
        boolean a2b = invariant.compareTo(BigDecimal.ONE) > 0;
        result.a2b = a2b;

        // simulate swaps
        BigInteger surplus = a2b //
                ? executeArbitrage(poolA_ethReserve, poolA_tokenReserve, poolB_ethReserve, poolB_tokenReserve) //
                : executeArbitrage(poolB_ethReserve, poolB_tokenReserve, poolA_ethReserve, poolA_tokenReserve);
        result.surplus = surplus;

        return result;
    }

    private BigInteger executeArbitrage(BigInteger poolA_ethReserve, BigInteger poolA_tokenReserve,
            BigInteger poolB_ethReserve, BigInteger poolB_tokenReserve) {

        // Calc amount to swap
        // (Only use 1% of eth reserve in the pool with lower eth reserve)
        BigInteger loanValue = poolA_ethReserve.compareTo(poolB_ethReserve) < 0
                ? poolA_ethReserve.divide(BigInteger.valueOf(100)) : poolB_ethReserve.divide(BigInteger.valueOf(100));

        // Fee on Flashloan
        BigInteger loanReturn = calcFlashLoanReturn(loanValue);

        // Swap eth -> token
        BigInteger tokenAmountAfterSwap = calcSwap(loanValue, poolA_ethReserve, poolA_tokenReserve);

        // Swap token -> eth
        BigInteger ethAmountAfterSwap = calcSwap(tokenAmountAfterSwap, poolB_tokenReserve, poolB_ethReserve);

        return ethAmountAfterSwap.subtract(loanReturn).subtract(TX_FEE);
    }

    public BigInteger calcFlashLoanReturn(BigInteger loanValue) {
        return loanValue.multiply(BigInteger.valueOf(10009)).divide(BigInteger.valueOf(10000));
    }

    public BigInteger calcSwap(BigInteger amountToSwap, BigInteger fromReserve, BigInteger toReserve) {
        // return = amountToSwap * x/y
        BigInteger x = toReserve.multiply(fromReserve.subtract(amountToSwap)).multiply(BigInteger.valueOf(997));
        BigInteger y = fromReserve.multiply(fromReserve).multiply(BigInteger.valueOf(1000));
        return amountToSwap.multiply(x).divide(y);
    }

    public static class SimulationResult {

        private BigDecimal invariant;
        private boolean a2b;
        private BigInteger surplus;

        public BigDecimal invariant() {
            return invariant;
        }

        public boolean a2b() {
            return a2b;
        }

        public BigInteger surplus() {
            return surplus;
        }

        @Override
        public String toString() {
            return new StringBuilder() //
                    .append("invariant=").append(invariant).append(";") //
                    .append("a2b=").append(a2b).append(";") //
                    .append("surplus=").append(Convert.fromWei(surplus.toString(10), Unit.ETHER)) //
                    .toString();
        }

    }

}

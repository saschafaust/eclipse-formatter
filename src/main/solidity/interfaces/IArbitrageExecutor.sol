// SPDX-License-Identifier: UNLICENSED
pragma solidity >=0.6.6;

interface IArbitrageExecutor {
    function execute(
        address token,
        address routerA,
        address routerB
    ) external;
}

package com.example.demo.strategy;

import com.example.demo.domain.StrategyRun;
import com.example.demo.domain.candlestick.CustomCandlestick;
import com.example.demo.service.CandlestickService;
import com.example.demo.service.StrategyRunService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.example.demo.Main.*;
import static com.example.demo.Values.*;

@Component
@Scope("prototype")
public class MomentumStrategy {

    @Autowired
    CandlestickService candlestickService;

    @Autowired
    StrategyRunService strategyRunService;

    private double balanceOpt;
    private double balancePes;
    private int splitCounter;
    private Object internalLock;
//    private long simOpenTime;
//    private long simCloseTime;


    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    private double changeOptimistic;
    private double changePessimistic;

    @Getter
    private boolean available = true;

    public void startSimulation(double percentageTrigger, double profitTrigger, double stopLossTrigger, double deviance, double initialBalance)  {
//        Future<?> futureOptimistic = executorService.submit(() -> {
//            startThread(percentageTrigger, profitTrigger, stopLossTrigger, deviance, true, initialBalance);
//        });
//
//        Future<?> futurePessimistic = executorService.submit(() -> {
//            startThread(percentageTrigger, profitTrigger, stopLossTrigger, deviance, false, initialBalance);
//        });
//
//        futureOptimistic.get();
//        futurePessimistic.get();
//
//        StrategyRun strategyRun = new StrategyRun();
//        strategyRun.setPercentageTrigger(percentageTrigger);
//        strategyRun.setProfitTrigger(profitTrigger);
//        strategyRun.setStopLossTrigger(stopLossTrigger);
//        strategyRun.setDeviance(deviance);
//
//        changeOptimistic = ((balanceOpt - initialBalance) * 100) / initialBalance;
//        changePessimistic = ((balancePes - initialBalance) * 100) / initialBalance;
//        strategyRun.setChangeOptimistic(changeOptimistic);
//        strategyRun.setChangePessimistic(changePessimistic);
//
//        strategyRun.setId(null);
//
//        strategyRunService.save(strategyRun);

        System.out.println("LOLOLOLO");
    }


    public void startThread(double percentageTrigger, double profitTrigger, double stopLossTrigger, double deviance, boolean optimistic, double balance) {
//        System.out.println("\n\n-------------------------\n\n");
//        System.out.println("STARTING SIMULATION");
//        System.out.println("\n\n-------------------------\n\n");
//
//        System.out.println("\n\n-------------------------\n\n");
//        System.out.println("FROM |" + new Date(candlestickService.getOpenDate(coins.get(0).getClass().getSimpleName())));
//        System.out.println("TO |" + new Date(candlestickService.getCloseDate(coins.get(0).getClass().getSimpleName())));
//        System.out.println("\n\n-------------------------\n\n");


        balanceOpt = balance;
        balancePes = balance;
        splitCounter = 0;


        int firstCandleId = 1;
        int lastCandleId = candlestickService.getLastMinute(coins.get(0).getClass().getSimpleName());

        CustomCandlestick currentCandle;

        double open;

        double yesterdayOpen;

        double percentageChange;
        double oneDayHigh;
        double twoDayHigh;

        for (int i = firstCandleId + TWO_DAYS; i < lastCandleId; i++) {
            for (CustomCandlestick coin : coins) {
                String name = coin.getClass().getSimpleName();
                currentCandle = candlestickService.getById(i, name);

                open = currentCandle.getOpen();
                yesterdayOpen = candlestickService.getById(i - ONE_DAY, name).getOpen();

                percentageChange = ((open - yesterdayOpen) * 100) / yesterdayOpen;

                if (percentageChange > percentageTrigger) {
                    oneDayHigh = candlestickService.get24hHigh(i, name);
                    if (currentCandle.getHigh() > oneDayHigh) {
                        twoDayHigh = candlestickService.get48hHigh(i, name);
                        if (oneDayHigh > twoDayHigh) {
                            double previousBalance = balance;
//                            System.out.println("\n-------------------------");
//                            System.out.println("BOUGHT " + name + " |" + new Date(currentCandle.getOpenTime()).toString());
                            i = buyCoin(i, lastCandleId, oneDayHigh, name, profitTrigger, stopLossTrigger, deviance, optimistic);
//                            System.out.println("Previous Balance:" + previousBalance + "|Current Balance:" + balance);
//                            System.out.println("-------------------------\n");
                        }
                    }
                }
            }
        }

        printResults(optimistic);
    }

    private void printResults(boolean optimistic) {
        synchronized (lock) {
            System.out.println("\n\n---------------------------------");
            System.out.println("Timeline was split " + splitCounter + " times | Optimistic:" + optimistic);
            if(optimistic){
                System.out.println("Final Balance is " + balanceOpt + "$");
            }else{
                System.out.println("Final Balance is " + balancePes + "$");
            }
        }
    }

    public int buyCoin(int index, int lastMinute, double priceBought, String name, double profitTrigger, double stopLossTrigger, double deviance, boolean optimistic) {

        CustomCandlestick currentCandle;
        boolean profit = false;
        boolean stopLoss = false;
        double stopLossTriger = priceBought - priceBought * stopLossTrigger;
        double previousHigh = priceBought * profitTrigger + priceBought;

        for (int i = index; i < lastMinute; i++) {

            currentCandle = candlestickService.getById(i, name);

            if (currentCandle.getHigh() >= previousHigh) {
                profit = true;
            }

            if (currentCandle.getLow() <= stopLossTriger) {
                stopLoss = true;
            }

            if (profit && stopLoss) {
                if (optimistic) {
                    splitCounter++;
                    // INCREASE TRAIL
                    double percentageChange = (currentCandle.getHigh() - priceBought) / priceBought;

                    stopLossTriger = percentageChange - deviance;
                    stopLossTriger = (priceBought * stopLossTriger) + priceBought;

                    // STOP LOSS
                    percentageChange = (stopLossTriger - priceBought) / priceBought;

                    balanceOpt = balanceOpt + (balanceOpt * percentageChange);
                    // System.out.println("SOLD " + name + " | " + new Date(currentCandle.getOpenTime()));
                    return i;
                } else {
                    // STOP LOSS
                    double percentageChange = (stopLossTriger - priceBought) / priceBought;

                    balancePes = balancePes + (balancePes * percentageChange);
                    // System.out.println("SOLD " + name + " | " + new Date(currentCandle.getOpenTime()));
                    return i;
                }
            } else if (profit) {
                // INCREASE TRAIL
                double percentageChange = (currentCandle.getHigh() - priceBought) / priceBought;

                stopLossTriger = percentageChange - deviance;
                stopLossTriger = (priceBought * stopLossTriger) + priceBought;

                // CHECK IF CLOSE PRICE IS LOWER THAN NEW STOP LOSS TRIGGER
                if (currentCandle.getClose() <= stopLossTriger) {
                    percentageChange = (stopLossTriger - priceBought) / priceBought;
                    if (optimistic) {
                        balanceOpt = balanceOpt + (balanceOpt * percentageChange);
                    } else {
                        balancePes = balancePes + (balancePes * percentageChange);
                    }

                    // System.out.println("SOLD " + name + " | " + new Date(currentCandle.getOpenTime()));
                    return i;
                }

                // RESET PROFIT TRIGGER
                previousHigh = currentCandle.getHigh();
                profit = false;
            } else if (stopLoss) {
                double percentageChange = (stopLossTriger - priceBought) / priceBought;

                if (optimistic) {
                    balanceOpt = balanceOpt + (balanceOpt * percentageChange);
                } else {
                    balancePes = balancePes + (balancePes * percentageChange);
                }
                // System.out.println("SOLD " + name + " | " + new Date(currentCandle.getOpenTime()));
                return i;
            }
        }

        return lastMinute;
    }


}

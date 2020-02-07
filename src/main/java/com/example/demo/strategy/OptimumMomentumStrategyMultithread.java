package com.example.demo.strategy;

import com.example.demo.domain.StrategyRun;
import com.example.demo.domain.candlestick.CustomCandlestick;
import com.example.demo.service.CandlestickService;
import com.example.demo.service.StrategyRunService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.demo.Main.lock;
import static com.example.demo.Main.start;
import static com.example.demo.Values.*;

@Component
@Scope("prototype")
public class OptimumMomentumStrategyMultithread {

    @Autowired
    CandlestickService candlestickService;

    @Autowired
    StrategyRunService strategyRunService;


    private int splitCounter;

    private double changeOptimistic;
    private double changePessimistic;

    private ExecutorService executorService;

    private int universalIndex;

    private int boughtSameMinute = 0;


    private Map<String, Integer> startFuture(int firstCandleId, int lastCandleId, String name, double percentageTrigger) {
        CustomCandlestick currentCandle;

        double open;

        double yesterdayOpen;

        double percentageChange;
        double oneDayHigh;
        double twoDayHigh;


        for (int i = firstCandleId + TWO_DAYS; i < lastCandleId; i++) {
            currentCandle = candlestickService.getById(i, name);

            open = currentCandle.getOpen();
            yesterdayOpen = candlestickService.getById(i - ONE_HOUR, name).getOpen();

            percentageChange = ((open - yesterdayOpen) * 100) / yesterdayOpen;

            if (percentageChange > percentageTrigger) {
                oneDayHigh = candlestickService.get24hHigh(i, name);
                if (currentCandle.getHigh() > oneDayHigh) {
                    twoDayHigh = candlestickService.get48hHigh(i, name);
                    if (oneDayHigh > twoDayHigh) {
                        Map<String, Integer> map = new HashMap<>();
                        map.put(name, i);
                        return map;
                    }
                }

            }
        }
        Map<String, Integer> map = new HashMap<>();
        map.put(name, lastCandleId);
        return map;
    }


    public boolean startSimulation(double percentageTrigger, double profitTrigger, double stopLossTrigger, double deviance, double initialBalance, long firstOpen, long lastOpen) throws ExecutionException, InterruptedException {
        executorService = Executors.newFixedThreadPool(coins.size());

        splitCounter = 0;
        Map<String, Integer> mapIndexes = new HashMap<>();
        Map<String, CompletableFuture<Map<String, Integer>>> mapFutures = new HashMap<>();

        int firstCandleId = candlestickService.getFirstMinute(firstOpen, coins.get(0).getClass().getSimpleName());
        int lastCandleId = candlestickService.getLastMinute(lastOpen, coins.get(0).getClass().getSimpleName());
        String minCoin = "null";
        int minIndex = Integer.MAX_VALUE;
        List<CompletableFuture<Map<String, Integer>>> futures = new ArrayList<>();
        CompletableFuture<Map<String, Integer>> toRemove = null;


        universalIndex = firstCandleId;
        while (universalIndex < lastCandleId) {
            for (CustomCandlestick coin : coins) {
                if (coin.getClass().getSimpleName().equals(minCoin) || minCoin.equals("null")) {
                    futures.add(CompletableFuture.supplyAsync(() -> {
                        return startFuture(universalIndex, lastCandleId, coin.getClass().getSimpleName(), percentageTrigger);
                    }, executorService));
                } else {
                    if (mapIndexes.get(coin.getClass().getSimpleName()) <= minIndex) {
                        futures.remove(mapFutures.get(coin.getClass().getSimpleName()));
                        futures.add(CompletableFuture.supplyAsync(() -> {
                            return startFuture(universalIndex, lastCandleId, coin.getClass().getSimpleName(), percentageTrigger);
                        }, executorService));
                    }
                }
            }

            minIndex = Integer.MAX_VALUE;


            for (CustomCandlestick coin : coins) {
                for (CompletableFuture<Map<String, Integer>> future : futures) {
                    if (future.get().get(coin.getClass().getSimpleName()) != null) {
                        int currentIndex = future.get().get(coin.getClass().getSimpleName());
                        if (currentIndex < minIndex) {
                            minIndex = currentIndex;
                            minCoin = coin.getClass().getSimpleName();
                            toRemove = future;
                        } else if (currentIndex == minIndex) {
                            boughtSameMinute++;
                        }
                        mapIndexes.put(coin.getClass().getSimpleName(), currentIndex);
                        mapFutures.put(coin.getClass().getSimpleName(), future);
                    }
                }
            }
            futures.remove(toRemove);
            if (minIndex == lastCandleId) {
                break;
            }
            universalIndex = buyCoin(minIndex, lastCandleId, candlestickService.get24hHigh(minIndex, minCoin), minCoin, profitTrigger, stopLossTrigger, deviance);
        }
        executorService.shutdown();

        StrategyRun strategyRun = new StrategyRun();
        strategyRun.setPercentageTrigger(percentageTrigger);
        strategyRun.setProfitTrigger(profitTrigger);
        strategyRun.setStopLossTrigger(stopLossTrigger);
        strategyRun.setDeviance(deviance);
        strategyRun.setSplitTimes(splitCounter);
        strategyRun.setOpenTime(firstOpen);
        strategyRun.setCloseTime(lastOpen);
        strategyRun.setChangeOptimistic(changeOptimistic);
        strategyRun.setChangePessimistic(changePessimistic);
        strategyRun.setBoughtSameTime(boughtSameMinute);

        strategyRun.setId(null);

        strategyRunService.save(strategyRun);

        printStats();

        return true;
    }

    public void printStats() {
        synchronized (lock) {
            long elapsedTime = System.currentTimeMillis() - start;
            Date date = new Date(elapsedTime);
            DateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            String dateFormatted = formatter.format(date);
            System.out.println("TIME ELAPSED: " + dateFormatted);
        }
    }

    public int buyCoin(int index, int lastMinute, double priceBought, String name, double profitTrigger, double stopLossTriggerInput, double deviance) {

        CustomCandlestick currentCandle;
        boolean profit = false;
        boolean stopLoss = false;
        double stopLossTrigger = priceBought - priceBought * stopLossTriggerInput;
        double previousHigh = priceBought * profitTrigger + priceBought;

        for (int i = index; i < lastMinute; i++) {

            currentCandle = candlestickService.getById(i, name);

            if (currentCandle.getHigh() >= previousHigh) {
                profit = true;
            }

            if (currentCandle.getLow() <= stopLossTrigger) {
                stopLoss = true;
            }

            if (profit && stopLoss) {
                double initialStopLossTrigger = stopLossTrigger;

                splitCounter++;
                // OPTIMISTIC
                double percentageChange = (currentCandle.getHigh() - priceBought) / priceBought;
                stopLossTrigger = percentageChange - deviance;
                stopLossTrigger = (priceBought * stopLossTrigger) + priceBought;
                percentageChange = (stopLossTrigger - priceBought) / priceBought;

                changeOptimistic = changeOptimistic + (percentageChange * 100);


                // PESSIMISTIC
                percentageChange = (initialStopLossTrigger - priceBought) / priceBought;
                changePessimistic = changePessimistic + (percentageChange * 100);
                return i;

            } else if (profit) {
                // INCREASE TRAIL
                double percentageChange = (currentCandle.getHigh() - priceBought) / priceBought;

                stopLossTrigger = percentageChange - deviance;
                stopLossTrigger = (priceBought * stopLossTrigger) + priceBought;

                // CHECK IF CLOSE PRICE IS LOWER THAN NEW STOP LOSS TRIGGER
                if (currentCandle.getClose() <= stopLossTrigger) {
                    percentageChange = (stopLossTrigger - priceBought) / priceBought;
                    changeOptimistic = changeOptimistic + (percentageChange * 100);
                    changePessimistic = changePessimistic + (percentageChange * 100);
                    return i;
                }
                // RESET PROFIT TRIGGER
                previousHigh = currentCandle.getHigh();
                profit = false;
            } else if (stopLoss) {
                double percentageChange = (stopLossTrigger - priceBought) / priceBought;
                changeOptimistic = changeOptimistic + (percentageChange * 100);
                changePessimistic = changePessimistic + (percentageChange * 100);
                return i;
            }
        }
        return lastMinute;
    }


}

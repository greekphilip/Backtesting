package com.example.demo.strategy;

import com.example.demo.domain.candlestick.CustomCandlestick;
import com.example.demo.service.CandlestickService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.demo.Main.start;
import static com.example.demo.Values.*;

@Component
public class SingleMomentumStrategyMultithread {

    @Autowired
    CandlestickService candlestickService;

    @Autowired
    ApplicationContext applicationContext;

    private ExecutorService executorService;

    private double balanceOpt;
    private double balancePes;
    private int splitCounter;


    private Set<Integer> set = new HashSet<>();
    private double changeOptimistic;
    private double changePessimistic;
    @Getter
    private int universalIndex;


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
            yesterdayOpen = candlestickService.getById(i - ONE_DAY, name).getOpen();

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

    public void startSimulation(double percentageTrigger, double profitTrigger, double stopLossTrigger, double deviance, double balance, long firstOpen, long lastOpen) throws ExecutionException, InterruptedException {
        executorService = Executors.newFixedThreadPool(coins.size());

        System.out.println("------------------------");
        System.out.println("Starting Simulation");
        System.out.println("------------------------");

        balanceOpt = balance;
        balancePes = balance;
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
        printResults();
        executorService.shutdown();
    }

    private void printResults() {
        System.out.println("\n\n---------------------------------");
        System.out.println("Timeline was split " + splitCounter + " times");
        System.out.println("Optimistic final Balance is " + balanceOpt + "$");
        System.out.println("Pessimistic final Balance is " + balancePes + "$");

        System.out.println("Optimistic Change is " + changeOptimistic + "$");
        System.out.println("Pessimistic Change is " + changePessimistic + "$");

        long elapsedTime = System.currentTimeMillis() - start;
        Date date = new Date(elapsedTime);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dateFormatted = formatter.format(date);
        System.out.println("TIME ELAPSED: " + dateFormatted);
    }

    public int buyCoin(int index, int lastMinute, double priceBought, String name, double profitTrigger, double stopLossTriggerPercentage, double deviance) {

        CustomCandlestick currentCandle;
        boolean profit = false;
        boolean stopLoss = false;
        double stopLossTrigger = priceBought - priceBought * stopLossTriggerPercentage;
        double previousHigh = priceBought * profitTrigger + priceBought;
        double initialStopLossTrigger;
        for (int i = index; i < lastMinute; i++) {

            currentCandle = candlestickService.getById(i, name);

            if (currentCandle.getHigh() >= previousHigh) {
                profit = true;
            }

            if (currentCandle.getLow() <= stopLossTrigger) {
                stopLoss = true;
            }

            if (profit && stopLoss) {
                initialStopLossTrigger = stopLossTrigger;
                splitCounter++;

                // OPTIMISTIC TIMELINE
                // INCREASE TRAIL
                double percentageChange = (currentCandle.getHigh() - priceBought) / priceBought;

                stopLossTrigger = percentageChange - deviance;
                stopLossTrigger = (priceBought * stopLossTrigger) + priceBought;

                // STOP LOSS
                percentageChange = (stopLossTrigger - priceBought) / priceBought;
                changeOptimistic=changeOptimistic + (percentageChange*100);

                balanceOpt = balanceOpt + (balanceOpt * percentageChange);


                // PESSIMISTIC TIMELINE
                // STOP LOSS
                percentageChange = (initialStopLossTrigger - priceBought) / priceBought;
                changePessimistic=changePessimistic + (percentageChange*100);
                balancePes = balancePes + (balancePes * percentageChange);
                return i;

            } else if (profit) {
                // INCREASE TRAIL
                double percentageChange = (currentCandle.getHigh() - priceBought) / priceBought;

                stopLossTrigger = percentageChange - deviance;
                stopLossTrigger = (priceBought * stopLossTrigger) + priceBought;

                // CHECK IF CLOSE PRICE IS LOWER THAN NEW STOP LOSS TRIGGER
                if (currentCandle.getClose() <= stopLossTrigger) {
                    percentageChange = (stopLossTrigger - priceBought) / priceBought;
                    changePessimistic=changePessimistic + (percentageChange*100);
                    changeOptimistic=changeOptimistic + (percentageChange*100);
                    balanceOpt = balanceOpt + (balanceOpt * percentageChange);
                    balancePes = balancePes + (balancePes * percentageChange);
                    return i;
                }

                // RESET PROFIT TRIGGER
                previousHigh = currentCandle.getHigh();
                profit = false;
            } else if (stopLoss) {
                double percentageChange = (stopLossTrigger - priceBought) / priceBought;
                changePessimistic=changePessimistic + (percentageChange*100);
                changeOptimistic=changeOptimistic + (percentageChange*100);
                balanceOpt = balanceOpt + (balanceOpt * percentageChange);
                balancePes = balancePes + (balancePes * percentageChange);
                return i;
            }
        }

        return lastMinute;
    }


}

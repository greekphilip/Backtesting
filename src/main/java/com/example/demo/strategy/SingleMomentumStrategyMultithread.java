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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

    private Integer numberOfActiveCoins;
//    private long simOpenTime;
//    private long simCloseTime;

    private Set<Integer> set = new HashSet<>();
    private double changeOptimistic;
    private double changePessimistic;
    @Getter
    private int universalIndex;


    private int startFuture(int i) {

        return 0;
    }

    public boolean startSimulation(double percentageTrigger, double profitTrigger, double stopLossTrigger, double deviance, double balance, long firstOpen, long lastOpen) {


        executorService = Executors.newFixedThreadPool(coins.size());

        System.out.println("------------------------");
        System.out.println("Starting Simulation");
        System.out.println("------------------------");

        balanceOpt = balance;
        balancePes = balance;
        splitCounter = 0;


        int firstCandleId = candlestickService.getFirstMinute(firstOpen, coins.get(0).getClass().getSimpleName());
        int lastCandleId = candlestickService.getLastMinute(lastOpen, coins.get(0).getClass().getSimpleName());

        List<Future<?>> futures = new ArrayList<>();

        for (int i = firstCandleId + TWO_DAYS; i < lastCandleId; i++) {
            universalIndex = i;
            for (CustomCandlestick coin : coins) {
                futures.add(executorService.submit(applicationContext.getBean(SimulationJob.class)));
            }
        }

        for (CustomCandlestick coin : coins) {
            Future<?> future = executorService.submit(() -> {
                CustomCandlestick currentCandle;

                double open;

                double yesterdayOpen;

                double percentageChange;
                double oneDayHigh;
                double twoDayHigh;


                for (int i = firstCandleId + TWO_DAYS; i < lastCandleId; i++) {

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
                                i = buyCoin(i, lastCandleId, oneDayHigh, name, profitTrigger, stopLossTrigger, deviance);
                            }
                        }

                    }
                }
            });
            futures.add(future);
        }

        for (Future future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        executorService.shutdown();
        printResults();
        return true;
    }

    private void printResults() {
        System.out.println("\n\n---------------------------------");
        System.out.println("Timeline was split " + splitCounter + " times");
        System.out.println("Optimistic final Balance is " + balanceOpt + "$");
        System.out.println("Pessimistic final Balance is " + balancePes + "$");

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

                balanceOpt = balanceOpt + (balanceOpt * percentageChange);


                // PESSIMISTIC TIMELINE
                // STOP LOSS
                percentageChange = (initialStopLossTrigger - priceBought) / priceBought;

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

                    balanceOpt = balanceOpt + (balanceOpt * percentageChange);
                    balancePes = balancePes + (balancePes * percentageChange);
                    return i;
                }

                // RESET PROFIT TRIGGER
                previousHigh = currentCandle.getHigh();
                profit = false;
            } else if (stopLoss) {
                double percentageChange = (stopLossTrigger - priceBought) / priceBought;

                balanceOpt = balanceOpt + (balanceOpt * percentageChange);
                balancePes = balancePes + (balancePes * percentageChange);
                return i;
            }
        }

        return lastMinute;
    }


}

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

import static com.example.demo.Main.lock;
import static com.example.demo.Main.start;
import static com.example.demo.Values.*;

@Component
@Scope("prototype")
public class OptimumMomentumStrategy {

    @Autowired
    CandlestickService candlestickService;

    @Autowired
    StrategyRunService strategyRunService;


    private int splitCounter;

    private double changeOptimistic;
    private double changePessimistic;

    @Getter
    private boolean available = true;


    public boolean startSimulation(double percentageTrigger, double profitTrigger, double stopLossTrigger, double deviance, double initialBalance, long firstOpen, long lastOpen) {

        splitCounter = 0;

        int firstCandleId = candlestickService.getFirstMinute(firstOpen, coins.get(0).getClass().getSimpleName());
        int lastCandleId = candlestickService.getLastMinute(lastOpen, coins.get(0).getClass().getSimpleName());

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
                            i = buyCoin(i, lastCandleId, oneDayHigh, name, profitTrigger, stopLossTrigger, deviance);
                        }
                    }
                }
            }
        }

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

    public int buyCoin(int index, int lastMinute, double priceBought, String name, double profitTrigger, double stopLossTrigger, double deviance) {

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
                double initialStopLossTrigger = stopLossTrigger;

                splitCounter++;
                // OPTIMISTIC
                double percentageChange = (currentCandle.getHigh() - priceBought) / priceBought;
                stopLossTriger = percentageChange - deviance;
                stopLossTriger = (priceBought * stopLossTriger) + priceBought;
                percentageChange = (stopLossTriger - priceBought) / priceBought;

                changeOptimistic = changeOptimistic + (percentageChange * 100);


                // PESSIMISTIC
                percentageChange = (initialStopLossTrigger - priceBought) / priceBought;
                changePessimistic = changePessimistic + (percentageChange * 100);
                return i;

            } else if (profit) {
                // INCREASE TRAIL
                double percentageChange = (currentCandle.getHigh() - priceBought) / priceBought;

                stopLossTriger = percentageChange - deviance;
                stopLossTriger = (priceBought * stopLossTriger) + priceBought;

                // CHECK IF CLOSE PRICE IS LOWER THAN NEW STOP LOSS TRIGGER
                if (currentCandle.getClose() <= stopLossTriger) {
                    percentageChange = (stopLossTriger - priceBought) / priceBought;
                    changeOptimistic = changeOptimistic + (percentageChange * 100);
                    changePessimistic = changePessimistic + (percentageChange * 100);
                    return i;
                }
                // RESET PROFIT TRIGGER
                previousHigh = currentCandle.getHigh();
                profit = false;
            } else if (stopLoss) {
                double percentageChange = (stopLossTriger - priceBought) / priceBought;
                changeOptimistic = changeOptimistic + (percentageChange * 100);
                changePessimistic = changePessimistic + (percentageChange * 100);
                return i;
            }
        }
        return lastMinute;
    }


}

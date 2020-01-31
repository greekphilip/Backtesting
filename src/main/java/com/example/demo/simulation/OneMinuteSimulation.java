package com.example.demo.simulation;

import com.binance.api.client.domain.market.Candlestick;
import com.example.demo.domain.CustomCandlestick;
import com.example.demo.service.CandlestickService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

import static com.example.demo.Values.*;

@Component
public class OneMinuteSimulation {

    @Autowired
    CandlestickService candlestickService;

    private final boolean OPTIMISTIC = false;
    private double balance = 100;
    private int splitCounter = 0;


    public int getSplitCounter() {
        return splitCounter;
    }

    public double getBalance() {
        return balance;
    }

    public void startSimulation() {
        System.out.println("\n\n-------------------------\n\n");
        System.out.println("STARTING SIMULATION");
        System.out.println("\n\n-------------------------\n\n");

        System.out.println("\n\n-------------------------\n\n");
        System.out.println("FROM |"+new Date(candlestickService.getOpenDate(coins.get(0).getClass().getSimpleName())));
        System.out.println("TO |"+new Date(candlestickService.getCloseDate(coins.get(0).getClass().getSimpleName())));
        System.out.println("\n\n-------------------------\n\n");

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

                if (percentageChange > 5) {
                    oneDayHigh = candlestickService.get24hHigh(i, name);
                    if (currentCandle.getHigh() > oneDayHigh) {
                        twoDayHigh = candlestickService.get48hHigh(i, name);
                        if (oneDayHigh > twoDayHigh) {
                            double previousBalance = balance;
                            System.out.println("\n-------------------------");
                            System.out.println("BOUGHT " + name + " |" + new Date(currentCandle.getOpenTime()).toString());
                            i = buyCoin(i, lastCandleId, oneDayHigh, name);
                            System.out.println("Previous Balance:" + previousBalance + "|Current Balance:" + balance);
                            System.out.println("-------------------------\n");
                        }
                    }
                }
            }
        }
    }

    public int buyCoin(int index, int lastMinute, double priceBought, String name) {

        CustomCandlestick currentCandle = null;
        boolean profit = false;
        boolean stopLoss = false;
        double stopLossTriger = priceBought - priceBought * 0.01;
        double deviance = 0.01;
        double previousHigh = priceBought * 0.01 + priceBought;

        for (int i = index; i < lastMinute; i++) {

            currentCandle = candlestickService.getById(i, name);

            if (currentCandle.getHigh() >= previousHigh) {
                profit = true;
            }

            if (currentCandle.getLow() <= stopLossTriger) {
                stopLoss = true;
            }

            if (profit && stopLoss) {
                splitCounter++;
                if (OPTIMISTIC) {

                    // INCREASE TRAIL
                    double percentageChange = (currentCandle.getHigh() - priceBought) / priceBought;

                    stopLossTriger = percentageChange - deviance;
                    stopLossTriger = (priceBought * stopLossTriger) + priceBought;

                    // STOP LOSS
                    percentageChange = (stopLossTriger - priceBought) / priceBought;

                    balance = balance + (balance * percentageChange);
                    System.out.println("SOLD " + name + " | " + new Date(currentCandle.getOpenTime()));
                    return i;
                } else {
                    // STOP LOSS
                    double percentageChange = (stopLossTriger - priceBought) / priceBought;

                    balance = balance + (balance * percentageChange);
                    System.out.println("SOLD " + name + " | " + new Date(currentCandle.getOpenTime()));
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

                    balance = balance + (balance * percentageChange);
                    System.out.println("SOLD " + name + " | " + new Date(currentCandle.getOpenTime()));
                    return i;
                }

                // RESET PROFIT TRIGGER
                previousHigh = currentCandle.getHigh();
                profit = false;
            } else if (stopLoss) {
                double percentageChange = (stopLossTriger - priceBought) / priceBought;

                balance = balance + (balance * percentageChange);
                System.out.println("SOLD " + name + " | " + new Date(currentCandle.getOpenTime()));
                return i;
            }
        }

        return lastMinute;
    }


}

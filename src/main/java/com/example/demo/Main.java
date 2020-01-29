package com.example.demo;

import com.example.demo.domain.Candlestick;
import com.example.demo.domain.Nano;
import com.example.demo.repository.CandlestickJPARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.List;
import java.util.Scanner;

import static com.example.demo.Values.ONE_DAY;
import static com.example.demo.Values.TWO_DAYS;

@Component
public class Main {


    @Autowired
    private CandlestickJPARepository repository;

    private boolean coinBought = false;

    private final boolean OPTIMISTIC = true;
    private double balance = 100;


    @PostConstruct
    public void init() throws Exception {
        //If Database is empty then load data from file
//        if (candlestickService.isEmpty()) {
//            insertData("NANOBTC");
//        }

//        monitorCoin();
//        System.out.println("Final Balance is:   " + balance);
        System.out.println(repository.retrieve("algo").getHigh());

    }


//    public void monitorCoin() {
//        int firstCandleId = candlestickService.getFirstMinute();
//        int lastCandleId = candlestickService.getLastMinute();
//
//        Candlestick currentCandle;
//        Candlestick oneDayCandle;
//        Candlestick twoDayCandle;
//
//        double open;
//        double high;
//        double close;
//        double low;
//
//        double yesterdayOpen;
//        double yesterdayHigh;
//        double yesterdayClose;
//        double yesterdayLow;
//
//        double percentageChange;
//        double oneDayHigh;
//        double twoDayHigh;
//
//        for (int i = firstCandleId + TWO_DAYS; i < lastCandleId; i++) {
//            if (!coinBought) {
//                currentCandle = candlestickService.getById(i);
//                oneDayCandle = candlestickService.getById(i - ONE_DAY);
//
//                open = currentCandle.getOpen();
//                yesterdayOpen = oneDayCandle.getOpen();
//
//                percentageChange = ((open - yesterdayOpen) * 100) / yesterdayOpen;
//
//                if (percentageChange > 5) {
//                    oneDayHigh = candlestickService.get24hHigh(i);
//                    if (currentCandle.getHigh() > oneDayHigh) {
//                        twoDayHigh = candlestickService.get48hHigh(i);
//                        if (oneDayHigh > twoDayHigh) {
////                            System.out.println("------------------------------BOUGHT COIN-----------------------");
//                            i = buyCoin(i, lastCandleId, oneDayHigh);
//                        }
//                    }
//                }
//            }
//        }
//
//    }
//
//    public int buyCoin(int index, int lastMinute, double priceBought) {
//
//        Candlestick currentCandle;
//        boolean profit = false;
//        boolean stopLoss = false;
////        double profitTrigger = priceBought * 0.02 + priceBought;
//        double stopLossTriger = priceBought - priceBought * 0.01;
//        double deviance = 0.01;
//        double previousHigh = priceBought * 0.02 + priceBought;
//
//        for (int i = index; i < lastMinute; i++) {
//
//            currentCandle = candlestickService.getById(i);
//
//            if (currentCandle.getHigh() >= previousHigh) {
//                profit = true;
//            }
//
//            if (currentCandle.getLow() <= stopLossTriger) {
//                stopLoss = true;
//            }
//
//            if (profit && stopLoss) {
//                if (OPTIMISTIC) {
//
//                    // INCREASE TRAIL
//                    double percentageChange = (currentCandle.getHigh() - priceBought) / 100;
//
//                    stopLossTriger = percentageChange - deviance;
//                    stopLossTriger = priceBought * stopLossTriger + priceBought;
//
//                    // STOP LOSS
//                    percentageChange = (stopLossTriger - priceBought) / 100;
//
//                    balance = balance + (balance * percentageChange);
//                    return i;
//                } else {
//                    // STOP LOSS
//                    double percentageChange = (stopLossTriger - priceBought) / 100;
//
//                    balance = balance + (balance * percentageChange);
//                    return i;
//                }
//            } else if (profit) {
//                // INCREASE TRAIL
//                double percentageChange = (currentCandle.getHigh() - priceBought) / 100;
//
//                stopLossTriger = percentageChange - deviance;
//                stopLossTriger = priceBought * stopLossTriger + priceBought;
//
//                // CHECK IF CLOSE PRICE IS LOWER THAN NEW STOP LOSS TRIGGER
//                if (currentCandle.getClose() <= stopLossTriger) {
//                    percentageChange = (stopLossTriger - priceBought) / 100;
//
//                    balance = balance + (balance * percentageChange);
//                    return i;
//                }
//
//                // RESET PROFIT TRIGGER
//                previousHigh = currentCandle.getHigh();
//                profit = false;
//            } else if (stopLoss) {
//                double percentageChange = (stopLossTriger - priceBought) / 100;
//
//                balance = balance + (balance * percentageChange);
//                return i;
//            }
//        }
//        return lastMinute;
//    }
//
//
//    public void insertData(String symbol) throws FileNotFoundException {
//        File file = new File(getClass().getClassLoader().getResource("Historical/" + symbol + "history.txt").getFile());
//        Scanner input = new Scanner(new FileInputStream(file.toString()));
//        String line;
//        Candlestick candlestick = new Candlestick();
//        while (input.hasNext()) {
//            line = input.nextLine();
//            String[] array = line.split(",");
//
//            String openTime = array[0].replace("Candlestick[openTime=", "");
//            String open = array[1].replace("open=", "");
//            String high = array[2].replace("high=", "");
//            String low = array[3].replace("low=", "");
//            String close = array[4].replace("close=", "");
//
//            candlestick.setOpenTime(Long.parseLong(openTime));
//            candlestick.setOpen(Double.parseDouble(open));
//            candlestick.setHigh(Double.parseDouble(high));
//            candlestick.setLow(Double.parseDouble(low));
//            candlestick.setClose(Double.parseDouble(close));
//
//            candlestick.setId(null);
//
//            candlestickService.save(candlestick);
//        }
//    }
}

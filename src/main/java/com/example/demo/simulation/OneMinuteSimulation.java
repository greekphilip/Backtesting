//package com.example.demo.simulation;
//
//import com.binance.api.client.domain.market.Candlestick;
//import com.example.demo.domain.CustomCandlestick;
//import com.example.demo.service.CandlestickService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import static com.example.demo.Values.ONE_DAY;
//import static com.example.demo.Values.TWO_DAYS;
//
//@Component
//public class OneMinuteSimulation {
//
//    @Autowired
//    CandlestickService candlestickService;
//
//    private final boolean OPTIMISTIC = true;
//    private double balance = 100;
//    private int splitCounter=0;
//
//    public void monitorCoin() {
//        int firstCandleId = candlestickService.getFirstMinute();
//        int lastCandleId = candlestickService.getLastMinute();
//
//        CustomCandlestick currentCandle;
//        CustomCandlestick oneDayCandle;
//
//        double open;
//
//        double yesterdayOpen;
//
//        double percentageChange;
//        double oneDayHigh;
//        double twoDayHigh;
//
//        for (int i = firstCandleId + TWO_DAYS; i < lastCandleId; i++) {
//            currentCandle = candlestickService.getById(i);
//            oneDayCandle = candlestickService.getById(i - ONE_DAY);
//
//            open = currentCandle.getOpen();
//            yesterdayOpen = oneDayCandle.getOpen();
//
//            percentageChange = ((open - yesterdayOpen) * 100) / yesterdayOpen;
//
//            if (percentageChange > 5) {
//                oneDayHigh = candlestickService.get24hHigh(i);
//                if (currentCandle.getHigh() > oneDayHigh) {
//                    twoDayHigh = candlestickService.get48hHigh(i);
//                    if (oneDayHigh > twoDayHigh) {
////                            System.out.println("------------------------------BOUGHT COIN-----------------------");
//                        i = buyCoin(i, lastCandleId, oneDayHigh);
//                    }
//                }
//            }
//        }
//
//    }
//
//    public int buyCoin(int index, int lastMinute, double priceBought) {
//
//        CustomCandlestick currentCandle;
//        boolean profit = false;
//        boolean stopLoss = false;
////        double profitTrigger = priceBought * 0.02 + priceBought;
//        double stopLossTriger = priceBought - priceBought * 0.01;
//        double deviance = 0.01;
//        double previousHigh = priceBought * 0.01 + priceBought;
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
//                splitCounter++;
//                if (OPTIMISTIC) {
//
//                    // INCREASE TRAIL
//                    double percentageChange = (currentCandle.getHigh() - priceBought) / priceBought;
//
//                    stopLossTriger = percentageChange - deviance;
//                    stopLossTriger = (priceBought * stopLossTriger) + priceBought;
//
//                    // STOP LOSS
//                    percentageChange = (stopLossTriger - priceBought) / priceBought;
//
//                    balance = balance + (balance * percentageChange);
//                    return i;
//                } else {
//                    // STOP LOSS
//                    double percentageChange = (stopLossTriger - priceBought) / priceBought;
//
//                    balance = balance + (balance * percentageChange);
//                    return i;
//                }
//            } else if (profit) {
//                // INCREASE TRAIL
//                double percentageChange = (currentCandle.getHigh() - priceBought) / priceBought;
//
//                stopLossTriger = percentageChange - deviance;
//                stopLossTriger = (priceBought * stopLossTriger) + priceBought;
//
//                // CHECK IF CLOSE PRICE IS LOWER THAN NEW STOP LOSS TRIGGER
//                if (currentCandle.getClose() <= stopLossTriger) {
//                    percentageChange = (stopLossTriger - priceBought) / priceBought;
//
//                    balance = balance + (balance * percentageChange);
//                    return i;
//                }
//
//                // RESET PROFIT TRIGGER
//                previousHigh = currentCandle.getHigh();
//                profit = false;
//            } else if (stopLoss) {
//                double percentageChange = (stopLossTriger - priceBought) / priceBought;
//
//                balance = balance + (balance * percentageChange);
//                return i;
//            }
//        }
//        return lastMinute;
//    }
//
//
//
//
//}

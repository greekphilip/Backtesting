package com.example.demo;

import com.example.demo.domain.Candlestick;
import com.example.demo.service.CandlestickService;
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
    private CandlestickService candlestickService;

    private boolean coinBought = false;

    @PostConstruct
    public void init() throws Exception {
        //If Database is empty then load data from file
        if (candlestickService.isEmpty()) {
            insertData("NANOBTC");
        }

        monitorCoin();
    }


    public void monitorCoin() {
        int firstCandleId = candlestickService.getFirstMinute();
        int lastCandleId = candlestickService.getLastMinute();

        Candlestick currentCandle;
        Candlestick oneDayCandle;
        Candlestick twoDayCandle;

        double open;
        double high;
        double close;
        double low;

        double yesterdayOpen;
        double yesterdayHigh;
        double yesterdayClose;
        double yesterdayLow;

        double percentageChange;
        double oneDayHigh;
        double twoDayHigh;

        for (int i = firstCandleId + TWO_DAYS; i < lastCandleId; i++) {
            if (!coinBought) {
                currentCandle = candlestickService.getById(i);
                oneDayCandle = candlestickService.getById(i - ONE_DAY);

                open = currentCandle.getOpen();
                yesterdayOpen = oneDayCandle.getOpen();

                percentageChange = ((open - yesterdayOpen) * 100) / yesterdayOpen;

                if (percentageChange > 5) {
                    oneDayHigh = candlestickService.get24hHigh(i);
                    if (currentCandle.getHigh() >= oneDayHigh) {
                        twoDayHigh = candlestickService.get48hHigh(i);
                        if (oneDayHigh >= twoDayHigh) {
                            System.out.println("------------------------------BOUGHT COIN-----------------------");
                            //coinBought = true;
                        }
                    }
                }
            }
        }

    }


    public void insertData(String symbol) throws FileNotFoundException {
        File file = new File(getClass().getClassLoader().getResource("Historical/" + symbol + "history.txt").getFile());
        Scanner input = new Scanner(new FileInputStream(file.toString()));
        String line;
        Candlestick candlestick = new Candlestick();
        while (input.hasNext()) {
            line = input.nextLine();
            String[] array = line.split(",");

            String openTime = array[0].replace("Candlestick[openTime=", "");
            String open = array[1].replace("open=", "");
            String high = array[2].replace("high=", "");
            String low = array[3].replace("low=", "");
            String close = array[4].replace("close=", "");

            candlestick.setOpenTime(Long.parseLong(openTime));
            candlestick.setOpen(Double.parseDouble(open));
            candlestick.setHigh(Double.parseDouble(high));
            candlestick.setLow(Double.parseDouble(low));
            candlestick.setClose(Double.parseDouble(close));

            candlestick.setId(null);

            candlestickService.save(candlestick);
        }
    }
}

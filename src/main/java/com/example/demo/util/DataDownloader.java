package com.example.demo.util;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DataDownloader {

    private BinanceApiClientFactory factory;
    private BinanceApiRestClient client;

    public DataDownloader() throws ParseException, FileNotFoundException {
        factory = BinanceApiClientFactory.newInstance();
        client = factory.newRestClient();
    }


    /**
     * @param //symbol
     * @param //dateInString1 Format =  (25-07-2019 13:00:00)
     * @param //dateInString2 Format =  (10-08-2019 13:00:00)
     * @throws ParseException
     */
    public void getData(String fileName, long previousStartTime, long endTime) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        String symbol = fileName.toUpperCase() + "BTC";
//        Date date1 = sdf.parse(dateInString1);
//        Date date2 = sdf.parse(dateInString2);

        long plusMinute = 60000;
        long plus500Minutes = plusMinute * 500;
//        long previousStartTime = date1.getTime();
//        long endTime = date2.getTime();
        long iterations = (endTime - previousStartTime) / plus500Minutes + 1;


        try (FileWriter fw = new FileWriter("Historical/" + fileName + "history.txt", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter printer = new PrintWriter(bw)) {
            int counter = 0;
            for (int i = 0; i < iterations; i++) {
                if (previousStartTime < endTime) {
                    List<Candlestick> candles = client.getCandlestickBars(symbol, CandlestickInterval.ONE_MINUTE, 500, previousStartTime, endTime);
                    previousStartTime = previousStartTime + plus500Minutes;
                    for (int j = 0; j < candles.size(); j++) {
                        printer.println(candles.get(j));
                    }
                    counter++;
                    if (counter > 235) {
                        Thread.sleep(60000);
                        counter = 0;
                    }
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Something went wrong. Input Output");
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

}

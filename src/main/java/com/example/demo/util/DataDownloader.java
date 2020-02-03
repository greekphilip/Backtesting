package com.example.demo.util;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import net.spy.memcached.MemcachedClient;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


@Service
public class DataDownloader {

    private BinanceApiClientFactory factory;
    private BinanceApiRestClient client;

    private static final long plusMinute = 60000;
    private static final long plus500Minutes = plusMinute * 500;
    private MemcachedClient memcacheClient;
    private String handle = "HANDLE";
    private int limit = 1000;

    public DataDownloader() throws ParseException, IOException, IllegalAccessException {
        factory = BinanceApiClientFactory.newInstance();
        client = factory.newRestClient();
    }

    private void startMemCacheServer() throws IOException, IllegalAccessException {

        String[] commandsWindows = {
                "cmd.exe",
                "cd \"Memcached\"",
                "memcached.exe -d install",
                "memcached.exe -d start"
        };


        if (SystemUtils.IS_OS_LINUX) {
            // Start memcached docker image
        } else if (SystemUtils.IS_OS_WINDOWS) {
            System.out.println("STARTED MEMCACHED");
            ProcessBuilder builder = new ProcessBuilder(commandsWindows);
            Process p = builder.start();
        } else {
            throw new IllegalAccessException("NO OS FOUND");
        }


        memcacheClient = new MemcachedClient(new InetSocketAddress("localhost", 11211));
        System.out.println("test");

    }

    public void closeMemCache() throws IOException, IllegalAccessException {
        memcacheClient.shutdown();

        String[] commandsWindows = {
                "cmd.exe",
                "cd \"Memcached\"",
                "memcached.exe -d stop"
        };

        String[] commandsLinux = {
                ""
        };

        if (SystemUtils.IS_OS_LINUX) {
            // install memcache
        } else if (SystemUtils.IS_OS_WINDOWS) {
            ProcessBuilder builder = new ProcessBuilder(commandsWindows);
            Process p = builder.start();
        } else {
            throw new IllegalAccessException("NO OS FOUND");
        }
    }


    /**
     * @param //symbol
     * @param //dateInString1 Format =  (25-07-2019 13:00:00)
     * @param //dateInString2 Format =  (10-08-2019 13:00:00)
     * @throws ParseException
     */
    public void getData(String fileName, long previousStartTime, long endTime) throws IOException, IllegalAccessException {
        startMemCacheServer();
        System.out.println("DOWNLOADING DATA FOR " + fileName.toUpperCase());


        String symbol = fileName.toUpperCase() + "BTC";

        long iterations = (endTime - previousStartTime) / plus500Minutes + 1;
        long previousPreviousStartTime = previousStartTime;


        long start = System.currentTimeMillis();

        try (FileWriter fw = new FileWriter("Historical/" + fileName + "history.txt", false);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter printer = new PrintWriter(bw)) {
            for (int i = 0; i < iterations; i++) {

                previousPreviousStartTime = previousStartTime;
                previousStartTime = download(previousStartTime, endTime, symbol, printer);

                if (previousStartTime == previousPreviousStartTime) {
                    i--;
                } else if (previousStartTime == (long) -1) {
                    memcacheClient.shutdown();
                    break;
                }
            }
            memcacheClient.shutdown();
        } catch (IOException e) {
            System.err.println("Something went wrong. Input Output");
        }
    }


    private long download(long previousStartTime, long endTime, String symbol, PrintWriter printer) {
        if (previousStartTime < endTime) {

            Object fetchedObject = memcacheClient.get(handle);
            //FIRST TIME
            if (fetchedObject == null) {
                memcacheClient.set(handle, 60, String.valueOf(limit - 1));
                List<Candlestick> candles = client.getCandlestickBars(symbol, CandlestickInterval.ONE_MINUTE, 500, previousStartTime, endTime);
                previousStartTime = previousStartTime + plus500Minutes;
                for (int j = 0; j < candles.size(); j++) {
                    printer.println(candles.get(j));
                }
                return previousStartTime;
            }
            //REACHED LIMIT FOR THIS MINUTE
            long counter = Long.parseLong((String) fetchedObject);
            if (counter == 0) {
                return previousStartTime;
            }
            //CONTINUE DOWNLOADING
            memcacheClient.decr(handle, 1);
            List<Candlestick> candles = client.getCandlestickBars(symbol, CandlestickInterval.ONE_MINUTE, 500, previousStartTime, endTime);
            previousStartTime = previousStartTime + plus500Minutes;
            for (int j = 0; j < candles.size(); j++) {
                printer.println(candles.get(j));
            }
            return previousStartTime;
        }
        return (long) -1;
    }
}

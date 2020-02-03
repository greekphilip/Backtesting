package com.example.demo;

import com.example.demo.exception.InvalidDataException;
import com.example.demo.strategy.OptimumMomentumStrategy;
import com.example.demo.strategy.SingleMomentumStrategy;
import com.example.demo.util.DataDownloader;
import com.example.demo.util.DatabaseUtil;
import com.example.demo.util.DateConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class Main {

    @Autowired
    DatabaseUtil databaseUtil;

    private double percentage;

    private double profit;
    private double stopLoss;
    private double deviance;

    private final double MIN_PERCENTAGE = 0;
    private final double MAX_PERCENTAGE = 10;

    private final double MIN_PROFIT = 0.002;
    private final double MAX_PROFIT = 0.05;

    private final double MIN_STOP_LOSS = 0.002;
    private final double MAX_STOP_LOSS = 0.05;

    private final double MIN_DEVIANCE = 0.001;

    public static final String FIRST_DATE = "02-02-2019 13:00:00";
    public static final String LAST_DATE = "02-02-2020 13:00:00";


    private static final double initialBalance = 100;
    public final static Integer lock = 1;

    private ExecutorService executorService = Executors.newFixedThreadPool(200);

    public static long start = System.currentTimeMillis();

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    SingleMomentumStrategy singleMomentumStrategy;

    @Autowired
    DataDownloader dataDownloader;

    @Autowired
    DateConverter dateConverter;

    @PostConstruct

    public void init() throws Exception {


        long start = System.currentTimeMillis();

        long beginDate = dateConverter.getLong(FIRST_DATE);
        long endDate = dateConverter.getLong(LAST_DATE);


        if (!databaseUtil.assertData()) {
            throw new InvalidDataException("Database data is not valid.");
        }

        runSingleSimulation(beginDate, endDate);


        executorService.shutdown();
    }


    private void startSimulation(double percentage, double profit, double stopLoss, double deviance, long beginDate, long endDate) {
        executorService.submit(() -> {
            applicationContext.getBean(OptimumMomentumStrategy.class).startSimulation(percentage,
                                                                                      profit,
                                                                                      stopLoss,
                                                                                      deviance,
                                                                                      initialBalance,
                                                                                      beginDate,
                                                                                      endDate);
        });
    }

    private void manualDownload(String coinName, long begin, long end){
        try {
            dataDownloader.getData(coinName, begin, end);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    private void runSingleSimulation(long begin, long end){
        singleMomentumStrategy.startSimulation(5,
                                               0.015,
                                               0.036,
                                               0.015,
                                               100,
                                               begin,
                                               end);
    }

    private void findOptimumParameters(long begin, long end) {
        int counter = 0;


        for (percentage = MIN_PERCENTAGE; percentage <= MAX_PERCENTAGE; percentage = percentage + 1) {
            for (profit = MIN_PROFIT; profit <= MAX_PROFIT; profit = profit + 0.001) {
                for (stopLoss = MIN_STOP_LOSS; stopLoss <= MAX_STOP_LOSS; stopLoss = stopLoss + 0.001) {
                    for (deviance = MIN_DEVIANCE; deviance <= profit; deviance = deviance + 0.001) {
                        counter++;
                    }
                }
            }
        }

        System.out.println("-------------------------------------");
        System.out.println("Number of simulations:" + counter);
        System.out.println("-------------------------------------");

        for (percentage = MIN_PERCENTAGE; percentage <= MAX_PERCENTAGE; percentage = percentage + 1) {
            for (profit = MIN_PROFIT; profit <= MAX_PROFIT; profit = profit + 0.001) {
                for (stopLoss = MIN_STOP_LOSS; stopLoss <= MAX_STOP_LOSS; stopLoss = stopLoss + 0.001) {
                    for (deviance = MIN_DEVIANCE; deviance <= profit; deviance = deviance + 0.001) {
                        startSimulation(percentage, profit, stopLoss, deviance, begin, end);
                    }
                }
            }
        }
    }


}

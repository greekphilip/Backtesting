package com.example.demo;

import com.example.demo.exception.InvalidDataException;
import com.example.demo.strategy.MomentumStrategy;
import com.example.demo.util.DatabaseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
public class Main {

    @Autowired
    DatabaseUtil databaseUtil;

    private double percentage;

    private double profit;
    private double stopLoss;
    private double deviance;
    public static final boolean OPTIMISTIC = true;

//    private final double MIN_PERCENTAGE = 0;
//    private final double MAX_PERCENTAGE = 10;
//
//    private final double MIN_PROFIT = 0.002;
//    private final double MAX_PROFIT = 0.05;
//
//    private final double MIN_STOP_LOSS = 0.002;
//    private final double MAX_STOP_LOSS = 0.05;
//
//    private final double MIN_DEVIANCE = 0.001;


    public final static Integer lock = 1;
    private final double MIN_PERCENTAGE = 1;
    private final double MAX_PERCENTAGE = 5;

    private final double MIN_PROFIT = 0.001;
    private final double MAX_PROFIT = 0.04;

    private final double MIN_STOP_LOSS = 0.01;
    private final double MAX_STOP_LOSS = 0.02;

    private final double MIN_DEVIANCE = 0.01;

    private List<Future<?>> futures = new ArrayList<>();

    private ExecutorService executorService = Executors.newFixedThreadPool(500);

    public static long start = System.currentTimeMillis();

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() throws Exception {

        long start = System.currentTimeMillis();

        if (!databaseUtil.assertData()) {
            throw new InvalidDataException("Database data is not valid.");
        }


        for (percentage = MIN_PERCENTAGE; percentage < MAX_PERCENTAGE; percentage++) {
            for (profit = MIN_PROFIT; profit < MAX_PROFIT; profit = profit + 0.001) {
                for (stopLoss = MIN_STOP_LOSS; stopLoss < MAX_STOP_LOSS; stopLoss = stopLoss + 0.005) {
                    for (deviance = MIN_DEVIANCE; deviance <= profit; deviance = deviance + 0.005) {
                        futures.add(executorService.submit(() -> {
                            startSimulation(percentage, profit, stopLoss, deviance);
                        }));
                    }
                }
            }
        }

//        for (Future future : futures) {
//            future.get();
//        }

        executorService.shutdown();


    }


    private void startSimulation(double percentage, double profit, double stopLoss, double deviance) {
        applicationContext.getBean(MomentumStrategy.class).startSimulation(percentage, profit, stopLoss, deviance);
    }


}

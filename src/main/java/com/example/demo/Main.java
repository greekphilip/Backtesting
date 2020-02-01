package com.example.demo;

import com.example.demo.exception.InvalidDataException;
import com.example.demo.simulation.OneMinuteSimulation;
import com.example.demo.util.DatabaseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class Main {

    @Autowired
    DatabaseUtil databaseUtil;

    @Autowired
    OneMinuteSimulation simulator;

    public static final double PROFIT = 0.005;
    public static final double STOP_LOSS = 0.02;
    public static final double DEVIANCE = 0.005;
    public static final boolean OPTIMISTIC = true;

    @PostConstruct
    public void init() throws Exception {

        if (!databaseUtil.assertData()) {
            throw new InvalidDataException("Database data is not valid.");
        }

        simulator.startSimulation();
        System.out.println("\n\n---------------------------------");
        System.out.println("Timeline was split " + simulator.getSplitCounter() + " times | Optimistic:" + OPTIMISTIC);
        System.out.println("Final Balance is " + simulator.getBalance() + "$");
    }


}

package com.example.demo;

import com.example.demo.domain.Algo;
import com.example.demo.domain.CustomCandlestick;
import com.example.demo.exception.InvalidDataException;
import com.example.demo.repository.CandlestickJPARepository;
import com.example.demo.service.CandlestickService;
import com.example.demo.simulation.OneMinuteSimulation;
import com.example.demo.util.DataDownloader;
import com.example.demo.util.DatabaseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static com.example.demo.Values.coins;

@Component
public class Main {

    @Autowired
    DatabaseUtil databaseUtil;

    @Autowired
    OneMinuteSimulation simulator;

//    @Autowired
//    DataDownloader dataDownloader;



    @PostConstruct
    public void init() throws Exception {

        if (!databaseUtil.assertData()) {
            throw new InvalidDataException("Database data is not valid.");
        }

        simulator.startSimulation();

        System.out.println("Timeline was split "+simulator.getSplitCounter()+" times");
        System.out.println("Final Balance is "+simulator.getBalance()+"$");

       // dataDownloader.testGetData();
    }


}

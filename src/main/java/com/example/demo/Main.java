package com.example.demo;

import com.example.demo.domain.Algo;
import com.example.demo.domain.CustomCandlestick;
import com.example.demo.repository.CandlestickJPARepository;
import com.example.demo.service.CandlestickService;
import com.example.demo.util.DatabaseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static com.example.demo.Values.coins;

@Component
public class Main {


    @Autowired
    CandlestickService candlestickService;

    private boolean coinBought = false;

    private final boolean OPTIMISTIC = true;
    private double balance = 100;


    @Autowired
    DatabaseUtil databaseUtil;

    @PostConstruct
    public void init() throws Exception {

       databaseUtil.assertData();

    }


}

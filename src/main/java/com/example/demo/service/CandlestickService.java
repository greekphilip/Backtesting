package com.example.demo.service;

import com.example.demo.domain.CustomCandlestick;
import com.example.demo.repository.CandlestickJPARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import javax.transaction.Transactional;

import static com.example.demo.Values.*;

@Service
public class CandlestickService {

    @Autowired
    private CandlestickJPARepository repository;


    public double get24hHigh(int currentIndex, String table) {
        int start = currentIndex - ONE_DAY;
        int end = currentIndex;
        return repository.findHigh(start, end, table);
    }

    public double get48hHigh(int currentIndex, String table) {
        int start = currentIndex - TWO_DAYS;
        int end = currentIndex - ONE_DAY;
        return repository.findHigh(start, end, table);
    }

    public CustomCandlestick getById(int id, String table) {
        return repository.findById(id, table);
    }

    @Transactional
    public void save(CustomCandlestick candlestick) {
        repository.save(candlestick);
    }

    public long size(String table){
        return repository.count(table);
    }

    public boolean isEmpty(String table) {
        return repository.count(table) == 0 ? true : false;
    }

    public boolean atLeastOneEmpty() {
        for (CustomCandlestick coin : coins) {
            return true ? isEmpty(coin.getClass().getSimpleName()) : false;
        }
        throw new IllegalArgumentException("Something is wrong");
    }

    public int getFirstMinute(String table) {
        return repository.findFirstMinute(table);
    }

    public int getLastMinute(String table) {
        return repository.findLastMinute(table);
    }

    @Transactional
    public void deleteData(String table) {
        repository.deleteAll(table);
    }

    public long getOpenDate(String table) {
        return repository.findOpenTime(table);
    }

    public long getCloseDate(String table) {
        return repository.findCloseTime(table);
    }
}

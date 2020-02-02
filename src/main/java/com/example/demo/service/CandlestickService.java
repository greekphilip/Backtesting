package com.example.demo.service;

import com.example.demo.domain.CustomCandlestick;
import com.example.demo.repository.CandlestickJPARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import static com.example.demo.Values.*;

@Service
public class CandlestickService {

    @Autowired
    private CandlestickJPARepository repository;


    @Cacheable(cacheNames = "24high")
    public double get24hHigh(int currentIndex, String table) {
        int start = currentIndex - ONE_DAY;
        int end = currentIndex;
        return repository.findHigh(start, end, table);
    }

    @Cacheable(cacheNames = "48high")
    public double get48hHigh(int currentIndex, String table) {
        int start = currentIndex - TWO_DAYS;
        int end = currentIndex - ONE_DAY;
        return repository.findHigh(start, end, table);
    }

    @Cacheable(cacheNames = "candlestick")
    public CustomCandlestick getById(int id, String table) {
        return repository.findById(id, table);
    }

    @CachePut("save")
    @Transactional
    public void save(CustomCandlestick candlestick) {
        repository.save(candlestick);
    }

    @Cacheable("size")
    public long size(String table) {
        return repository.count(table);
    }

    @Cacheable("empty")
    public boolean isEmpty(String table) {
        return repository.count(table) == 0;
    }


    public boolean atLeastOneEmpty() {
        for (CustomCandlestick coin : coins) {
            return isEmpty(coin.getClass().getSimpleName());
        }
        throw new IllegalArgumentException("Something is wrong");
    }

    public int getFirstMinute(String table) {
        return repository.findFirstMinute(table);
    }

    @Cacheable("lastMinute")
    public int getLastMinute(String table) {
        return repository.findLastMinute(table);
    }

    @Transactional
    @CacheEvict("delete")
    public void deleteData(String table) {
        repository.deleteAll(table);
    }

    @Cacheable("openDate")
    public long getOpenDate(String table) {
        return repository.findOpenTime(table);
    }

    public long getCloseDate(String table) {
        return repository.findCloseTime(table);
    }
}

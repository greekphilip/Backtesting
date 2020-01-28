package com.example.demo.service;

import com.example.demo.domain.Candlestick;
import com.example.demo.repository.CandlestickRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.demo.Values.ONE_DAY;
import static com.example.demo.Values.TWO_DAYS;

@Service
public class CandlestickService {

    @Autowired
    private CandlestickRepository repository;

    public List<Candlestick> getAllCandlesticks() {
        List<Candlestick> list = new ArrayList<>();
        Iterable<Candlestick> iterable = repository.findAll();

        for (Candlestick candlestick : iterable) {
            list.add(candlestick);
        }
        return list;
    }

    public double get24hHigh(int currentIndex) {
        int start = currentIndex - ONE_DAY;
        int end = currentIndex;
        return repository.findHigh(start, end);
    }

    public double get48hHigh(int currentIndex) {
        int start = currentIndex - TWO_DAYS;
        int end = currentIndex - ONE_DAY;
        return repository.findHigh(start, end);
    }

    public Candlestick getById(int id) {
        Optional<Candlestick> optional = repository.findById(id);
        if (optional.isPresent())
            return repository.findById(id).get();

        throw new NullPointerException("Not Found");
    }

    public void save(Candlestick candlestick) {
        repository.save(candlestick);
    }

    public boolean isEmpty() {
        return repository.count() == 0 ? true : false;
    }

    public int getFirstMinute(){
        return repository.findFirstMinute();
    }

    public int getLastMinute(){
        return repository.findLastMinute();
    }
}

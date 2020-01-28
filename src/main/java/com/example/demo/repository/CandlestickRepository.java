package com.example.demo.repository;

import com.example.demo.domain.Candlestick;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandlestickRepository extends CrudRepository<Candlestick, Integer> {

    @Query("select max(high) from candlestick where id>= ?1 and id<= ?2")
    public double findHigh(int start, int end);

    @Query("select min(id) from candlestick")
    public int findFirstMinute();

    @Query("select max(id) from candlestick")
    public int findLastMinute();

}

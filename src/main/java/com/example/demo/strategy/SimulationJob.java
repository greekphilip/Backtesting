package com.example.demo.strategy;

import com.example.demo.domain.candlestick.CustomCandlestick;
import com.example.demo.service.CandlestickService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

import static com.example.demo.Values.ONE_DAY;

@Component
@Scope("prototype")
public class SimulationJob implements Callable<Boolean> {

    @Autowired
    CandlestickService candlestickService;

    private int i;
    private String name;
    private double percentageTrigger;

    public SimulationJob(SingleMomentumStrategyMultithread singleMomentumStrategyMultithread) {
        i = singleMomentumStrategyMultithread.getUniversalIndex();
    }

    public SimulationJob(String name, double percentageTrigger, int i) {
        this.i = i;
        this.name = name;
        this.percentageTrigger = percentageTrigger;
    }

    @Override
    public Boolean call() throws Exception {

        CustomCandlestick currentCandle = candlestickService.getById(i, name);

        double open;

        double yesterdayOpen;

        double percentageChange;
        double oneDayHigh;
        double twoDayHigh;

        open = currentCandle.getOpen();
        yesterdayOpen = candlestickService.getById(i - ONE_DAY, name).getOpen();

        percentageChange = ((open - yesterdayOpen) * 100) / yesterdayOpen;

        if (percentageChange > percentageTrigger) {
            oneDayHigh = candlestickService.get24hHigh(i, name);
            if (currentCandle.getHigh() > oneDayHigh) {
                twoDayHigh = candlestickService.get48hHigh(i, name);
                if (oneDayHigh > twoDayHigh) {
                    return true;
                }
            }

        }
        return false;
    }
}

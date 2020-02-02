package com.example.demo.service;

import com.example.demo.domain.StrategyRun;
import com.example.demo.repository.StrategyRunRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StrategyRunService {

    @Autowired
    StrategyRunRepository strategyRunRepository;

    public void save(StrategyRun strategyRun) {
        strategyRunRepository.save(strategyRun);
    }

}

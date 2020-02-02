package com.example.demo.repository;

import com.example.demo.domain.StrategyRun;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class StrategyRunRepository {

    @PersistenceContext
    EntityManager em;

    public void save(StrategyRun strategyRun) {
        em.persist(strategyRun);
    }
}

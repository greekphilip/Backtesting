package com.example.demo.repository;


import com.example.demo.domain.Candlestick;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Repository
public class CandlestickJPARepository {

    @PersistenceContext
    EntityManager em;




    public Candlestick retrieve(String table){
        Query jpqlQuery = em.createQuery("Select c from "+ table+" c where id=:id");
        jpqlQuery.setParameter("id", 1);
        return (Candlestick) jpqlQuery.getSingleResult();
    }

}

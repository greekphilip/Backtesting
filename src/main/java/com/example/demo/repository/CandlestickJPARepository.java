package com.example.demo.repository;


import com.example.demo.domain.CustomCandlestick;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Repository
public class CandlestickJPARepository {

    @PersistenceContext
    EntityManager em;


    public double findHigh(int start, int end, String table) {
        Query jpqlQuery = em.createQuery("Select max(high) from " + table + " where id>=:start and id<:end");
        jpqlQuery.setParameter("start", start);
        jpqlQuery.setParameter("end", end);
        Double result = (Double) jpqlQuery.getSingleResult();
        return result == null ? 0 : result;
    }

    public int findFirstMinute(String table) {
        Query jpqlQuery = em.createQuery("Select min(id) from " + table);
        Integer result = (Integer) jpqlQuery.getSingleResult();
        return result == null ? 0 : result;
    }

    public int findLastMinute(String table) {
        Query jpqlQuery = em.createQuery("Select max(id) from " + table);
        Integer result = (Integer) jpqlQuery.getSingleResult();
        return result == null ? 0 : result;
    }

    public CustomCandlestick findById(int id, String table) {
        Query jpqlQuery = em.createQuery("Select c from " + table + " c where id=:id");
        jpqlQuery.setParameter("id", id);
        return (CustomCandlestick) jpqlQuery.getSingleResult();
    }


    public void save(CustomCandlestick customCandlestick) {
        em.persist(customCandlestick);
    }

    public long count(String table) {
        Query jpqlQuery = em.createQuery("Select count(id) from " + table);
        return (Long) jpqlQuery.getSingleResult();
    }

    public void deleteAll(String table) {
        Query query = em.createQuery("DELETE FROM " + table);
        query.executeUpdate();
    }

    public long findOpenTime(String table) {
        Query jpqlQuery = em.createQuery("Select min(openTime) from " + table);
        Long result = (Long) jpqlQuery.getSingleResult();
        Long alt = Long.parseLong("0");
        return result == null ? alt : result;
    }

    public long findCloseTime(String table) {
        Query jpqlQuery = em.createQuery("Select max(openTime) from " + table);
        Long result = (Long) jpqlQuery.getSingleResult();
        Long alt = Long.parseLong("0");
        return result == null ? alt : result;
    }


}

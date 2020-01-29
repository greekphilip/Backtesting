package com.example.demo.domain;

import lombok.Data;

import javax.persistence.*;

@Data
@MappedSuperclass
public class CustomCandlestick {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "opentime")
    private long openTime;

    @Column(name = "open")
    private double open;

    @Column(name = "high")
    private double high;

    @Column(name = "low")
    private double low;

    @Column(name = "close")
    private double close;
}

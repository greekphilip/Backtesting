package com.example.demo.domain.candlestick;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@Data
@MappedSuperclass
public class CustomCandlestick {


    @Id
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

    @Column(name = "volume")
    private Double volume;
}

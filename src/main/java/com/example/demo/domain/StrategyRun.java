package com.example.demo.domain;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class StrategyRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "opentime")
    private long openTime;

    @Column(name = "closetime")
    private long closeTime;

    @Column(name = "change_opt")
    private double changeOptimistic;

    @Column(name = "change_pes")
    private double changePessimistic;

    @Column(name = "split")
    private int splitTimes;

    @Column(name = "percentage_trigger")
    private double percentageTrigger;

    @Column(name = "profit_trigger")
    private double profitTrigger;

    @Column(name = "stop_loss_trigger")
    private double stopLossTrigger;

    @Column(name = "deviance")
    private double deviance;

}

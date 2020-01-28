package com.example.demo.domain;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity(name = "nano")
public class Nano extends Candlestick{

}

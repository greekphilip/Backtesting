package com.example.demo;

import com.example.demo.domain.candlestick.CustomCandlestick;

import java.util.ArrayList;
import java.util.List;

public class Values {

    public static final int ONE_DAY = 1440;
    public static final int TWO_DAYS = 2880;
    public static final boolean ALL_COINS = false;

    public static List<CustomCandlestick> coins = new ArrayList<>();

    public static final String[] coinsToAdd = {
            "Ada",
            "Etc",
            "Icx",
            "Nano"
    };

}

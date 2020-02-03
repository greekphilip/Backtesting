package com.example.demo.util;

import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class DateConverter {

    /**
     *
     * @param dateInString "02-12-2019 13:00:00"
     * @return
     */
    public long getLong(String dateInString){

        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(dateInString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

}

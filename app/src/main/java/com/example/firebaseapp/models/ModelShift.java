package com.example.firebaseapp.models;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;


public class ModelShift {
    private Date start;
    private Date ends;
    private long timeDifference;
    private static long totalTime = 0;

    private long getTimeZoneDifference(){
        Calendar mCalendar = new GregorianCalendar();
        TimeZone mTimeZone = mCalendar.getTimeZone();
        int mGMTOffset = mTimeZone.getRawOffset();
        return TimeUnit.HOURS.convert(mGMTOffset, TimeUnit.MILLISECONDS);
    }

    //empty constructor
    public ModelShift() {
    }

    public ModelShift(String start, String ends) {
        //create start and end dates
        this.start = new Date(Long.parseLong(start));
        this.ends = new Date(Long.parseLong(ends));
        //get the difference in long
        this.timeDifference = this.ends.getTime() - this.start.getTime();
        //add to static property total
        ModelShift.totalTime = ModelShift.totalTime + this.timeDifference;
    }

    public Date getStart() {
        return start;
    }

    public Date getEnds() {
        return ends;
    }

    public String getTotal(){
        //time differences ?
        Date total = new Date (this.timeDifference);
        long hours = total.getHours() - getTimeZoneDifference();
        long minutes = total.getMinutes() % 60;
        String hoursStr = hours < 10 ? "0"+hours : hours+"";
        String minStr = minutes < 10 ? "0"+minutes : minutes+"";
        return hoursStr + ":" +minStr;
    }

    public static long getTotalHours(){
        //time differences ?
        return new Date(ModelShift.totalTime).getHours()-2;
    }

    public static long getTotalMinutes(){
        return new Date(ModelShift.totalTime).getMinutes() % 60;
    }

    public static long getTotalSeconds(){
        return new Date(ModelShift.totalTime).getSeconds() % 60;
    }

    public static void resetTotal(){
        ModelShift.totalTime = 0;
    }
}

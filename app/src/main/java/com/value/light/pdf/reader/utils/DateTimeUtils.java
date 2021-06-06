package com.value.light.pdf.reader.utils;

import android.content.Context;
import android.text.format.DateFormat;

import java.util.Date;

public class DateTimeUtils {
    private static final String DATE_TIME_FORMAT = "dd/MM/yyyy • HH:mm:ss";
    private static final String DATE_TIME_NAMING_FORMAT = "ddMMyyyy_HHmmss";
    private static final String DATE_NAMING_FORMAT = "ddMMyyyy";
    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final long SECOND = 1000;
    private static final long MINUTE = 60*SECOND;
    private static final long HOUR = 60*MINUTE;
    private static final long DAY = 24*HOUR;
    private static final long MONTH = 30*DAY;
    private static final long YEAR = 365*DAY;

    public static Date getCurrentDateTime() {
        return new Date(System.currentTimeMillis());
    }

    public static long getCurrentDateTimeUnix() {
        return System.currentTimeMillis() / 1000L;
    }

    public static String fromDateToDateTimeString(Date date) {
        return DateFormat.format(DATE_TIME_FORMAT, date).toString();
    }

    public static String fromDateToDateString(Date date) {
        return DateFormat.format(DATE_FORMAT, date).toString();
    }

    public static String currentTimeToNaming() {
        return DateFormat.format(DATE_TIME_NAMING_FORMAT, new Date()).toString();
    }

    public static String currentDateToNaming() {
        return DateFormat.format(DATE_NAMING_FORMAT, new Date()).toString();
    }

    public static String fromTimeUnixToDateTimeString(long timeUnix) {
        if (timeUnix == 0)  return "";
        Date date = new Date(timeUnix * 1000L);
        return DateFormat.format(DATE_TIME_FORMAT, date).toString();
    }

    public static String fromTimeUnixToDateString(long timeUnix) {
        if (timeUnix == 0)  return "";
        Date date = new Date(timeUnix * 1000L);
        return DateFormat.format(DATE_FORMAT, date).toString();
    }

    public static Date fromMillisToDate(long timeUnix) {
        if (timeUnix == 0)  return new Date();
        return new Date(timeUnix * 1000L);
    }
}

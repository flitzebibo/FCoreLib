package me.FurH.Core.time;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import me.FurH.Core.exceptions.CoreException;
import me.FurH.Core.number.NumberUtils;

/**
 *
 * @author FurmigaHumana All Rights Reserved unless otherwise explicitly stated.
 */
public class TimeUtils {

    /**
     * Get a simple formated time for the given time in milliseconds
     * 
     * @param time the time in milliseconds
     * @return the formated time
     */
    public static String getSimpleFormatedTime(long time) {
        return new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss").format(time);
    }

    /**
     * Get a normal formated time for the given time in milliseconds
     * 
     * @param time the time in milliseconds
     * @return the formated time
     */
    public static String getFormatedTime(long time) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(time);
    }

    /**
     * Get a normal formated time with milliseconds for the given time in milliseconds
     * 
     * @param time the time in milliseconds
     * @return the formated time
     */
    public static String getFormatedTimeWithMillis(long time) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS").format(time);
    }

    /**
     * Get a simple formated time with milliseconds for the given time in milliseconds
     * 
     * @param time the time in milliseconds
     * @return the formated time
     */
    public static String getSimpleFormatedTimeWithMillis(long time) {
        return new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss-SSS").format(time);
    }

    /**
     * Adds an amount of time to the current time in millis using the given arguments
     * 
     * The argument must be a number, which is the amount of data to increment followed by one of this characters:
     * 
     * w, to add X amount of weeks
     * t, to add X amount of ticks
     * M, to add X amount of months
     * m, to add X amount of minutes
     * h, to add X amount of hours
     * d, to add X amount of days
     * none or just a number to add as milliseconds
     * 
     * Eg: to add 10 minutes to the current time:
     *      getTimeInMillis(theTimezone, 10m);
     * 
     *     to add 1 month to the current time:
     *      getTimeInMillis(theTimezone, 1M);
     * 
     * @param timezone the timezone used to retrieve the current time
     * @param time the argument
     * @return the time in milliseconds
     * @throws CoreException  
     */
    public static long getTimeInMillis(String timezone, String time) throws CoreException {
        Calendar calendar = new GregorianCalendar();

        calendar.setTimeZone(TimeZone.getTimeZone(timezone));
        int i = NumberUtils.toInteger(time);

        if (time.contains("w")) {
            calendar.add(Calendar.WEEK_OF_YEAR, i);
        } else if (time.contains("t")) {
            calendar.add(Calendar.SECOND, (i * 20));
        } else if (time.contains("M")) {
            calendar.add(Calendar.MONTH, i);
        } else if (time.contains("m")) {
            calendar.add(Calendar.MINUTE, i);
        } else if (time.contains("h")) {
            calendar.add(Calendar.HOUR, i);
        } else if (time.contains("d")) {
            calendar.add(Calendar.DAY_OF_YEAR, i);
        } else {
            calendar.add(Calendar.MILLISECOND, i);
        }

        return calendar.getTimeInMillis();
    }
    
    /**
     * Get the current time in millis given the timezone
     *
     * @param timezone the timezone to be used
     * @return the time in millis
     */
    public static long getCurrentTime(String timezone) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone(timezone));
        return calendar.getTimeInMillis();
    }
}

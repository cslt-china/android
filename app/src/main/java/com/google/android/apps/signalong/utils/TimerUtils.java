package com.google.android.apps.signalong.utils;

import java.sql.Date;

public class TimerUtils {

  public static void enoughSleep(int timeToSleep) {
    long start;
    while(timeToSleep > 0) {
      start = System.currentTimeMillis();
      try{
        Thread.sleep(timeToSleep);
        break;
      } catch(InterruptedException e){
        timeToSleep -= System.currentTimeMillis() - start;
      }
    }
  }

  public static boolean verifyTime(long time) {
    return System.currentTimeMillis() / 1000 < time;
  }

  /**
   * Convert the server returned MySQL timestamp int to a readable date time string.
   * @param time The number of seconds that have elapsed since midnight, January 1, 1970.
    */
  public static String convertTimestamp(int time) {
    Date date = new Date(1000L * time);
    return date.toString();
  }
}

package com.google.android.apps.signalong.utils;

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

}

package com.example.snake;
import android.view.*;
import android.graphics.Canvas;
import android.content.Context;
import android.util.*;

public class MainThread extends Thread {
  boolean isRunning; 
  GamePanel panel;

  public MainThread(GamePanel p) {
    panel = p;
    isRunning = true;
  }

  public void quit() {
    isRunning = false;
  }

  @Override
  public void run() {
    super.run();
    SurfaceHolder holder = panel.getHolder();
    while (isRunning) {
      Canvas canvas = holder.lockCanvas();
      int timeToSleep = 100;
      if (null != canvas) {
        timeToSleep = panel.refresh(canvas);
        holder.unlockCanvasAndPost(canvas);
      }
      try {
        sleep(timeToSleep);
      }
      catch (InterruptedException e) {
        Log.v("Exception: ", e.getMessage());
      }
    }
  }
}

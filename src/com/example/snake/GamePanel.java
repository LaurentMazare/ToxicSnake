package com.example.snake;
import android.view.*;
import android.graphics.*;
import android.content.Context;
import android.util.*;
import java.util.*;

class Point {
  int x;
  int y;
  Point(int x_, int y_) {x = x_; y = y_;}
}

class Snake {
  LinkedList<Point> points;
  int dx;
  int dy;
  Paint paint;

  int width;
  int height;
  boolean hasCrashed = false;

  Snake(int width_, int height_) {
    init();
    width = width_;
    height = height_;
  }

  void init() {
    points = new LinkedList();
    for (int i = 0; i < 7; i++) points.add(new Point(10+i, 10));
    dx = 1;
    dy = 0;
    hasCrashed = false;
    paint = new Paint();
    paint.setColor(Color.GREEN);
    paint.setAntiAlias(true);
  }

  void next() {
    if (!hasCrashed) {
      Point pLast = points.getLast();
      int new_x = pLast.x + dx;
      int new_y = pLast.y + dy;
      hasCrashed = new_x < 0 || new_x >= width || new_y < 0 || new_y >= height;
      for (Point p: points)
        if (p.x == new_x && p.y == new_y) hasCrashed = true;
      if (!hasCrashed) {
        points.add(new Point(new_x, new_y));
        points.removeFirst();
      }
    }
  }

  void draw(Canvas canvas, float sq_size, float x0, float y0) {
    if (hasCrashed)
      paint.setColor(Color.RED);
    else
      paint.setColor(Color.GREEN);
    for (Point p: points) {
      float x = x0 + sq_size * p.x;
      float y = y0 + sq_size * p.y;
      float x_p = x + sq_size-1;
      float y_p = y + sq_size-1;
      canvas.drawRect(x, y, x_p, y_p, paint);
    }
  }
}

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
  MainThread mainThread;
  Snake snake;
  float sq_size, x0, y0;
  int width = 30;
  int height = 30;
  Paint bgPaint;

  public GamePanel(Context context) {
    super(context);
    getHolder().addCallback(this); // Register self as call back
  }

  public GamePanel(Context context, AttributeSet attrSet) {
    super(context, attrSet);
    getHolder().addCallback(this); // Register self as call back
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    int dimX = ((int)getWidth() - 10) / (width + 1);
    int dimY = ((int)getHeight() - 10) / (height + 1);
    sq_size = Math.min(dimX, dimY);
    x0 = ((float)getWidth() - width * sq_size) / 2;
    y0 = ((float)getHeight() - height * sq_size) / 2;
    bgPaint = new Paint();
    bgPaint.setColor(Color.DKGRAY);
    snake = new Snake(width, height);
    mainThread = new MainThread(this);
    mainThread.start();
  }
  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
  }
  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    mainThread.quit();
    while (true) {
      try {
        mainThread.join();
        break;
      }
      catch(Exception e) {
        Log.v("Exception: ", e.getMessage());
      }
    }
  }

  void refresh(Canvas canvas) {
    canvas.drawColor(Color.BLACK);
    canvas.drawRect(x0, y0, x0 + width*sq_size, y0 + height*sq_size, bgPaint);
    snake.draw(canvas, sq_size, x0, y0);
    snake.next();
  }

  public void restart() {
    if (snake.hasCrashed) snake.init();
  }

  public void onFling(float dx, float dy) {
    if (Math.abs(dx) > Math.abs(dy)) {
      if (dx > 3.0 && snake.dy != 0) {snake.dx = 1; snake.dy = 0;}
      else if (dx < -3.0 && snake.dy != 0) {snake.dx = -1; snake.dy = 0;}
    }
    else {
      if (dy > 3.0 && snake.dx != 0) {snake.dx = 0; snake.dy = 1;}
      else if (dy < -3.0 && snake.dx != 0) {snake.dx = 0; snake.dy = -1;}
    }
  }
}

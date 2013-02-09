package com.example.snake;
import android.view.*;
import android.graphics.*;
import android.content.*;
import android.util.*;
import java.util.*;

enum Direction {LEFT, RIGHT, UP, DOWN}

class Point {
  int x;
  int y;
  Point(int x_, int y_) {x = x_; y = y_;}
}

class Snake {
  LinkedList<Point> points;
  Direction dir;
  Direction prev_dir;
  boolean removeLast;
  Paint paint;

  int width;
  int height;
  boolean hasCrashed = false;

  Snake(int width_, int height_) {
    init();
    width = width_;
    height = height_;
    removeLast = true;
  }

  void init() {
    points = new LinkedList();
    for (int i = 0; i < 7; i++) points.add(new Point(1+i, 6));
    hasCrashed = false;
    paint = new Paint();
    paint.setColor(Color.GREEN);
    paint.setAntiAlias(true);
    dir = Direction.RIGHT;
    dir = Direction.RIGHT;
  }

  boolean contains(Point p_) {
    boolean res = false;
    for (Point p: points)
      if (p.x == p_.x && p.y == p_.y) res = true;
    return res;
  }

  boolean next() {
    boolean justCrashed = false;
    if (!hasCrashed) {
      Point pLast = points.getLast();
      int new_x = pLast.x;
      int new_y = pLast.y;
      if (dir == Direction.LEFT) new_x--;
      else if (dir == Direction.RIGHT) new_x++;
      else if (dir == Direction.DOWN) new_y--;
      else if (dir == Direction.UP) new_y++;
      hasCrashed = new_x < 0 || new_x >= width || new_y < 0 || new_y >= height;
      for (Point p: points)
        if (p.x == new_x && p.y == new_y) hasCrashed = true;
      if (!hasCrashed) {
        points.add(new Point(new_x, new_y));
        if (removeLast)
          points.removeFirst();
      }
      else
        justCrashed = true;
    }
    prev_dir = dir;
    removeLast = true;
    return justCrashed;
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

class Elements {
  Point diamond;
  Paint paint;
  int width;
  int height;
  Random rng;

  Elements(int w, int h) {
    width = w;
    height = h;
    paint = new Paint();
    paint.setAntiAlias(true);
    diamond = new Point(15, 15);
    rng = new Random();
  }

  void replaceDiamond(Snake s) {
    while (true) {
      diamond.x = rng.nextInt(width);
      diamond.y = rng.nextInt(height);
      if (!s.contains(diamond)) break;
    }
  }

  void draw(Canvas canvas, float sq_size, float x0, float y0) {
    float radius = sq_size / 2;
    paint.setColor(Color.CYAN);
    float x = x0 + sq_size * diamond.x + radius;
    float y = y0 + sq_size * diamond.y + radius;
    canvas.drawCircle(x, y, radius-1, paint);
  }
}

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
  MainThread mainThread;
  Snake snake;
  Elements elts;
  int score = 0;
  int hScore = 0;
  boolean isPaused = true;
  float sq_size, x0, y0;
  private static final int width = 30;
  private static final int height = 40;
  Paint bgPaint;
  SharedPreferences prefs;

  public GamePanel(Context context) {
    super(context);
    init(context);
  }

  private void init(Context context) {
    getHolder().addCallback(this); // Register self as call back
    prefs = context.getSharedPreferences("ToxicSnakePrefs", 0);
    hScore = prefs.getInt("HighScore", 0);
  }

  public GamePanel(Context context, AttributeSet attrSet) {
    super(context, attrSet);
    init(context);
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    int dimX = ((int)getWidth() - 15) / (width + 1);
    int dimY = ((int)getHeight() - 30) / (height + 1);
    sq_size = Math.min(dimX, dimY);
    x0 = ((float)getWidth() - width * sq_size) / 2;
    y0 = ((float)getHeight() - height * sq_size) / 2;
    bgPaint = new Paint();
    bgPaint.setColor(Color.DKGRAY);
    bgPaint.setTextSize(20);
    bgPaint.setAntiAlias(true);
    bgPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD_ITALIC));
    snake = new Snake(width, height);
    elts = new Elements(width, height);
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

  private void paintGame(Canvas canvas) {
    // Fixed layout for now...
    canvas.drawColor(Color.BLACK);
    bgPaint.setColor(Color.DKGRAY);
    canvas.drawRect(x0-6, y0-26, x0 + width*sq_size + 6, y0 + height*sq_size + 6, bgPaint);
    bgPaint.setColor(Color.BLACK);
    canvas.drawRect(x0, y0, x0 + width*sq_size, y0 + height*sq_size, bgPaint);
    elts.draw(canvas, sq_size, x0, y0);
    snake.draw(canvas, sq_size, x0, y0);
    bgPaint.setColor(Color.WHITE);
    String scoreStr = String.format("%03d/%03d", score, hScore);
    canvas.drawText(scoreStr, x0 + width*sq_size - 90, y0 - 5, bgPaint);
  }

  public void refresh(Canvas canvas) {
    paintGame(canvas);
    next();
  }

  private void next() {
    if (isPaused) return;
    boolean justCrashed = snake.next();
    if (snake.contains(elts.diamond)) {
      score++;
      snake.removeLast = false;
      elts.replaceDiamond(snake);
    }
    if (justCrashed) {
      if (score > hScore) {
        hScore = score;
        SharedPreferences.Editor edit = prefs.edit();
        edit.putInt("HighScore", score);
        edit.commit();
      }
      score = 0;
    }
  }

  public void onTap(float x, float y) {
    if (isPaused)
      isPaused = false;
    else if (snake.hasCrashed) {
      snake.init();
      isPaused = false;
    }
    else if (y < y0)
      isPaused = true;
  }

  public void onMove(Direction dir) {
    if (isPaused || snake.hasCrashed) return;
    boolean isVertical = snake.dir == Direction.DOWN || snake.dir == Direction.UP;
    if ((dir == Direction.RIGHT && isVertical) ||
        (dir == Direction.LEFT && isVertical) ||
        (dir == Direction.UP && !isVertical) ||
        (dir == Direction.DOWN && !isVertical)) snake.dir = dir;
  }
}

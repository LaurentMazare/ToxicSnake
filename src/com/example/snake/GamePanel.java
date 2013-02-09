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
  Paint paint;

  int width;
  int height;
  boolean hasCrashed;

  Snake(int width_, int height_) {
    init();
    hasCrashed = true;
    width = width_;
    height = height_;
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

  boolean next(LinkedList<Point> wPoints) {
    boolean justCrashed = false;
    if (!hasCrashed) {
      Point pLast = points.getLast();
      int newX = pLast.x;
      int newY = pLast.y;
      if (dir == Direction.LEFT) newX--;
      else if (dir == Direction.RIGHT) newX++;
      else if (dir == Direction.DOWN) newY--;
      else if (dir == Direction.UP) newY++;
      hasCrashed = newX < 0 || newX >= width || newY < 0 || newY >= height;
      for (Point p: wPoints)
        if (p.x == newX && p.y == newY) hasCrashed = true;
      for (Point p: points)
        if (p.x == newX && p.y == newY) hasCrashed = true;
      if (!hasCrashed) {
        points.add(new Point(newX, newY));
      }
      else
        justCrashed = true;
    }
    prev_dir = dir;
    return justCrashed;
  }

  Point removeFirst() {
    Point p = points.getFirst();
    points.removeFirst();
    return p;
  }

  void draw(Canvas canvas, float sqSize, float x0, float y0) {
    if (hasCrashed)
      paint.setColor(Color.RED);
    else
      paint.setColor(Color.GREEN);
    for (Point p: points) {
      float x = x0 + sqSize * p.x;
      float y = y0 + sqSize * p.y;
      float x_p = x + sqSize-1;
      float y_p = y + sqSize-1;
      canvas.drawRect(x, y, x_p, y_p, paint);
    }
  }
}

class Elements {
  Point diamond;
  LinkedList<Point> wPoints;
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
    wPoints = new LinkedList();
  }

  void replaceDiamond(Snake s) {
    while (true) {
      diamond.x = 1 + rng.nextInt(width - 2);
      diamond.y = 1 + rng.nextInt(height - 2);
      if (!s.contains(diamond)) break;
    }
  }

  void draw(Canvas canvas, float sqSize, float x0, float y0) {
    float radius = sqSize / 2;
    paint.setColor(Color.RED);
    for (Point p: wPoints) {
      float x = x0 + sqSize * p.x + radius;
      float y = y0 + sqSize * p.y + radius;
      canvas.drawCircle(x, y, radius-1, paint);
    }
    paint.setColor(Color.CYAN);
    float x = x0 + sqSize * diamond.x + radius;
    float y = y0 + sqSize * diamond.y + radius;
    canvas.drawCircle(x, y, radius-1, paint);
  }
}

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
  MainThread mainThread;
  Snake snake;
  Elements elts;
  int score = 0;
  int hScore = 0;
  boolean isPaused = false;
  float sqSize, x0, y0;
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
    sqSize = Math.min(dimX, dimY);
    x0 = ((float)getWidth() - width * sqSize) / 2;
    y0 = ((float)getHeight() - height * sqSize) / 2;
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
    canvas.drawRect(x0-6, y0-26, x0 + width*sqSize + 6, y0 + height*sqSize + 6, bgPaint);
    bgPaint.setColor(Color.BLACK);
    canvas.drawRect(x0, y0, x0 + width*sqSize, y0 + height*sqSize, bgPaint);
    elts.draw(canvas, sqSize, x0, y0);
    snake.draw(canvas, sqSize, x0, y0);
    bgPaint.setColor(Color.WHITE);
    String scoreStr = "";
    if (snake.hasCrashed) {
      scoreStr = String.format("    %03d", hScore);
      String statusStr = String.format("%03d - HIGH SCORE", score);
      canvas.drawText(statusStr, x0 + 5, y0 - 5, bgPaint);
    }
    else {
      if (isPaused) canvas.drawText("PAUSED", x0 + 5, y0 - 5, bgPaint);
      scoreStr = String.format("%03d/%03d", score, hScore);
    }
    canvas.drawText(scoreStr, x0 + width*sqSize - 90, y0 - 5, bgPaint);
  }

  public void refresh(Canvas canvas) {
    paintGame(canvas);
    next();
  }

  private void next() {
    if (isPaused || snake.hasCrashed) return;
    boolean justCrashed = snake.next(elts.wPoints);
    if (snake.contains(elts.diamond)) {
      score++;
      elts.replaceDiamond(snake);
      if (score % 3 == 0) {
        Point p = snake.removeFirst();
        elts.wPoints.add(p);
      }
    }
    else
      snake.removeFirst();
    if (justCrashed && score > hScore) {
      hScore = score;
      SharedPreferences.Editor edit = prefs.edit();
      edit.putInt("HighScore", score);
      edit.commit();
    }
  }

  public void onTap(float x, float y) {
    if (isPaused)
      isPaused = false;
    else if (snake.hasCrashed) {
      snake.init();
      isPaused = false;
      score = 0;
      elts.wPoints.clear();
      elts.replaceDiamond(snake);
    }
    else if (y < y0 - 10)
      isPaused = true;
    else {
      boolean isVertical = snake.dir == Direction.DOWN || snake.dir == Direction.UP;
      Point p = snake.points.getLast();
      float currentX = x0 + sqSize * p.x;
      float currentY = y0 + sqSize * p.y;
      if (isVertical && x < currentX) snake.dir = Direction.LEFT;
      else if (isVertical && currentX + sqSize < x) snake.dir = Direction.RIGHT;
      else if (!isVertical && currentY + sqSize < y) snake.dir = Direction.UP;
      else if (!isVertical && y < currentY) snake.dir = Direction.DOWN;
    }
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

package com.trialanderrorapps.snake;
import android.app.Activity;
import android.view.*;
import android.graphics.*;
import android.content.*;
import android.util.*;
import java.util.*;

import java.io.Serializable;

enum Direction {LEFT, RIGHT, UP, DOWN}
enum Mode {PAUSED, PLAYING, CRASHED, DEMO}

class Point implements Serializable {
  int x;
  int y;
  Point(int x_, int y_) {x = x_; y = y_;}
}

class Snake implements Serializable {
  LinkedList<Point> points;
  Direction dir;
  Direction prevDir;

  int width;
  int height;

  Snake(int width_, int height_) {
    width = width_;
    height = height_;
    points = new LinkedList();
    init();
  }

  void init() {
    points.clear();
    for (int i = 0; i < 7; i++) points.add(new Point(5, i));
    dir = Direction.UP;
    prevDir = Direction.UP;
  }

  boolean contains(Point p_) {
    boolean res = false;
    for (Point p: points)
      if (p.x == p_.x && p.y == p_.y) res = true;
    return res;
  }

  boolean next(LinkedList<Point> wPoints) {
    Point pLast = points.getLast();
    int newX = pLast.x;
    int newY = pLast.y;
    if (dir == Direction.LEFT) newX--;
    else if (dir == Direction.RIGHT) newX++;
    else if (dir == Direction.DOWN) newY--;
    else if (dir == Direction.UP) newY++;
    boolean hasCrashed = newX < 0 || newX >= width || newY < 0 || newY >= height;
    for (Point p: wPoints)
      if (p.x == newX && p.y == newY) hasCrashed = true;
    for (Point p: points)
      if (p.x == newX && p.y == newY) hasCrashed = true;
    if (!hasCrashed) points.add(new Point(newX, newY));
    prevDir = dir;
    return hasCrashed;
  }

  Point removeFirst() {
    Point p = points.getFirst();
    points.removeFirst();
    return p;
  }

  void draw(Canvas canvas, float sqSize, float x0, float y0, Paint paint) {
    for (Point p: points) {
      float x = x0 + sqSize * p.x;
      float y = y0 + sqSize * p.y;
      float x_p = x + sqSize-1;
      float y_p = y + sqSize-1;
      canvas.drawRect(x, y, x_p, y_p, paint);
    }
  }
}

class Elements implements Serializable {
  Point diamond;
  LinkedList<Point> wPoints;
  int width;
  int height;
  Random rng;
  int gColor = 0;
  boolean gColIncrease = true;

  Elements(int w, int h) {
    width = w;
    height = h;
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
    Paint paint = new Paint();
    paint.setAntiAlias(true);
    paint.setColor(Color.rgb(255, gColor, 0));
    if (gColIncrease) {
      gColor += 15;
      if (gColor > 200) {gColor = 200; gColIncrease = false;}
    }
    else {
      gColor -= 15;
      if (gColor < 0) {gColor = 0; gColIncrease = true;}
    }
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

class Demo {
  static Direction getDirection(Snake snake, Elements elts) {
    Point pLast = snake.points.getLast();
    int x = pLast.x;
    int y = pLast.y;
    Direction dir = snake.dir;
    if ((snake.dir == Direction.DOWN && y < 2) || (snake.dir == Direction.UP && y > snake.height - 3))
      dir = (x < snake.width / 2) ? Direction.RIGHT: Direction.LEFT;
    else if ((snake.dir == Direction.LEFT && x < 2) || (snake.dir == Direction.RIGHT && x > snake.width - 3))
      dir = (y < snake.height / 2) ? Direction.UP: Direction.DOWN;
    else if (x == elts.diamond.x && (snake.dir == Direction.LEFT || snake.dir == Direction.RIGHT))
      dir = (y < elts.diamond.y) ? Direction.UP: Direction.DOWN;
    else if (y == elts.diamond.y && (snake.dir == Direction.UP || snake.dir == Direction.DOWN))
      dir = (x < elts.diamond.x) ? Direction.RIGHT: Direction.LEFT;
    return dir;
  }
}

class GameData implements Serializable {
  Snake snake = null;
  Elements elts = null;
  Mode mode = Mode.PLAYING;
  int score = 0;
}

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
  Activity activity;
  Paint bgPaint = new Paint();
  MainThread mainThread = null;
  int lScore = 0;
  int hScore = 0;
  float sqSize, x0, y0;
  private static final int width = 22;
  private static final int height = 30;
  long restartTime = System.currentTimeMillis();
  GameData gd = new GameData();

  public GamePanel(Context context) {
    super(context);
    activity = (Activity)context;
    initGame();
  }

  private void initGame() {
    getHolder().addCallback(this); // Register self as call back
    SharedPreferences prefs = activity.getSharedPreferences("ToxicSnakePrefs", 0);
    hScore = prefs.getInt("HighScore", 0);
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    bgPaint.setColor(Color.DKGRAY);
    bgPaint.setTextSize(20);
    bgPaint.setAntiAlias(true);
    bgPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD_ITALIC));
    int dimX = ((int)getWidth() - 15) / (width + 1);
    int dimY = ((int)getHeight() - 30) / (height + 1);
    sqSize = Math.min(dimX, dimY);
    x0 = ((float)getWidth() - width * sqSize) / 2;
    y0 = ((float)getHeight() - height * sqSize) / 2;
    if (gd.snake == null) { // This means that the application was already initialized
      gd.snake = new Snake(width, height);
      gd.elts = new Elements(width, height);
    }
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
      }
    }
  }

  private void paintGame(Canvas canvas) {
    // Fixed layout for now...
    float yMax = y0 + height * sqSize;
    float xMax = x0 + width * sqSize;
    canvas.drawColor(Color.BLACK);
    bgPaint.setColor(Color.DKGRAY);
    canvas.drawRect(x0-6, y0-26, xMax + 6, yMax + 6, bgPaint);
    bgPaint.setColor(Color.BLACK);
    canvas.drawRect(x0, y0, xMax, yMax, bgPaint);
    gd.elts.draw(canvas, sqSize, x0, y0);
    bgPaint.setColor(Color.GREEN);
    gd.snake.draw(canvas, sqSize, x0, y0, bgPaint);
    if (gd.mode == Mode.CRASHED || gd.mode == Mode.DEMO) drawMenu(canvas);
    else {
      bgPaint.setColor(Color.WHITE);
      String statusStr = (gd.mode == Mode.PAUSED) ? "PAUSED": "PAUSE";
      canvas.drawText(statusStr, x0 + 5, y0 - 5, bgPaint);
      String scoreStr = String.format("%03d/%03d", gd.score, hScore);
      canvas.drawText(scoreStr, xMax - 90, y0 - 5, bgPaint);
    }
  }

  private void drawMenu(Canvas canvas) {
    float yMax = y0 + height * sqSize;
    float xMax = x0 + width * sqSize;
    bgPaint.setColor(Color.DKGRAY);
    bgPaint.setAlpha(200);
    canvas.drawRect(x0+20, y0+20, xMax-20, y0 + 150, bgPaint);
    bgPaint.setColor(Color.WHITE);
    canvas.drawText("GAME OVER!", x0+25, y0+50, bgPaint);
    if (lScore > 0 && gd.mode != Mode.DEMO)
      canvas.drawText(String.format("Last Score:   %03d", lScore), x0+25, y0+75, bgPaint);
    canvas.drawText(String.format("High Score:   %03d", hScore), x0+25, y0+100, bgPaint);
    bgPaint.setAlpha(255);
  }

  public int refresh(Canvas canvas) {
    paintGame(canvas);
    if (gd.mode == Mode.DEMO) gd.snake.dir = Demo.getDirection(gd.snake, gd.elts);
    next();
    if (gd.mode == Mode.CRASHED) 
      if (System.currentTimeMillis() > restartTime)
        startGame(Mode.DEMO);
    return Math.max(100, 150 - 2 * gd.score);
  }

  private void next() {
    if (gd.mode == Mode.CRASHED || gd.mode == Mode.PAUSED) return;
    boolean hasCrashed = gd.snake.next(gd.elts.wPoints);
    if (!hasCrashed) {
      if (gd.snake.contains(gd.elts.diamond)) {
        gd.score++;
        gd.elts.replaceDiamond(gd.snake);
        if (gd.score % 3 == 0) {
          Point p = gd.snake.removeFirst();
          gd.elts.wPoints.add(p);
        }
      }
      else gd.snake.removeFirst();
    }
    else {
      if (gd.mode == Mode.PLAYING) lScore = gd.score;
      if (gd.score > hScore && gd.mode == Mode.PLAYING) {
        hScore = gd.score;
        SharedPreferences prefs = activity.getSharedPreferences("ToxicSnakePrefs", 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putInt("HighScore", gd.score);
        edit.commit();
      }
      restartTime = System.currentTimeMillis() + 1000 * 20;
      gd.mode = Mode.CRASHED;
    }
  }

  private void startGame(Mode m) {
    gd.mode = m;
    gd.score = 0;
    gd.snake.init();
    gd.elts.wPoints.clear();
    gd.elts.replaceDiamond(gd.snake);
  }

  public void onTap(float x, float y) {
    if (gd.mode == Mode.PAUSED) gd.mode = Mode.PLAYING;
    else if (gd.mode == Mode.CRASHED || gd.mode == Mode.DEMO) {
      activity.setResult(Activity.RESULT_OK, new Intent());
      activity.finish();
    }
    else if (y < y0) gd.mode = Mode.PAUSED;
    else {
      Snake snake = gd.snake;
      boolean isVertical = snake.prevDir == Direction.DOWN || snake.prevDir == Direction.UP;
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
    if (gd.mode != Mode.PLAYING) return;
    Snake snake = gd.snake;
    boolean isVertical = snake.prevDir == Direction.DOWN || snake.prevDir == Direction.UP;
    if ((dir == Direction.RIGHT && isVertical) ||
        (dir == Direction.LEFT && isVertical) ||
        (dir == Direction.UP && !isVertical) ||
        (dir == Direction.DOWN && !isVertical)) snake.dir = dir;
  }

  public void pause() {
    if (gd.mode == Mode.PLAYING) gd.mode = Mode.PAUSED;
  }
}

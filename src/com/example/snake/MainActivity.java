package com.example.snake;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.view.GestureDetector.*;

public class MainActivity extends Activity implements OnGestureListener {
  private GestureDetector gestureDetector;
  GamePanel gamePanel;
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    gamePanel = new GamePanel(this);
    setContentView(gamePanel);
    gestureDetector = new GestureDetector(this, this);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
      return (gestureDetector.onTouchEvent(event));
  }

  @Override
  public boolean onSingleTapUp(MotionEvent ev) {
    gamePanel.onTap(ev.getX(), ev.getY());
    return true;
  }
  @Override
  public void onShowPress(MotionEvent ev) {
  }
  @Override
  public void onLongPress(MotionEvent ev) {
  }
  @Override
  public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
    if (Math.abs(dx) > 2 * Math.abs(dy)) {
      if (dx > 8.0) gamePanel.onMove(Direction.LEFT);
      else if (dx < -8.0) gamePanel.onMove(Direction.RIGHT);
    }
    else if (Math.abs(dy) > 2 * Math.abs(dx)) {
      if (dy > 8.0) gamePanel.onMove(Direction.DOWN);
      else if (dy < -8.0) gamePanel.onMove(Direction.UP);
    }
    return true;
  }
  @Override
  public boolean onDown(MotionEvent ev) {
    return true;
  }
  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    return true;
  }
}

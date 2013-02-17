package com.trialanderrorapps.snake;

import com.trialanderrorapps.snake.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MenuActivity extends Activity implements OnClickListener {
  GameData savedGameData = null;
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.menu);
    ((Button)findViewById(R.id.bStart)).setOnClickListener(this);
    ((Button)findViewById(R.id.bQuit)).setOnClickListener(this);
    ((Button)findViewById(R.id.bResume)).setOnClickListener(this);
    if (savedGameData == null)
      ((Button)findViewById(R.id.bResume)).setEnabled(false);
  }

  @Override
  public void onClick(View view) {
    Intent intent = new Intent(this, MainActivity.class);
    switch(view.getId()) {
      case R.id.bQuit:
        finish();
        break;
      case R.id.bStart:
        startActivityForResult(intent, 1);
        break;
      case R.id.bResume:
        if (savedGameData != null) intent.putExtra("GameData", savedGameData);
        startActivityForResult(intent, 1);
        break;
    }
  }
  @Override
  protected void onActivityResult (int requestCode, int resultCode, Intent data)  {
    if (resultCode == RESULT_OK && data != null) {
      if (data.hasExtra("GameData")) {
        savedGameData = (GameData)data.getSerializableExtra("GameData");
        ((Button)findViewById(R.id.bResume)).setEnabled(true);
      }
      else {
        savedGameData = null;
        ((Button)findViewById(R.id.bResume)).setEnabled(false);
      }
    }
  }
}

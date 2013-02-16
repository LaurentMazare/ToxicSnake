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
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        ((Button)findViewById(R.id.bStart)).setOnClickListener(this);
        ((Button)findViewById(R.id.bQuit)).setOnClickListener(this);
    }

	@Override
	public void onClick(View view) {
		switch(view.getId()) {
			case R.id.bQuit:
				finish();
				break;
			case R.id.bStart:
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
				break;
		}
	}
}

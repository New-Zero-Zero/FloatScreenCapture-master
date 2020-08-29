package com.branch.www.screencapture;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.SpanUtils;

import pl.droidsonroids.gif.GifImageView;

public class StartActivity extends FragmentActivity {



  public void starMainActivity(){
      Intent intent = new Intent(this,MainActivity.class);
      startActivity(intent);
      finish();
  }
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if(!TextUtils.isEmpty(SPUtils.getInstance().getString(MainActivity.FACTION_IS))){
      starMainActivity();
      return;
    }
    setContentView(R.layout.start_main);
    GifImageView Enlightened = (GifImageView) findViewById(R.id.Enlightened);
    GifImageView Resistance = (GifImageView) findViewById(R.id.Resistance);

    Enlightened.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        SPUtils.getInstance().put(MainActivity.FACTION_IS,MainActivity.Enlightened);
        starMainActivity();

      }
    });
    Resistance.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        SPUtils.getInstance().put(MainActivity.FACTION_IS,MainActivity.Resistance);
        starMainActivity();

      }
    });
  }


}

package com.branch.www.screencapture;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;

import java.util.List;

public class MainActivity extends FragmentActivity {


  public static final int REQUEST_MEDIA_PROJECTION = 18;
  private CheckBox saveimagelocal;
  private Button stareingress;

  public final static String  SAVE_IMAGE_LOCAL_IS="save_image_local";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    saveimagelocal = (CheckBox) findViewById(R.id.save_image_local);
    stareingress = (Button) findViewById(R.id.stare_ingress);
    saveimagelocal.setChecked(SPUtils.getInstance().getBoolean(SAVE_IMAGE_LOCAL_IS));
    saveimagelocal.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        final CheckBox checkBox = (CheckBox) v;
        if(checkBox.isChecked()){
          //检测读取权限
          PermissionUtils.permission(PermissionConstants.STORAGE)
                  .callback(new PermissionUtils.SimpleCallback() {
                    @Override
                    public void onGranted() {
                      SPUtils.getInstance().put(SAVE_IMAGE_LOCAL_IS,true);
                      checkBox.setChecked(true);
                    }

                    @Override
                    public void onDenied() {
                      SPUtils.getInstance().put(SAVE_IMAGE_LOCAL_IS,false);
                      checkBox.setChecked(false);
                    }
                  })
                  .request();
          return;
        }
        SPUtils.getInstance().put(SAVE_IMAGE_LOCAL_IS,false);
        checkBox.setChecked(false);
      }
    });

    stareingress.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String ingressPkg="com.nianticproject.ingress";
        if (AppUtils.isAppInstalled(ingressPkg)){
          AppUtils.launchApp(ingressPkg);
          moveTaskToBack(false);
        }else {
          ToastUtils.showLong("未安装ingress");
        }
      }
    });
    requestCapturePermission();


  }


  public void requestCapturePermission() {

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      //5.0 之后才允许使用屏幕截图

      return;
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (!Settings.canDrawOverlays(this)) {
        Toast.makeText(this, "当前无权限，请授权", Toast.LENGTH_SHORT);
        startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 1);
      } else {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(),
                REQUEST_MEDIA_PROJECTION);

//        startService(new Intent(MainActivity.this, FloatingImageDisplayService.class));
      }
    }

  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    switch (requestCode) {
      case REQUEST_MEDIA_PROJECTION:

        if (resultCode == RESULT_OK && data != null) {
          FloatWindowsService.setResultData(data);
          startService(new Intent(getApplicationContext(), FloatWindowsService.class));
        }
        break;
    }

  }

}

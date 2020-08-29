package com.branch.www.screencapture;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;

import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends FragmentActivity {


  public static final int REQUEST_MEDIA_PROJECTION = 18;
  private CheckBox saveimagelocal;
  private Button stareingress;

  public final static String  SAVE_IMAGE_LOCAL_IS="save_image_local";
  public final static String  FACTION_IS="faction";
  public final static String  Enlightened="Enlightened";//启蒙者 (绿)
  public final static String  Resistance="Resistance";//反抗者 （蓝）
  String ingressPkg="com.nianticproject.ingress";//ingress 包名

  private GifImageView gifimage;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    gifimage = (GifImageView) findViewById(R.id.gif_image);
    saveimagelocal = (CheckBox) findViewById(R.id.save_image_local);
    stareingress = (Button) findViewById(R.id.stare_ingress);
    stareingress.setEnabled(true);
    saveimagelocal.setEnabled(true);
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
        if (AppUtils.isAppInstalled(ingressPkg)){
          AppUtils.launchApp(ingressPkg);
          moveTaskToBack(false);
        }else {
          ToastUtils.showLong("未安装ingress");
        }
      }
    });
    //阵营的选择
    String faction= SPUtils.getInstance().getString(FACTION_IS,"");

    if(faction.equals(Enlightened)){
      stareingress.setBackgroundColor(getResources().getColor(R.color.Enlightened));
      gifimage.setImageResource(R.drawable.e);
    }else if(faction.equals(Resistance)){
      stareingress.setBackgroundColor(getResources().getColor(R.color.Resistance));
      gifimage.setImageResource(R.drawable.r);
    }
    saveimagelocal.setChecked(SPUtils.getInstance().getBoolean(SAVE_IMAGE_LOCAL_IS));

    requestCapturePermission();

  }


  public void requestCapturePermission() {

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      //5.0 之后才允许使用屏幕截图
      ToastUtils.showLong("当前Android版本不支持截图功能！！！");
      return;
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (!Settings.canDrawOverlays(this)) {
        Toast.makeText(this, "请授权应用悬浮桌面权限", Toast.LENGTH_SHORT);
        startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 1);
      }else{
            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                    getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            startActivityForResult(
                    mediaProjectionManager.createScreenCaptureIntent(),
                    REQUEST_MEDIA_PROJECTION);
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

  @Override
  protected void onResume() {
    super.onResume();
  }
}

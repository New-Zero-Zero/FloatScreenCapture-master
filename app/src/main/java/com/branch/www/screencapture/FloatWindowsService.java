package com.branch.www.screencapture;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.SPUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by branch on 2016-5-25.
 *
 * 启动悬浮窗界面
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class FloatWindowsService extends Service {


  private View displayView;
  private ImageView imageView;
  private ImageView imageView2;
  private ImageView imageView3;
  private ImageView imageView4;
  private ImageView imageView5;

  public static Intent newIntent(Context context, Intent mResultData) {

    Intent intent = new Intent(context, FloatWindowsService.class);

    if (mResultData != null) {
      intent.putExtras(mResultData);
    }
    return intent;
  }

  private MediaProjection mMediaProjection;
  private VirtualDisplay mVirtualDisplay;
  private WindowManager.LayoutParams layoutParams;

  private static Intent mResultData = null;


  private ImageReader mImageReader;
  private WindowManager mWindowManager;
  private GestureDetector mGestureDetector;


  private int mScreenWidth;
  private int mScreenHeight;
  private int mScreenDensity;
  private int index;


  @Override
  public void onCreate() {
    super.onCreate();

    createFloatView();

    createImageReader();
  }

  public static Intent getResultData() {
    return mResultData;
  }

  public static void setResultData(Intent mResultData) {
    FloatWindowsService.mResultData = mResultData;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  private void createFloatView() {
    mGestureDetector = new GestureDetector(getApplicationContext(), new FloatGestrueTouchListener());
    mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

    DisplayMetrics metrics = new DisplayMetrics();
    mWindowManager.getDefaultDisplay().getMetrics(metrics);
    mScreenDensity = metrics.densityDpi;
    mScreenWidth = metrics.widthPixels;
    mScreenHeight = metrics.heightPixels;

    layoutParams = new WindowManager.LayoutParams();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
    } else {
      layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
    }
    layoutParams.format = PixelFormat.RGBA_8888;
    layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
    layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
    layoutParams.height = getApplicationContext().getResources().getDimensionPixelOffset(R.dimen.dp_80);
    layoutParams.x = 50;
    layoutParams.y = 60;


//    mFloatView = new ImageView(getApplicationContext());
//    mFloatView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_imagetool_crop));

    LayoutInflater layoutInflater = LayoutInflater.from(this);
    displayView = layoutInflater.inflate(R.layout.image_display, null);
    displayView.setOnTouchListener(new FloatingOnTouchListener());
    imageView = displayView.findViewById(R.id.image_display_imageview);
    imageView2 = displayView.findViewById(R.id.image_display_imageview_2);
    imageView3 = displayView.findViewById(R.id.image_display_imageview_3);
    imageView4 = displayView.findViewById(R.id.image_display_imageview_4);
    imageView5 = displayView.findViewById(R.id.image_display_imageview_5);

    ImageView tv_click = displayView.findViewById(R.id.tv_click);
    tv_click.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        imageView.setImageBitmap(null);
        imageView2.setImageBitmap(null);
        imageView3.setImageBitmap(null);
        imageView4.setImageBitmap(null);
        imageView5.setImageBitmap(null);
        index=0;
        return true;
      }
    });

    tv_click.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startCapture();
      }
    });


    mWindowManager.addView(displayView, layoutParams);

//
//    mFloatView.setOnTouchListener(new View.OnTouchListener() {
//      @Override
//      public boolean onTouch(View v, MotionEvent event) {
//        return mGestureDetector.onTouchEvent(event);
//      }
//    });

  }
  private class FloatingOnTouchListener implements View.OnTouchListener {
    private int x;
    private int y;

    @Override
    public boolean onTouch(View view, MotionEvent event) {
      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
          x = (int) event.getRawX();
          y = (int) event.getRawY();
          break;
        case MotionEvent.ACTION_MOVE:
          int nowX = (int) event.getRawX();
          int nowY = (int) event.getRawY();
          int movedX = nowX - x;
          int movedY = nowY - y;
          x = nowX;
          y = nowY;
          layoutParams.x = layoutParams.x + movedX;
          layoutParams.y = layoutParams.y + movedY;
          mWindowManager.updateViewLayout(view, layoutParams);
          break;
        default:
          break;
      }
      return false;
    }
  }


  private class FloatGestrueTouchListener implements GestureDetector.OnGestureListener {
    int lastX, lastY;
    int paramX, paramY;

    @Override
    public boolean onDown(MotionEvent event) {
      lastX = (int) event.getRawX();
      lastY = (int) event.getRawY();
      paramX = layoutParams.x;
      paramY = layoutParams.y;
      return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
      startScreenShot();
      return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      int dx = (int) e2.getRawX() - lastX;
      int dy = (int) e2.getRawY() - lastY;
      layoutParams.x = paramX + dx;
      layoutParams.y = paramY + dy;
      // 更新悬浮窗位置
      mWindowManager.updateViewLayout(displayView, layoutParams);
      return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      return false;
    }
  }


  private void startScreenShot() {


    Handler handler1 = new Handler();
    handler1.postDelayed(new Runnable() {
      public void run() {
        //start virtual
        startVirtual();
      }
    }, 5);

    handler1.postDelayed(new Runnable() {
      public void run() {
        //capture the screen
        startCapture();

      }
    }, 30);
  }


  private void createImageReader() {

    mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 1);

  }

  public void startVirtual() {
    if (mMediaProjection != null) {
      virtualDisplay();
    } else {
      setUpMediaProjection();
      virtualDisplay();
    }
  }

  public void setUpMediaProjection() {
    if (mResultData == null) {
      Intent intent = new Intent(Intent.ACTION_MAIN);
      intent.addCategory(Intent.CATEGORY_LAUNCHER);
      startActivity(intent);
    } else {
      mMediaProjection = getMediaProjectionManager().getMediaProjection(Activity.RESULT_OK, mResultData);
    }
  }

  private MediaProjectionManager getMediaProjectionManager() {

    return (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
  }

  private void virtualDisplay() {
    mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
        mScreenWidth, mScreenHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
        mImageReader.getSurface(), null, null);
  }

  private void startCapture() {

    Image image = mImageReader.acquireLatestImage();

    if (image == null) {
      startScreenShot();
    } else {
      SaveTask mSaveTask = new SaveTask();
      mSaveTask.doInBackground(image);
//      AsyncTask.executeParallel(mSaveTask, image);
    }
  }


  public class SaveTask extends AsyncTask<Image, Void, Bitmap> {


    @Override
    protected Bitmap doInBackground(Image... params) {

      if (params == null || params.length < 1 || params[0] == null) {

        return null;
      }

      Image image = params[0];

      int width = image.getWidth();
      int height = image.getHeight();
      final Image.Plane[] planes = image.getPlanes();
      final ByteBuffer buffer = planes[0].getBuffer();
      //每个像素的间距
      int pixelStride = planes[0].getPixelStride();
      //总的间距
      int rowStride = planes[0].getRowStride();
      int rowPadding = rowStride - pixelStride * width;
      Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
      bitmap.copyPixelsFromBuffer(buffer);
      bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
      image.close();
      File fileImage = null;
      index++;
      if(index ==1){
        imageView.setImageBitmap(bitmap);
      }else if(index ==2){
        imageView2.setImageBitmap(bitmap);

      }else if(index ==3){
        imageView3.setImageBitmap(bitmap);

      }else if(index ==4){
        imageView4.setImageBitmap(bitmap);

      }else if(index ==5){
        imageView5.setImageBitmap(bitmap);

      }
      if(!SPUtils.getInstance().getBoolean(MainActivity.SAVE_IMAGE_LOCAL_IS)){
        return null;
      }
      if (bitmap != null) {
        try {
          fileImage = new File(FileUtil.getScreenShotsName(getApplicationContext()));
          if (!fileImage.exists()) {
            fileImage.createNewFile();
          }
          FileOutputStream out = new FileOutputStream(fileImage);
          if (out != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            Intent media = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(fileImage);
            media.setData(contentUri);
            sendBroadcast(media);
          }
        } catch (FileNotFoundException e) {
          e.printStackTrace();
          fileImage = null;
        } catch (IOException e) {
          e.printStackTrace();
          fileImage = null;
        }
      }

      if (fileImage != null) {
        return bitmap;
      }
      return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
      super.onPostExecute(bitmap);
      //预览图片
      if (bitmap != null) {

        ((ScreenCaptureApplication) getApplication()).setmScreenCaptureBitmap(bitmap);
        Log.e("ryze", "获取图片成功");
        startActivity(PreviewPictureActivity.newIntent(getApplicationContext()));
      }

//      mFloatView.setVisibility(View.VISIBLE);

    }
  }


  private void tearDownMediaProjection() {
    if (mMediaProjection != null) {
      mMediaProjection.stop();
      mMediaProjection = null;
    }
  }

  private void stopVirtual() {
    if (mVirtualDisplay == null) {
      return;
    }
    mVirtualDisplay.release();
    mVirtualDisplay = null;
  }

  @Override
  public void onDestroy() {
    // to remove mFloatLayout from windowManager
    super.onDestroy();
//    if (mFloatView != null) {
//      mWindowManager.removeView(mFloatView);
//    }
    stopVirtual();

    tearDownMediaProjection();
  }


}

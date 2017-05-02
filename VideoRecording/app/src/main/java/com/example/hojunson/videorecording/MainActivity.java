package com.example.hojunson.videorecording;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{
    private static final String TAG = "opencv";
    private CameraBridgeViewBase mOpenCvVameraView;
    private Mat matInput;
    private Mat matResult;

    public native void ConvertRGBtoGray(long matAddrInput, long matAddrResult);
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            /*check binding of opencv */
            switch (status)
            {
                case LoaderCallbackInterface.SUCCESS:
                {
                    mOpenCvVameraView.enableView(); /* set java camera vew enable */
                }break;
                default:
                {
                    super.onManagerConnected(status);
                }break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);


        /*******  check Permission *************/
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(!hasPermissions(PERMISSIONS))
            {
                requestPermissions(PERMISSIONS,PERMISSIONS_REQUEST_CODE);
            }
        }

        mOpenCvVameraView = (CameraBridgeViewBase)findViewById(R.id.activity_surface_view);
        mOpenCvVameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvVameraView.setCvCameraViewListener(this);
        mOpenCvVameraView.setCameraIndex(0);
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);

    }

    @Override
    public void onPause()
    {
        super.onPause();
        if(mOpenCvVameraView != null)
            mOpenCvVameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume::Internal Opencv library not found.");

            /* link user activity with opencv */
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        }
        else
        {
            Log.d(TAG,"onResume::OpenCV library found inside package. Using it");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }


    public void onDestroy()
    {
        super.onDestroy();
        if(mOpenCvVameraView != null)
            mOpenCvVameraView.disableView();
    }



    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        matInput = inputFrame.rgba();
        if(matResult != null) matResult.release();
        matResult = new Mat(matInput.rows(),matInput.cols(), matInput.type());
        ConvertRGBtoGray(matInput.getNativeObjAddr(),matResult.getNativeObjAddr());

        return matResult;
    }

    static final int PERMISSIONS_REQUEST_CODE = 1000;

    String[] PERMISSIONS = {"android.permission.CAMERA"};

    private boolean hasPermissions(String[] permissions)
    {
        int result;
        /* check permision in PERMISSION */
        for (String perms : PERMISSIONS)
        {
            result = ContextCompat.checkSelfPermission(this,perms);

            if(result == PackageManager.PERMISSION_DENIED) {
                /* not allowed permission is detected */
                return false;
            }
        }

        /* all permission are allowed */
        return true;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);

        switch(requestCode)
        {
            case PERMISSIONS_REQUEST_CODE:
                if(grantResults.length > 0) {
                    boolean cameraPermissionAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (!cameraPermissionAccepted)
                        showDialogForPermission("need permission to run this app");
                }break;
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("notice");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("Yes",new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id){
                requestPermissions(PERMISSIONS,PERMISSIONS_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("No",new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface arg0, int arg1){
                finish();
            }
        });
        builder.create().show();
    }
}


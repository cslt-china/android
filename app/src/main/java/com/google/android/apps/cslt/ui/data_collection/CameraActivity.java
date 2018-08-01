package com.google.android.apps.cslt.ui.data_collection;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.widget.Button;

import com.google.android.apps.cslt.R;
import com.google.android.apps.cslt.utils.FileUtils;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CameraActivity extends AppCompatActivity {


    @BindView(R.id.surfaceview)
    SurfaceView mSurfaceView;
    @BindView(R.id.capture_btn)
    Button mCaptureBtn;

    private MediaRecorder mMediaRecorder;
    private Camera mCamera;
    private static String TAG = CameraActivity.class.getSimpleName();
    private boolean isRecording = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);
        prepareVideoRecorder();
        mCamera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] bytes, Camera camera) {
            }
        });
        mCamera.startPreview();

    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    private boolean prepareVideoRecorder() {

        getCameraInstance();
        if (mCamera == null) {
            return false;
        }
        mCamera.setDisplayOrientation(90);
        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();

        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);

        mMediaRecorder.setProfile(profile);
        mMediaRecorder.setOutputFile(FileUtils.getVideoPath());
        mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    public void getCameraInstance() {
        if(mCamera==null)
        {
            try {
                mCamera = Camera.open();
            } catch (Exception e) {
                Log.d(TAG, "getCameraInstance: " + e.getMessage());
            }
        }
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            try {
                mCamera.reconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera = null;
        }
    }

    @OnClick(R.id.capture_btn)
    public void onViewClicked() {
        if (isRecording) {
            stopRecord();
        } else {
            startRecord();
        }
        mCaptureBtn.setText(isRecording ?"Capture":"Stop");
        isRecording = isRecording==false;
    }

    public void  startRecord(){

        if (prepareVideoRecorder()) {
            mMediaRecorder.start();
        } else {
            releaseMediaRecorder();
        }
    }

    public void stopRecord()
    {
        mMediaRecorder.stop();
        releaseMediaRecorder();
        mCamera.stopPreview();
        mCamera.lock();
    }
}

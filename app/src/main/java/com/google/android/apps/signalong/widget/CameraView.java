package com.google.android.apps.signalong.widget;

import static android.hardware.camera2.CameraMetadata.LENS_FACING_BACK;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraDevice.StateCallback;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** CameraView implements preview and record video. */
public class CameraView extends TextureView implements TextureView.SurfaceTextureListener {

  private static final String TAG = "CameraActivity";
  private final Context context;
  private CameraDevice cameraDevice;
  private MediaRecorder mediaRecorder;
  private CameraCaptureSession cameraCaptureSession;
  private CallBack callBack;
  private Size videoSize;
  private Size previewSize;
  private HandlerThread backgroundThread;
  private Handler backgroundHandler;

  public CameraView(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.context = context;
    init();
  }

  private void init() {
    this.setSurfaceTextureListener(this);
  }

  private void startCameraCaptureSession(final int captureRequestTemplateType, String recordFile) {
    if (null == cameraDevice || !isAvailable()) {
      return;
    }
    try {
      closePreviewSession();
      SurfaceTexture texture = getSurfaceTexture();
      texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
      final CaptureRequest.Builder captureRequestBuilder =
          cameraDevice.createCaptureRequest(captureRequestTemplateType);
      captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
      List<Surface> surfaces = new ArrayList<>();
      Surface previewSurface = new Surface(texture);
      surfaces.add(previewSurface);
      captureRequestBuilder.addTarget(previewSurface);
      if (captureRequestTemplateType == CameraDevice.TEMPLATE_RECORD) {
        setUpMediaRecorder(recordFile);
        Surface recorderSurface = mediaRecorder.getSurface();
        surfaces.add(recorderSurface);
        captureRequestBuilder.addTarget(recorderSurface);
      }

      cameraDevice.createCaptureSession(
          surfaces,
          new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
              cameraCaptureSession = session;
              try {
                cameraCaptureSession.setRepeatingRequest(
                    captureRequestBuilder.build(), null, backgroundHandler);
                if (captureRequestTemplateType == CameraDevice.TEMPLATE_RECORD) {
                  callBack.onRecording();
                  mediaRecorder.start();
                } else {
                  callBack.onPreview();
                }
              } catch (CameraAccessException e) {
                Log.d(TAG, e.getMessage());
              }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {}
          },
          backgroundHandler);

    } catch (CameraAccessException | IOException e) {
      Log.d(TAG, e.getMessage());
    }
  }

  private void closePreviewSession() {
    if (cameraCaptureSession != null) {
      cameraCaptureSession.close();
      cameraCaptureSession = null;
    }
  }

  private Size chooseVideoSize(Size[] choices) {
    for (Size size : choices) {
      if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
        return size;
      }
    }
    return choices[choices.length - 1];
  }

  private void setUpMediaRecorder(String outputFile) throws IOException {
    mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
    mediaRecorder.setOutputFile(outputFile);
    mediaRecorder.setVideoEncodingBitRate(10000000);
    mediaRecorder.setVideoFrameRate(30);
    mediaRecorder.setVideoSize(videoSize.getWidth(), videoSize.getHeight());
    mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
    mediaRecorder.setOrientationHint(270);
    mediaRecorder.prepare();
  }

  private void startBackgroundThread() {
    backgroundThread = new HandlerThread(TAG);
    backgroundThread.start();
    backgroundHandler = new Handler(backgroundThread.getLooper());
  }

  private void stopBackgroundThread() {
    backgroundThread.quitSafely();
    try {
      backgroundThread.join();
      backgroundThread = null;
      backgroundHandler = null;
    } catch (InterruptedException e) {
      Log.d(TAG, e.getMessage());
    }
  }

  @Override
  public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
    startBackgroundThread();
    openCamera();
  }

  @Override
  public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

  @Override
  public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
    return false;
  }

  @Override
  public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

  @SuppressLint("MissingPermission")
  private void openCamera() {
    CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    try {
      String cameraId = manager.getCameraIdList()[LENS_FACING_BACK];
      StreamConfigurationMap map =
          manager
              .getCameraCharacteristics(cameraId)
              .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
      if (map == null) {
        throw new RuntimeException("Cannot get available preview/video sizes");
      }
      videoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
      previewSize = videoSize;
      mediaRecorder = new MediaRecorder();
      manager.openCamera(
          cameraId,
          new StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
              cameraDevice = camera;
              callBack.onCameraOpened();
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {}

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {
              camera.close();
              cameraDevice = null;
              callBack.onCameraError();
            }
          },
          backgroundHandler);
    } catch (CameraAccessException e) {
      Log.d(TAG, e.getMessage());
    }
  }

  public void closeCamera() {
    closePreviewSession();
    if (null != cameraDevice) {
      cameraDevice.close();
      cameraDevice = null;
    }
    if (null != mediaRecorder) {
      mediaRecorder.release();
      mediaRecorder = null;
    }
    stopBackgroundThread();
  }

  public void startPreview() {
    startCameraCaptureSession(CameraDevice.TEMPLATE_PREVIEW, null);
  }

  public void startRecord(String outputFile) {
    startCameraCaptureSession(CameraDevice.TEMPLATE_RECORD, outputFile);
  }

  public void stopRecording() {
    if (mediaRecorder != null) {
      try {
        //Prevent RuntimeException when recording time is less than 1 second
        //see: https://www.cnblogs.com/over140/p/3811084.html
        mediaRecorder.setOnErrorListener(null);
        mediaRecorder.setPreviewDisplay(null);

        mediaRecorder.stop();
      } catch (IllegalStateException e) {
        Log.w("Exception", Log.getStackTraceString(e));
      } catch(RuntimeException e) {
        Log.w("Exception", Log.getStackTraceString(e));
      } catch (Exception e) {
        Log.w("Exception", Log.getStackTraceString(e));
      }

      mediaRecorder.reset();
    }
  }

  public void setCallBack(CallBack callBack) {
    this.callBack = callBack;
  }

  /** CallBack is called when entering a state. */
  static public class CallBack {
    public void onRecording() {}

    public void onPreview() {}

    public void onCameraOpened() {}

    public void onCameraError() {}
  }
}

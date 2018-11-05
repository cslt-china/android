/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.apps.signalong;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.apps.signalong.widget.AutoFitTextureView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class RecordFragment2 extends BaseFragment {


  private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
  private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
  private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
  private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();

  private static final String TAG = "RecordFragment2";
  private static final int REQUEST_VIDEO_PERMISSIONS = 1;
  private static final String FRAGMENT_DIALOG = "dialog";

  private static final String[] VIDEO_PERMISSIONS = {
      Manifest.permission.CAMERA,
      Manifest.permission.RECORD_AUDIO,
      };

  static {
    DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
    DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
    DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
    DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
  }

  static {
    INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
    INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
    INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
    INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
  }

  /**
   * An {@link AutoFitTextureView} for camera preview.
   */
  private AutoFitTextureView mTextureView;

  /**
   * Button to record video
   */
  private Button mButtonVideo;

  /**
   * Other widgets
   */
  private TextView titleTextView;
  private ProgressBar progressBar;
  private ValueAnimator progressAnimator;

  /**
   * A reference to the opened {@link android.hardware.camera2.CameraDevice}.
   */
  private CameraDevice mCameraDevice;

  /**
   * A reference to the current {@link android.hardware.camera2.CameraCaptureSession} for
   * preview.
   */
  private CameraCaptureSession mPreviewSession;

  /**
   * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
   * {@link TextureView}.
   */
  private TextureView.SurfaceTextureListener mSurfaceTextureListener
      = new TextureView.SurfaceTextureListener() {

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture,
                                          int width, int height) {
      openCamera(width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture,
                                            int width, int height) {
      configureTransform(width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
      return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

  };

  /**
   * The {@link android.util.Size} of camera preview.
   */
  private Size mPreviewSize;

  /**
   * The {@link android.util.Size} of video recording.
   */
  private Size mVideoSize;

  /**
   * MediaRecorder
   */
  private MediaRecorder mMediaRecorder;

  /**
   * Whether the app is recording video now
   */
  private boolean mIsRecordingVideo;

  /**
   * An additional thread for running tasks that shouldn't block the UI.
   */
  private HandlerThread mBackgroundThread;

  /**
   * A {@link Handler} for running tasks in the background.
   */
  private Handler mBackgroundHandler;

  /**
   * A {@link Semaphore} to prevent the app from exiting before closing the camera.
   */
  private Semaphore mCameraOpenCloseLock = new Semaphore(1);
  private Semaphore mRecordStartStopLock = new Semaphore(1);

  /**
   * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its status.
   */
  private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

    @Override
    public void onOpened(@NonNull CameraDevice cameraDevice) {
      mCameraDevice = cameraDevice;
      startPreview();
      mCameraOpenCloseLock.release();
      if (null != mTextureView) {
        configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
      }
      mCameraCallback.onOpened();
    }

    @Override
    public void onDisconnected(@NonNull CameraDevice cameraDevice) {
      mCameraOpenCloseLock.release();
      cameraDevice.close();
      mCameraDevice = null;

      mCameraCallback.onError(
          "disconnect due tochange in security policy or permissions");
    }

    private String errorCode2String(int error) {
      switch (error) {
        case ERROR_CAMERA_IN_USE:
          return "ERROR_CAMERA_IN_USE";
        case ERROR_MAX_CAMERAS_IN_USE:
          return "ERROR_MAX_CAMERAS_IN_USE";
        case ERROR_CAMERA_DISABLED:
          return "ERROR_CAMERA_DISABLED";
        case ERROR_CAMERA_DEVICE:
          return "ERROR_CAMERA_DEVICE";
        case ERROR_CAMERA_SERVICE:
          return "ERROR_CAMERA_SERVICE";
        default:
          return "ERROR_UNKNOWN";
      }
    }

    @Override
    public void onError(@NonNull CameraDevice cameraDevice, int error) {
      mCameraOpenCloseLock.release();
      cameraDevice.close();
      mCameraDevice = null;
      mCameraCallback.onError(errorCode2String(error));
    }

  };
  private Integer mSensorOrientation;
  private CaptureRequest.Builder mPreviewBuilder;

  public static RecordFragment2 newInstance() {
    return new RecordFragment2();
  }

  /**
   * In this sample, we choose a video size with 3x4 aspect ratio. Also, we don't use sizes
   * larger than 1080p, since MediaRecorder cannot handle such a high-resolution video.
   *
   * @param choices The list of available sizes
   * @return The video size
   */
  private static Size chooseVideoSize(Size[] choices) {
    for (Size size : choices) {
      if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
        return size;
      }
    }
    Log.e(TAG, "Couldn't find any suitable video size");
    return choices[choices.length - 1];
  }

  /**
   * Given {@code choices} of {@code Size}s supported by a camera, chooses the smallest one whose
   * width and height are at least as large as the respective requested values, and whose aspect
   * ratio matches with the specified value.
   *
   * @param choices     The list of sizes that the camera supports for the intended output class
   * @param width       The minimum desired width
   * @param height      The minimum desired height
   * @param aspectRatio The aspect ratio
   * @return The optimal {@code Size}, or an arbitrary one if none were big enough
   */
  private static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
    // Collect the supported resolutions that are at least as big as the preview Surface
    List<Size> bigEnough = new ArrayList<>();
    int w = aspectRatio.getWidth();
    int h = aspectRatio.getHeight();
    for (Size option : choices) {
      if (option.getHeight() == option.getWidth() * h / w &&
          option.getWidth() >= width && option.getHeight() >= height) {
        bigEnough.add(option);
      }
    }

    // Pick the smallest of those, assuming we found any
    if (bigEnough.size() > 0) {
      return Collections.min(bigEnough, new CompareSizesByArea());
    } else {
      Log.e(TAG, "Couldn't find any suitable preview size");
      return choices[0];
    }
  }

  static private int getHectoAspectRatio(Size size) {
    return size.getWidth() * 100 / size.getHeight();
  }

  private void setBestMatchVideoAndPreviewSize(Size[] choices, int viewWidth,
                                               int viewHeight) {
    List<Size> bigEnough = new ArrayList<>();
    for (Size choice : choices) {
      int area = choice.getWidth() * choice.getHeight();
      if (640*480 <= area && area <= 1280*720) {
        bigEnough.add(choice);
      }
    }

    final int expectHectoAspectRatio;

    int orientation = getResources().getConfiguration().orientation;
    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
      expectHectoAspectRatio = viewWidth * 100 / viewHeight;
    } else {
      expectHectoAspectRatio = viewHeight * 100 / viewWidth;
    }

    Size best = Collections.min(bigEnough,
        (Size lhs, Size rhs)-> Math.abs(getHectoAspectRatio(lhs) - expectHectoAspectRatio) -
                Math.abs(getHectoAspectRatio(rhs) - expectHectoAspectRatio));

    mPreviewSize = best;
    mVideoSize = best;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_record2, container, false);
  }

  @Override
  public void onViewCreated(final View view, Bundle savedInstanceState) {
    mTextureView =  view.findViewById(R.id.texture);
    progressBar = view.findViewById(R.id.progres_bar);
    titleTextView = view.findViewById(R.id.camera_title);
    mButtonVideo = view.findViewById(R.id.back_button);
    mButtonVideo.setOnClickListener((v)-> cancelRecording());
    initProgressAnimation();
  }

  @Override
  public void onResume() {
    super.onResume();
    startBackgroundThread();
    if (mTextureView.isAvailable()) {
      openCamera(mTextureView.getWidth(), mTextureView.getHeight());
    } else {
      mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
    }
  }

  @Override
  public void onPause() {
    if (mIsRecordingVideo) {
      // Stop recording
      stopMediaRecorderSafely();
      mIsRecordingVideo = false;
    }
    closeCamera();
    stopBackgroundThread();
    progressAnimator.cancel();
    progressBar.setProgress(0);
    super.onPause();
  }

  /**
   * Starts a background thread and its {@link Handler}.
   */
  private void startBackgroundThread() {
    mBackgroundThread = new HandlerThread("CameraBackground");
    mBackgroundThread.start();
    mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
  }

  /**
   * Stops the background thread and its {@link Handler}.
   */
  private void stopBackgroundThread() {
    mBackgroundHandler.removeCallbacksAndMessages(null);
    mBackgroundThread.quitSafely();
    try {
      mBackgroundThread.join();
      mBackgroundThread = null;
      mBackgroundHandler = null;
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }


  /**
   * Tries to open a {@link CameraDevice}. The result is listened by `mStateCallback`.
   */
  @SuppressLint("MissingPermission")
  private void openCamera(int width, int height) {
    final Activity activity = getActivity();
    if (null == activity || activity.isFinishing()) {
      return;
    }
    CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
    try {
      Log.d(TAG, "tryAcquire");
      if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
        throw new RuntimeException("Time out waiting to lock camera opening.");
      }
      String cameraId = manager.getCameraIdList()[CameraMetadata.LENS_FACING_BACK];

      // Choose the sizes for camera preview and video recording
      CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
      StreamConfigurationMap map = characteristics
          .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
      mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
      if (map == null) {
        throw new RuntimeException("Cannot get available preview/video sizes");
      }
      /*
      mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
      mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                                       width, height, mVideoSize);
                                       */
      setBestMatchVideoAndPreviewSize(map.getOutputSizes(SurfaceTexture.class),
                                      width, height);

      int orientation = getResources().getConfiguration().orientation;
      if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
      } else {
        mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
      }
      configureTransform(width, height);
      mMediaRecorder = new MediaRecorder();
      manager.openCamera(cameraId, mStateCallback, null);
    } catch (CameraAccessException e) {
      mCameraCallback.onError(
          "Cannot access the camera: " + e.getMessage());
    } catch (NullPointerException e) {
      mCameraCallback.onError(
          "This device doesn\'t support Camera2 API: ");
    } catch (InterruptedException e) {
      throw new RuntimeException("Interrupted while trying to lock camera opening.");
    }
  }

  private void closeCamera() {
    try {
      mCameraOpenCloseLock.acquire();
      closePreviewSession();
      if (null != mCameraDevice) {
        mCameraDevice.close();
        mCameraDevice = null;
      }
    } catch (InterruptedException e) {
      throw new RuntimeException("Interrupted while trying to lock camera closing.");
    } finally {
      mCameraOpenCloseLock.release();
    }
  }

  private void stopMediaRecorderSafely() {
    if (mMediaRecorder == null) {
      return;
    }
    try {
      //Prevent RuntimeException when recording time is less than 1 second
      //see: https://www.cnblogs.com/over140/p/3811084.html
      mMediaRecorder.setOnErrorListener(null);
      mMediaRecorder.setPreviewDisplay(null);

      mMediaRecorder.stop();
      mMediaRecorder.reset();
    } catch (IllegalStateException e) {
      //Log.w("Exception", Log.getStackTraceString(e));
    } catch(RuntimeException e) {
      //Log.w("Exception", Log.getStackTraceString(e));
    } catch (Exception e) {
      Log.w("Exception", Log.getStackTraceString(e));
    }
  }

  /**
   * Start the camera preview.
   */
  private void startPreview() {
    if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
      return;
    }
    try {
      closePreviewSession();
      SurfaceTexture texture = mTextureView.getSurfaceTexture();
      assert texture != null;
      texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
      mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

      Surface previewSurface = new Surface(texture);
      mPreviewBuilder.addTarget(previewSurface);

      mCameraDevice.createCaptureSession(Collections.singletonList(previewSurface),
                                         new CameraCaptureSession.StateCallback() {

                                           @Override
                                           public void onConfigured(@NonNull CameraCaptureSession session) {
                                             mPreviewSession = session;
                                             updatePreview();
                                           }

                                           @Override
                                           public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                                             mCameraCallback.onError("device configure failed");
                                           }
                                         }, mBackgroundHandler);
    } catch (CameraAccessException e) {
      e.printStackTrace();
    }
  }

  /**
   * Update the camera preview. {@link #startPreview()} needs to be called in advance.
   */
  private void updatePreview() {
    if (null == mCameraDevice) {
      return;
    }
    try {
      setUpCaptureRequestBuilder(mPreviewBuilder);
      HandlerThread thread = new HandlerThread("CameraPreview");
      thread.start();
      mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, mBackgroundHandler);
    } catch (CameraAccessException e) {
      e.printStackTrace();
    }
  }

  private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
    builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
  }

  /**
   * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
   * This method should not to be called until the camera preview size is determined in
   * openCamera, or until the size of `mTextureView` is fixed.
   *
   * @param viewWidth  The width of `mTextureView`
   * @param viewHeight The height of `mTextureView`
   */
  private void configureTransform(int viewWidth, int viewHeight) {
    Activity activity = getActivity();
    if (null == mTextureView || null == mPreviewSize || null == activity) {
      return;
    }
    int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
    Matrix matrix = new Matrix();
    RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
    RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
    float centerX = viewRect.centerX();
    float centerY = viewRect.centerY();
    bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());

    //scale to bufferRect
    matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
    float scale = Math.max(
        (float) viewHeight / mPreviewSize.getHeight(),
        (float) viewWidth / mPreviewSize.getWidth());
    //rescale to restore view width or height
    matrix.postScale(scale, scale, centerX, centerY);

    if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
      //the old code is 90 * (rotation - 2), it equals 360 - 90 * rotation
      matrix.postRotate(360 - 90 * rotation, centerX, centerY);
    }
    mTextureView.setTransform(matrix);
  }

  private void setUpMediaRecorder(String videoFilePath) throws IOException {
    final Activity activity = getActivity();
    if (null == activity) {
      return;
    }
    mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
    mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
    mMediaRecorder.setOutputFile(videoFilePath);
    mMediaRecorder.setVideoEncodingBitRate(10000000);
    mMediaRecorder.setVideoFrameRate(30);
    mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
    mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
    int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
    switch (mSensorOrientation) {
      case SENSOR_ORIENTATION_DEFAULT_DEGREES:
        mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
        break;
      case SENSOR_ORIENTATION_INVERSE_DEGREES:
        mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
        break;
    }
    mMediaRecorder.prepare();
  }

  private String getVideoFilePath(Context context) {
    final File dir = context.getExternalFilesDir(null);
    return (dir == null ? "" : (dir.getAbsolutePath() + "/"))
           + System.currentTimeMillis() + ".mp4";
  }

  private void startRecordingVideo(String videoFilePath, int recordingTime) {
    if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
      return;
    }

    try {
      closePreviewSession();
      setUpMediaRecorder(videoFilePath);
      SurfaceTexture texture = mTextureView.getSurfaceTexture();
      assert texture != null;
      texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
      mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
      List<Surface> surfaces = new ArrayList<>();

      // Set up Surface for the camera preview
      Surface previewSurface = new Surface(texture);
      surfaces.add(previewSurface);
      mPreviewBuilder.addTarget(previewSurface);

      // Set up Surface for the MediaRecorder
      Surface recorderSurface = mMediaRecorder.getSurface();
      surfaces.add(recorderSurface);
      mPreviewBuilder.addTarget(recorderSurface);

      // Start a capture session
      // Once the session starts, we can update the UI and start recording
      mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

        @Override
        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
          mPreviewSession = cameraCaptureSession;
          updatePreview();
          getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
              // UI
              mMediaRecorder.setOnErrorListener(
                  (MediaRecorder mr, int what, int extra)-> {
                  String message = String.format("record error: %d %d",
                                                 what, extra);
                  mRecordCallback.onError(message);
                });
              mMediaRecorder.start();

              progressAnimator.setDuration(Math.min(2, recordingTime) * 1000);
              progressAnimator.start();

              mIsRecordingVideo = true;
            }
          });
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
          Log.e("wxg", "camera configre error");
          mCameraCallback.onError("device configure failed");
        }
      }, mBackgroundHandler);
    } catch (CameraAccessException | IOException e) {
      e.printStackTrace();
    }

  }

  private void closePreviewSession() {
    if (mPreviewSession != null) {
      mPreviewSession.close();
      mPreviewSession = null;
    }
  }

  private void stopRecordingVideo() {
    if (mIsRecordingVideo == false) {
      return;
    }
    // UI
    mIsRecordingVideo = false;
    // Stop recording
    stopMediaRecorderSafely();

    startPreview();
  }

  /**
   * Compares two {@code Size}s based on their areas.
   */
  static class CompareSizesByArea implements Comparator<Size> {

    @Override
    public int compare(Size lhs, Size rhs) {
      // We cast here to ensure the multiplications won't overflow
      return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                         (long) rhs.getWidth() * rhs.getHeight());
    }

  }

  private CameraCallback mCameraCallback = new CameraCallback() ;

  void setCameraCallback(CameraCallback cameraCallback) {
    assert(cameraCallback != null);
    this.mCameraCallback = cameraCallback;
  }

  public static class CameraCallback {
    public void onOpened() { }
    public void onError(String errorMessage) { }
  }

  public static class RecordCallback {
    public void onStart() {};
    public void onFinished() {};
    public void onCancel() {};
    public void onError(String errorMessage) {};
  }

  RecordCallback mRecordCallback = new RecordCallback();

  public void setRecordCallback(RecordCallback mRecordCallback) {
    this.mRecordCallback = mRecordCallback;
  }

  private void initProgressAnimation() {
    progressBar.setMax(100);
    progressAnimator = ValueAnimator.ofInt(0, 100);
    progressAnimator.addUpdateListener(
        valueAnimator ->
        {
          progressBar.setProgress((int) valueAnimator.getAnimatedValue());
        });

    progressAnimator.addListener(mAnimationListener);
  }

  private CancelOrEndAnimatorListener mAnimationListener =
      new CancelOrEndAnimatorListener() {
    public void onStart(Animator animator) {
      mRecordCallback.onStart();
    }
    public void onEnd(Animator animator) {
      stopRecordingVideo();
      mRecordCallback.onFinished();
      progressBar.setProgress(0);
    }
    public void onCancel(Animator animator) {
      stopRecordingVideo();
      mRecordCallback.onCancel();
      progressBar.setProgress(0);
    }
  };

  public void startRecord(String name, String videoFilePath, int recordingTime) {
    titleTextView.setText(String.format(getString(R.string.please_sign), name));
    startRecordingVideo(videoFilePath, recordingTime);
  }

  public void cancelRecording() {
    progressAnimator.cancel();
    progressBar.setProgress(0);
  }
}

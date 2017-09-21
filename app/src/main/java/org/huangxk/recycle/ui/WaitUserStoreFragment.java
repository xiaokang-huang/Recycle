package org.huangxk.recycle.ui;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.huangxk.recycle.CountDown;
import org.huangxk.recycle.R;
import org.huangxk.recycle.Speeker;
import org.huangxk.recycle.TaskData;
import org.huangxk.recycle.statusMachine.statusBase;
import org.huangxk.recycle.statusMachine.statusManager;

import java.util.Arrays;

public class WaitUserStoreFragment extends FragmentBase implements CountDown.CountListener, View.OnClickListener {
    private static final String LOG_TAG = "WaitUserStoreFragment";
    private ImageView mNext;
    private TextView mTick;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private CountDown mCountDown;

    private Handler mChildHandler;
    private Handler mMainHandler;
    private String mCameraID;
    private CameraManager mCameraManager;
    private ImageReader mImageReader;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCameraCaptureSession;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_userstore_fragment, container, false);

        mNext = (ImageView)view.findViewById(R.id.next);
        mTick = (TextView) view.findViewById(R.id.tick);

        mSurfaceView = (SurfaceView)view.findViewById(R.id.preview);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);

        mMainHandler = new Handler(getActivity().getMainLooper());

        mNext.setOnClickListener(this);

        return view;
        //return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        int timeout = this.getActivity().getResources().getInteger(R.integer.waituserstore_timeout_second);
        mTick.setText(String.format("%d", timeout));
        mCountDown = new CountDown(timeout, this);

        mSurfaceHolder.addCallback(mCallback);
        Speeker.getInstance().startSpeak(Speeker.SOUND_WAITUSER, 1000);
        super.onResume();
    }

    @Override
    public void onPause() {
        Speeker.getInstance().stopSpeak();
        mCountDown.cancelCount();
        super.onPause();
    }

    @Override
    public void onCountChanged(int count) {
        final int time = count;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTick.setText(String.format("%d", time));
            }
        });

        if (count == 0) {
            statusManager.getInstance().getCurrentStatus().updateStatus(statusBase.EVENT_TIMEOUT);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.next) {
            if (mCameraDevice != null)
                takePicture();
            else
                statusManager.getInstance().getCurrentStatus().updateStatus(statusBase.EVENT_NEXT1);
        }
    }

    private void initCamera2() {
        HandlerThread handlerThread = new HandlerThread("Camera2");
        handlerThread.start();
        mChildHandler = new Handler(handlerThread.getLooper());
        mCameraID = "" + CameraCharacteristics.LENS_FACING_BACK;//后摄像头

        mImageReader = ImageReader.newInstance(640, 480, ImageFormat.JPEG,1);
        mImageReader.setOnImageAvailableListener(mImageGetListener, mMainHandler);

        mCameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraID = (mCameraManager.getCameraIdList())[0];
            // set surface rotation
            Matrix matrix = new Matrix();
            matrix.setRotate(90);
            mCameraManager.openCamera(mCameraID, mCameraDeviceStateCallback, mMainHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void takePreview() {
        try { // 创建预览需要的
            final CaptureRequest.Builder previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(mSurfaceHolder.getSurface());

            mCameraDevice.createCaptureSession(Arrays.asList(mSurfaceHolder.getSurface(), mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    if (null == mCameraDevice) return;
                    mCameraCaptureSession = cameraCaptureSession;
                    CaptureRequest previewRequest = previewRequestBuilder.build();
                    try {
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        mCameraCaptureSession.setRepeatingRequest(previewRequest, null, mChildHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(getActivity(), "配置失败", Toast.LENGTH_SHORT).show();
                }
            }, mChildHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void takePicture() {
        if (mCameraDevice == null) return;
        final CaptureRequest.Builder captureRequestBuilder;
        try {
            captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureRequestBuilder.addTarget(mImageReader.getSurface());
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            CaptureRequest mCaptureRequest = captureRequestBuilder.build();
            mCameraCaptureSession.capture(mCaptureRequest, null, mChildHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            initCamera2();
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            if (null != mCameraDevice) {
                mCameraDevice.close();
                WaitUserStoreFragment.this.mCameraDevice = null;
            }
        }
    };

    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            takePreview();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            if (null != mCameraDevice) {
                mCameraDevice.close();
                WaitUserStoreFragment.this.mCameraDevice = null;
            }
        }

        @Override
        public void onError(CameraDevice cameraDevice, int i) {
            Toast.makeText(getActivity(), "摄像头开启失败", Toast.LENGTH_SHORT).show();
        }
    };

    private ImageReader.OnImageAvailableListener mImageGetListener = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader imageReader) {
            Log.d(LOG_TAG, "onImageAvailable format = " + imageReader.getImageFormat());
            mCameraDevice.close();
            Image image = imageReader.acquireNextImage();
            TaskData.getInstance().getAnimalInfo().mJpegPic = image;

            statusManager.getInstance().getCurrentStatus().updateStatus(statusBase.EVENT_NEXT1);
        }
    };
}

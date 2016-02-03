package tk.sanchev.computervision;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements CvCameraViewListener2, OnClickListener {

    // A tag for log output.
    private static final String TAG = "SmileOCV";
    // A key for storing the index of the active camera.
    private static final String STATE_CAMERA_INDEX = "cameraIndex";
    // The index of the active camera.
    private int mCameraIndex;
    // Whether the active camera is front-facing.
    // If so, the camera view should be mirrored.
    private boolean mIsCameraFrontFacing;
    // The number of cameras on the device.
    private int mNumCameras;
    // The camera view.
    private CameraBridgeViewBase mCameraView;
    // Whether the next camera frame should be saved as a photo.
    private boolean mIsPhotoPending;
    // A matrix that is used when saving photos.
    private Mat mBgr;
    // Whether an asynchronous menu action is in progress.
    // If so, menu interaction should be disabled.
    private boolean mIsMenuLocked;

    private CameraBridgeViewBase mOpenCvCameraView;
    private int absoluteFaceSize;
    private CascadeClassifier faceCascadeOne;
    private int currentCameraIngex = -1;
    private boolean isCurrentCameraFront = false;
    private Mat matEmoji;
    private Mat matMask;
    AssetManager assetManager;

    static {
        System.loadLibrary("opencv_java3");
    }

    //Activity methods start
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (savedInstanceState != null) {
            mCameraIndex = savedInstanceState.getInt(STATE_CAMERA_INDEX, 0);
        } else {
            mCameraIndex = 0;
        }

        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(mCameraIndex, cameraInfo);
        mIsCameraFrontFacing = (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT);
        mNumCameras = Camera.getNumberOfCameras();

        setContentView(R.layout.activity_main);

        mCameraView = (CameraBridgeViewBase) findViewById(R.id.view);
        mCameraView.setCameraIndex(mCameraIndex);
        mCameraView.setCvCameraViewListener(this);
        mCameraView.enableView();
        mCameraView.setVisibility(View.VISIBLE);

        if (mNumCameras > 1) {
            FloatingActionButton fabFlipCam = (FloatingActionButton) findViewById(R.id.fabFlipCam);
            fabFlipCam.setOnClickListener(this);
            fabFlipCam.setVisibility(View.VISIBLE);
        }

        FloatingActionButton fabPhoto = (FloatingActionButton) findViewById(R.id.fabPhoto);
        fabPhoto.setOnClickListener(this);

        mBgr = new Mat();
    }

    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        assetManager = getAssets();
        faceCascadeOne = newCascadeClassifier("lbpcascade_frontalface.xml");

        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.view);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        //Инициализация камер START
        int numberOfCameras = Camera.getNumberOfCameras();
        if (numberOfCameras >= 1) {
            currentCameraIngex = 0;
            checkIsCurrentCameraFront();
            if (numberOfCameras > 1) {
                fab.setVisibility(View.VISIBLE);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentCameraIngex = currentCameraIngex ^ 1; //bitwise not operation to flip 1 to 0 and vice versa
                        checkIsCurrentCameraFront();
                        mOpenCvCameraView.disableView();
                        mOpenCvCameraView.setCameraIndex(currentCameraIngex);
                        mOpenCvCameraView.enableView();
                    }
                });
            }
        }
        mOpenCvCameraView.setCameraIndex(currentCameraIngex);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        //Инициализация камер END

        //Инициализация EMOJI START
        Bitmap bitmapEmoji = BitmapFactory.decodeResource(getResources(), R.drawable.emoji);
        Bitmap bitmapMask = BitmapFactory.decodeResource(getResources(), R.drawable.mask);

        matEmoji = new Mat(bitmapEmoji.getWidth(), bitmapEmoji.getHeight(), CvType.CV_8UC3);
        Utils.bitmapToMat(bitmapEmoji, matEmoji);

        matMask = new Mat(bitmapMask.getWidth(), bitmapMask.getHeight(), CvType.CV_8UC3);
        Utils.bitmapToMat(bitmapMask, matMask);
        //Инициализация EMOJI END

        //Сделать фото START
        FloatingActionButton photo = (FloatingActionButton) findViewById(R.id.photo);
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //реализовать снимок
            }
        });
        //Сделать фото END
    }
    */

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(STATE_CAMERA_INDEX, mCameraIndex);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onPause() {
        if (mCameraView != null)
            mCameraView.disableView();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        OpenCVLoader.initDebug();
        mCameraView.enableView();
        mIsMenuLocked = false;
    }

    @Override
    protected void onDestroy() {
        if (mCameraView != null)
            mCameraView.disableView();
        super.onDestroy();
    }
    //Activity methods end

    //CvCameraViewListener2 methods start
    @Override
    public void onCameraViewStarted(int width, int height) {
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat result = inputFrame.gray();

        if (mIsCameraFrontFacing)
            Core.flip(result, result, 1);

        if (mIsPhotoPending) {
            mIsPhotoPending = false;
            takePhoto(result);
        }

        return result;
    }

    /*
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat result = inputFrame.rgba();
        if (isCurrentCameraFront) //зеркальное отоброжение входного кадра
            Core.flip(result, result, 1);

        Mat grayFrame = inputFrame.gray();
        if (isCurrentCameraFront) //зеркальное отоброжение входного кадра
            Core.flip(grayFrame, grayFrame, 1);
        Imgproc.equalizeHist(grayFrame, grayFrame);

        if (this.absoluteFaceSize == 0) {
            int height = grayFrame.rows();
            if (Math.round(height * 0.2f) > 0)
                this.absoluteFaceSize = Math.round(height * 0.2f);
        }

        MatOfRect faces = new MatOfRect();
        faceCascadeOne.detectMultiScale(grayFrame, faces, 1.1, 2, Objdetect.CASCADE_SCALE_IMAGE, new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());

        Rect[] facesArray = faces.toArray();
        for (Rect rect : facesArray) {
            Mat submat = result.submat(rect);
            Mat resizedEmoji = new Mat();
            Imgproc.resize(matEmoji, resizedEmoji, submat.size());
            Mat resizedMask = new Mat();
            Imgproc.resize(matMask, resizedMask, submat.size());
            resizedEmoji.copyTo(submat, resizedMask);
        }
        return result;
    }
    */
    //CvCameraViewListener2 methods end

    //OnClickListener methods start
    @Override
    public void onClick(View v) {
        if (mIsMenuLocked) {
            return;
        }
        switch (v.getId()) {
            case R.id.fabFlipCam:
                mIsMenuLocked = true;
                // With another camera index, recreate the activity.
                mCameraIndex++;
                if (mCameraIndex == mNumCameras) {
                    mCameraIndex = 0;
                }
                recreate();
                return;
            case R.id.fabPhoto:
                mIsMenuLocked = true;
                // Next frame, take the photo.
                mIsPhotoPending = true;
                return;
            default:
        }
    }
    //OnClickListener methods end

    //Smile methods start
    private void checkIsCurrentCameraFront() {
        Camera.CameraInfo currentCameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(currentCameraIngex, currentCameraInfo);
        isCurrentCameraFront = (currentCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
    }

    private CascadeClassifier newCascadeClassifier(String file) {
        try {
            InputStream is = assetManager.open(file);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, file);
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1)
                os.write(buffer, 0, bytesRead);
            is.close();
            os.close();

            CascadeClassifier result = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            cascadeDir.delete();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void takePhoto(Mat result) {
        //Determinate the path and metadata for the photo
        final long currentTime = System.currentTimeMillis();
        final String appName = getString(R.string.app_name);
        final String galeryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        final String albumPath = galeryPath + "/" + appName;
        final String photoPath = albumPath + "/" + currentTime + ".png";
        final ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, photoPath);
        values.put(MediaStore.Images.Media.MIME_TYPE, LabActivity.PHOTO_MIME_TYPE);
        values.put(MediaStore.Images.Media.TITLE, appName);
        values.put(MediaStore.Images.Media.DESCRIPTION, appName);
        values.put(MediaStore.Images.Media.DATE_TAKEN, currentTime);

        //Ensure that the album directory exist
        File album = new File(albumPath);
        if (!album.isDirectory() && !album.mkdirs()) {
            Log.e(TAG, "Failed to create album directory at " + albumPath);
            onTakePhotoFailed();
            return;
        }

        //Try to create the photo
        Imgproc.cvtColor(result, mBgr, Imgproc.COLOR_GRAY2BGR, 3);
        if (!Imgcodecs.imwrite(photoPath, mBgr)) {
            Log.e(TAG, "Failed to save photo to " + photoPath);
            onTakePhotoFailed();
        }
        Log.d(TAG, "Photo saved successfully to " + photoPath);

        //Try to insert the photo into MediaStore
        Uri uri;
        try {
            uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } catch (final Exception e) {
            Log.e(TAG, "Failed to insert photo into MediaStore");
            e.printStackTrace();

            //Since the insertion failed, delete the photo
            File photo = new File(photoPath);
            if (!photo.delete())
                Log.e(TAG, "Failed to delete non-inserted photo");

            onTakePhotoFailed();
            return;
        }
        Log.d(TAG, "Photo inserted successfully into MediaStore");

        //Open photo in LabActivity
        final Intent intent = new Intent(this, LabActivity.class);
        intent.putExtra(LabActivity.EXTRA_PHOTO_URI, uri);
        intent.putExtra(LabActivity.EXTRA_PHOTO_DATA_PATH, photoPath);
        startActivity(intent);
    }

    private void onTakePhotoFailed() {
        mIsMenuLocked = false;

        final String errorMessage = getString(R.string.photo_error_message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
    //Smile methods end
}
package tk.sanchev.computervision;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

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
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements CvCameraViewListener2 {
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
    }

    private void checkIsCurrentCameraFront() {
        Camera.CameraInfo currentCameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(currentCameraIngex, currentCameraInfo);
        isCurrentCameraFront = (currentCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initDebug();
        mOpenCvCameraView.enableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

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
}
package tk.sanchev.computervision;

import android.content.Context;
import android.content.res.AssetManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements CvCameraViewListener2 {
    private CameraBridgeViewBase mOpenCvCameraView;
    private int absoluteFaceSize;
    private CascadeClassifier faceCascadeOne;
    private CascadeClassifier faceCascadeTwo;
    private int currentCameraIngex = -1;
    private boolean isCurrentCameraFront = false;
    private Mat smile;

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
        faceCascadeTwo = newCascadeClassifier("haarcascade_frontalface_alt.xml");

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
                        currentCameraIngex = currentCameraIngex^1; //bitwise not operation to flip 1 to 0 and vice versa
                        checkIsCurrentCameraFront();
                        mOpenCvCameraView.disableView();
                        mOpenCvCameraView.setCameraIndex(currentCameraIngex);
                        mOpenCvCameraView.enableView();
                    }
                });
            }
        }
        //Инициализация камер END

        mOpenCvCameraView.setCameraIndex(currentCameraIngex);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        try {
            smile = Utils.loadResource(this, R.drawable.emoji, CvType.CV_16S);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkIsCurrentCameraFront() {
        Camera.CameraInfo currentCameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(currentCameraIngex, currentCameraInfo);
        if ((currentCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT))
            isCurrentCameraFront = true;
        else
            isCurrentCameraFront = false;
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
       /* Mat result = inputFrame.rgba();
        //зеркальное отоброжение входного кадра
        if (isCurrentCameraFront)
            Core.flip(result, result, 1);

        Mat grayFrame = inputFrame.gray();
        //зеркальное отоброжение входного кадра
        if (isCurrentCameraFront)
            Core.flip(grayFrame, grayFrame, 1);
        Imgproc.equalizeHist(grayFrame, grayFrame);

        if (this.absoluteFaceSize == 0) {
            int height = grayFrame.rows();
            if (Math.round(height * 0.2f) > 0) {
                this.absoluteFaceSize = Math.round(height * 0.2f);
            }
        }


        MatOfRect faces = new MatOfRect();
        faceCascadeOne.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());

        /*
        MatOfRect mouths = new MatOfRect();
        faceCascadeTwo.detectMultiScale(grayFrame, mouths, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());
*/
/*
        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++) {
            Rect rect = facesArray[i];
            //Imgproc.rectangle(result, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);
            int circleRadius = rect.height / 2;
            double circleCenterX = rect.x + circleRadius;
            double circleCenterY = rect.y + circleRadius;
            Point circleCenter = new Point(circleCenterX, circleCenterY);
            Imgproc.circle(result, circleCenter, circleRadius, new Scalar(0, 255, 0, 255), 3);

            Size size = new Size(rect.width, rect.height);
            Mat resizedSmile = new Mat();
            Imgproc.resize(smile, resizedSmile, size);
            resizedSmile.copyTo(result.row(rect.y).col(rect.x));
        }

        /*
        Rect[] mouthsArray = mouths.toArray();
        for (int i = 0; i < mouthsArray.length; i++){
            Rect rect = mouthsArray[i];
            //Imgproc.rectangle(result, mouthsArray[i].tl(), mouthsArray[i].br(), new Scalar(255, 128, 0, 255), 3);
            int circleRadius = rect.height / 2;
            double circleCenterX = rect.x + circleRadius;
            double circleCenterY = rect.y + circleRadius;
            Point circleCenter = new Point(circleCenterX, circleCenterY);
            Imgproc.circle(result, circleCenter, circleRadius, new Scalar(255, 128, 0, 255), 3);
        }


*/
        return smile;
  //      return result;
    }

    private CascadeClassifier newCascadeClassifier (String file) {
        try {
            // load cascade file from application resources

            InputStream is = assetManager.open(file);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, file);
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            CascadeClassifier result = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            if (result.empty()) {
                Log.e("SANCV", "Failed to load cascade classifier");
            } else
                Log.i("SANCV", "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

            cascadeDir.delete();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("SANCV", "Failed to load cascade. Exception thrown: " + e);
        }
        return null;
    }

    private Mat openFile(String fileName) throws FileNotFoundException {
        Mat result = Imgcodecs.imread(fileName);
        if (result.dataAddr() == 0) {
            throw new FileNotFoundException("Couldn't open file " + fileName);
        }
        return result;
    }
}
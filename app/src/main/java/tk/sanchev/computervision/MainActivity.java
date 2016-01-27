package tk.sanchev.computervision;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

public class MainActivity extends AppCompatActivity{
    private Mat smile;

    private final String[] inputModes = {
            "CV_8U", "CV_8UC1", "CV_8UC2", "CV_8UC3", "CV_8UC4",
            "CV_8S", "CV_8SC1", "CV_8SC2", "CV_8SC3", "CV_8SC4",
            "CV_16U", "CV_16UC1", "CV_16UC2", "CV_16UC3", "CV_16UC4",
            "CV_16S", "CV_16SC1", "CV_16SC2", "CV_16SC3", "CV_16SC4",
            "CV_32S", "CV_32SC1", "CV_32SC2", "CV_32SC3", "CV_32SC4",
            "CV_32F", "CV_32FC1", "CV_32FC2", "CV_32FC3", "CV_32FC4",
            "CV_64F", "CV_64FC1", "CV_64FC2", "CV_64FC3", "CV_64FC4",
            "CV_USRTYPE1"};

    static {
        System.loadLibrary("opencv_java3");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        ImageView ivOCV1 = ((ImageView) findViewById(R.id.ivOCV1));
        ImageView ivOCV2 = (ImageView) findViewById(R.id.ivOCV2);
        ImageView ivOCV3 = (ImageView) findViewById(R.id.ivOCV3);
        ImageView ivOCV4 = (ImageView) findViewById(R.id.ivOCV4);

        Mat mRgba = null;
        try {
            mRgba = Utils.loadResource(this, R.drawable.emoji, CvType.CV_8UC3);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bitmap bm1 = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mRgba, bm1);
        ivOCV1.setImageBitmap(bm1);

        Bitmap bm2 = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_4444);
        Utils.matToBitmap(mRgba, bm2);
        ivOCV2.setImageBitmap(bm2);

        Bitmap bm3 = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(mRgba, bm3);
        ivOCV3.setImageBitmap(bm3);
    }
}
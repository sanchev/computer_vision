package tk.sanchev.computervision;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity{

    static {
        System.loadLibrary("opencv_java3");
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        ImageView ivEmoji = ((ImageView) findViewById(R.id.ivEmoji));
        ImageView ivResult = ((ImageView) findViewById(R.id.ivResult));
        ImageView ivBackground = ((ImageView) findViewById(R.id.ivBackground));

        Bitmap bitmapEmoji = BitmapFactory.decodeResource(getResources(), R.drawable.emoji2);
        Bitmap bitmapMask = BitmapFactory.decodeResource(getResources(), R.drawable.mask);
        Bitmap bitmapBackground = BitmapFactory.decodeResource(getResources(), R.drawable.background);

        ivEmoji.setImageBitmap(bitmapEmoji);
        ivBackground.setImageBitmap(bitmapBackground);

        Mat matEmoji = new Mat(bitmapEmoji.getWidth(), bitmapEmoji.getHeight(), CvType.CV_8UC3);
        Utils.bitmapToMat(bitmapEmoji, matEmoji);

        Mat matMask = new Mat(bitmapMask.getWidth(), bitmapMask.getHeight(), CvType.CV_8UC3);
        Utils.bitmapToMat(bitmapMask, matMask);

        Mat matBackground = new Mat(bitmapBackground.getWidth(), bitmapBackground.getHeight(), CvType.CV_8UC3);
        Utils.bitmapToMat(bitmapBackground, matBackground);

        Mat matResult = new Mat(matBackground.rows(), matBackground.cols(), matBackground.type());
        matBackground.copyTo(matResult);

        Mat submat = matResult.submat(0, 100, 0, 100);
        Size size = submat.size();
        Mat resizedEmoji = new Mat();
        Imgproc.resize(matEmoji, resizedEmoji, size);
        Mat resizedMask = new Mat();
        Imgproc.resize(matMask, resizedMask, size);
        resizedEmoji.copyTo(submat, resizedMask);

        Bitmap bitmapResult = Bitmap.createBitmap(matResult.cols(), matResult.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(matResult, bitmapResult);

        ivResult.setImageBitmap(bitmapResult);
    }
}
package tk.sanchev.computervision;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class LoadEmoji {

    private Mat matEmoji;
    private Mat matMask;

    public LoadEmoji() {
        Bitmap bitmapEmoji = BitmapFactory.decodeResource(getResources(), R.drawable.emoji);
        Bitmap bitmapMask = BitmapFactory.decodeResource(getResources(), R.drawable.mask);

        matEmoji = new Mat(bitmapEmoji.getWidth(), bitmapEmoji.getHeight(), CvType.CV_8UC3);
        Utils.bitmapToMat(bitmapEmoji, matEmoji);

        matMask = new Mat(bitmapMask.getWidth(), bitmapMask.getHeight(), CvType.CV_8UC3);
        Utils.bitmapToMat(bitmapMask, matMask);

        Mat matBackground = new Mat(bitmapBackground.getWidth(), bitmapBackground.getHeight(), CvType.CV_8UC3);

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

    }
}

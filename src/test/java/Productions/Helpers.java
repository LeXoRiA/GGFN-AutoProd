package Productions;

import io.appium.java_client.android.AndroidDriver;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ShotGum on 17.8.2016.
 * GitHub: LeXoRiA
 */

public abstract class Helpers {
    public static AndroidDriver driver;

    public static URL serverAddress;
    private static WebDriverWait driverWait;
    public static String screenshotsFolder;



    public static void init(AndroidDriver webDriver, URL driverServerAddress) {
        driver = webDriver;
        serverAddress = driverServerAddress;
        int timeoutInSeconds = 60;
        // must wait at least 60 seconds for running on Sauce.
        // waiting for 30 seconds works locally however it fails on Sauce.
        driverWait = new WebDriverWait(webDriver, timeoutInSeconds);
    }



    // STARTS HERE //


    public void log(String msg)
    {
        Date dNow = new Date();
        SimpleDateFormat tmfr = new SimpleDateFormat("kk:mm:ss");
        System.out.println(tmfr.format(dNow) + " - " + msg);
    }

    public void sleep(int seconds) throws Exception
    {
        Thread.sleep(seconds * 1000);
    }

    /* Take screenshot of application while it's running */
    public boolean takeScreenshot(final String ssName, AndroidDriver _driver2)
    {
        /* Keep Appium alive */
        _driver2.getOrientation();


        String screenshotDirectory = System.getProperty("appium.screenshots.dir", System.getProperty("java.io.tmpdir", ""));
        File screenshot = ((TakesScreenshot) _driver2).getScreenshotAs(OutputType.FILE);
        return screenshot.renameTo(new File(screenshotDirectory, String.format("/%s.png", ssName)));
    }

    /* Save image from URL (AWS S3) */
    public void saveImage(String saveImageUrl, String saveImageDest, AndroidDriver _driver2) throws Exception
    {
        String screenshotDirectory = System.getProperty("appium.screenshots.dir", System.getProperty("java.io.tmpdir", ""));

        URL url = new URL(saveImageUrl);
        InputStream is = url.openStream();
        OutputStream os = new FileOutputStream(saveImageDest);

        byte[] b = new byte[2048];
        int length;

        while ((length = is.read(b)) != -1)
        {
            os.write(b, 0, length);
        }
        is.close();
        os.close();

        return;
    }


    /* Resize image to Canny and match */
    public String resizeCanny(String resizeCannyImage, String resizedCanny, String resizeCannyResult, int width, int height, int inter) throws Exception
    {
        String screenshotDirectory = System.getProperty("appium.screenshots.dir", System.getProperty("java.io.tmpdir", ""));

        String resizeCannyImageStr = screenshotDirectory + resizeCannyImage;
        String resizedCannyStr = screenshotDirectory + resizedCanny;
        String resizeCannyResultStr = screenshotDirectory + resizeCannyResult;

        /* Create array for new dimensions and get H and W of image */
        int[] dim = new int[2];
        int hCan = Highgui.imread(resizeCannyImageStr).height();
        int wCan = Highgui.imread(resizeCannyImageStr).width();

        /* Conditions: */
        if (width == 0 && height == 0)
        {
            return resizeCannyImageStr;
        }
        else if (width == 0)
        {
            float r = height / (float) hCan;
            dim[0] = (int) (wCan * r);
            dim[1] = height;
        }
        else
        {
            float r = width / (float) wCan;
            dim[0] = width;
            dim[1] = (int) (hCan * r);
        }

        /* Some matrix conversions */
        Mat resizeCannyImageMat = Highgui.imread(resizeCannyImageStr);
        Mat resizedCannyMat = Highgui.imread(resizedCannyStr);

        /* Get new dimension after conditions to resize image */
        int nW = dim[0];
        int nH = dim[1];

        /* Resize and write the image */
        Size newDim = new Size(nW, nH);
        Imgproc.resize(resizeCannyImageMat, resizedCannyMat, newDim);
        Highgui.imwrite(resizedCannyStr, resizedCannyMat);

        return resizedCannyStr;
    } //end resizeCanny




}


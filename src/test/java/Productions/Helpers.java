package Productions;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidKeyCode;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by ShotGum on 17.8.2016
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

    // EVERYTHING STARTS HERE //
    double selectedScale;

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

        /* Local */
        String screenshotDirectory = "C:/Users/qa1/Desktop/ms_test";

        /* Remote */
//        String screenshotDirectory = System.getProperty("appium.screenshots.dir", System.getProperty("java.io.tmpdir", ""));

        File screenshot = ((TakesScreenshot) _driver2).getScreenshotAs(OutputType.FILE);
        return screenshot.renameTo(new File(screenshotDirectory, String.format("/%s.png", ssName)));
    }

    /* Save image from URL (AWS S3) */
    public void saveImage(String saveImageUrl, String saveImageDest, AndroidDriver _driver2) throws Exception
    {
        /* Local */
        String screenshotDirectory = "C:/Users/qa1/Desktop/ms_test";

        /* Remote */
//        String screenshotDirectory = System.getProperty("appium.screenshots.dir", System.getProperty("java.io.tmpdir", ""));

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
        /* Local */
        String screenshotDirectory = "C:/Users/qa1/Desktop/ms_test";

        /* Remote */
//        String screenshotDirectory = System.getProperty("appium.screenshots.dir", System.getProperty("java.io.tmpdir", ""));

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

    /* Match template and image and then click/swipe */
    public void CannyForSpaceSelection(String cannyTemplate, String cannyImage, String cannyGray, String cannyCannyied, String cannyResized, String cannyResult,
                      String cannyOut, AndroidDriver _driver2) throws Exception
    {
        /* Local */
        String screenshotDirectory = "C:/Users/qa1/Desktop/ms_test";
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        /* Remote */
//        String screenshotDirectory = System.getProperty("appium.screenshots.dir", System.getProperty("java.io.tmpdir", ""));
//        OpenCV.loadShared();

        /*Keep Appium alive*/
        _driver2.getOrientation();

        String cannyTemplateStr = screenshotDirectory + cannyTemplate;
        String cannyImageStr = screenshotDirectory + cannyImage;
        String cannyGrayStr = screenshotDirectory + cannyGray;
        String cannyCannyiedStr = screenshotDirectory + cannyCannyied;
        String cannyResizedStr = screenshotDirectory + cannyResized;
        String cannyResultStr = screenshotDirectory + cannyResult;
        String cannyOutStr = screenshotDirectory + cannyOut;

        /* Variables for linspace */
        double linStart;
        double linEnd;
        double counter;
        double space;

        /* Read template, convert it to gray and Canny it */
        Mat cannyTemplateMat = Highgui.imread(cannyTemplateStr);

        Imgproc.cvtColor(cannyTemplateMat, cannyTemplateMat, Imgproc.COLOR_BGR2GRAY);
        Highgui.imwrite(cannyTemplateStr, cannyTemplateMat);

        Imgproc.Canny(cannyTemplateMat, cannyTemplateMat, 50, 200);
        Highgui.imwrite(cannyTemplateStr, cannyTemplateMat);

        Mat templateMatchMat = Highgui.imread(cannyTemplateStr);

        /* Get Height and Width of template image */
        int tH = Highgui.imread(cannyTemplateStr).height();
        int tW = Highgui.imread(cannyTemplateStr).width();

        /* Start counter */
        int ctr = 0;

        /* Read image and convert it to gray */
        Mat cannyImageMat = Highgui.imread(cannyImageStr);
        Mat imgGryMat = Highgui.imread(cannyImageStr);

        Imgproc.cvtColor(cannyImageMat, imgGryMat, Imgproc.COLOR_BGR2GRAY);
        Highgui.imwrite(cannyGrayStr, imgGryMat);

        /* Some parameters to use in every turn of for loop */
        double[] found = new double[4];
        Point mLoc = null;
        double mVal = 0;
        float r = 0;

        /* Values for linspace and for loop */
        linStart = 1.0;
        linEnd = 0.1;
        counter = 90;
        space = (linStart - linEnd) / counter;

        List<Double> scaleArray = new ArrayList<Double>();

        /* For loop. The mothership of the script */
        for (double scale = linStart; scale >= linEnd; scale = scale - space)
        {
            /* Keep Appium alive */
            _driver2.getOrientation();

            /* Get H and W of grayed image. And multiply the width with scale for multi-scale */
            int gryW = Highgui.imread(cannyGrayStr).width();
            double newWidth = gryW * scale;
            int gryH = Highgui.imread(cannyGrayStr).height();

            /* Change the name for easy use in resizeCanny() function */
            cannyCannyied = cannyGray;

            /* Start resizeCanny function. It resizes the image to Canny and match for later */
            resizeCanny(cannyCannyied, cannyResized, cannyResult, (int) newWidth, gryH, Imgproc.INTER_AREA);

            /* Get H and W of resized image */
            int rszH = Highgui.imread(cannyResizedStr).height();
            int rszW = Highgui.imread(cannyResizedStr).width();

            /* r = (grayed image's width)/(resized image's width) */
            r = gryW / (float) rszW;

            /* If resized image is smaller than template, then break */
            if (rszH < tH || rszW < tW)
            {
                break;
            }

            /* Some matrix conversion */
            Mat cannyResizedMat = Highgui.imread(cannyResizedStr);
            String edged = cannyResizedStr;
            Mat edgedMat = Highgui.imread(edged);

            /* Canny and write the image that has been resized */
            Imgproc.Canny(cannyResizedMat, edgedMat, 50, 200);
            Highgui.imwrite(cannyResultStr, edgedMat);

            /* Some matrix conversions */
            Mat cannyResultMat = Highgui.imread(cannyResultStr);
            String matchResult = cannyResultStr;
            Mat matchResultMat = Highgui.imread(matchResult);

            Mat matchTemplateMat = Highgui.imread(cannyTemplateStr);

            /* Match Canny'd template and Canny'd image */
            Imgproc.matchTemplate(cannyResultMat, matchTemplateMat, matchResultMat, Imgproc.TM_CCOEFF_NORMED); // was templateMatchMat

            /* Get maximum value and maximum location */
            Core.MinMaxLocResult mmrValues = Core.minMaxLoc(matchResultMat);
            mLoc = mmrValues.maxLoc;
            mVal = mmrValues.maxVal;

            /* If found array is empty maximum value is bigger than previous max value, then update the variables */
            if (found == null || mVal > found[0])
            {
                found[0] = mVal;
                found[1] = mLoc.x;
                found[2] = mLoc.y;
                found[3] = (double) r;

                scaleArray.add(scale);

                System.out.println("maxVal (IF): " + mVal);
                System.out.println("scaleArray: " + scaleArray);

            } // end if

            else
            {
                System.out.println("maxVal (ELSE): " + mVal);
            }

//            if (found[0] < 0.40 || found[0] > 1.00)
//            {
//                log("Match not found!");
//                log("Ending the test!");
//                _driver2.quit();
//            }

        } //end for
        selectedScale = Collections.min(scaleArray);
        log("Scaling multiplier selected as: " + selectedScale);

        mLoc.x = found[1];
        mLoc.y = found[2];
        r = (float) found[3];

        /* Found template's edges */
        int startX, startY;
        startX = (int) ((mLoc.x) * r);
        startY = (int) ((mLoc.y) * r);
        int endX, endY;
        endX = (int) ((mLoc.x + tW) * r);
        endY = (int) ((mLoc.y + tH) * r);

        log("startX, startY: " + startX + " : " + startY);

        /*Keep Appium alive*/
        _driver2.getOrientation();

        // Draw rectangle on match.
        Core.rectangle(cannyImageMat, new Point(startX, startY), new Point(endX, endY), new Scalar(0, 0, 255));

        // Write the matched imaged to show if it's true or not.
        log("Writing image as " + cannyOut);
        Highgui.imwrite(cannyOutStr, cannyImageMat);

        return;
    } //end CannyForSpaceSelection

    /* Match template and image and then click/swipe */
    public void Canny(String cannyTemplate, String cannyImage, String cannyGray, String cannyCannyied, String cannyResized, String cannyResult,
                                       String cannyOut, AndroidDriver _driver2) throws Exception
    {
        /* Local */
        String screenshotDirectory = "C:/Users/qa1/Desktop/ms_test";
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        /* Remote */
//        String screenshotDirectory = System.getProperty("appium.screenshots.dir", System.getProperty("java.io.tmpdir", ""));
//        OpenCV.loadShared();

        /*Keep Appium alive*/
        _driver2.getOrientation();

        String cannyTemplateStr = screenshotDirectory + cannyTemplate;
        String cannyImageStr = screenshotDirectory + cannyImage;
        String cannyGrayStr = screenshotDirectory + cannyGray;
        String cannyCannyiedStr = screenshotDirectory + cannyCannyied;
        String cannyResizedStr = screenshotDirectory + cannyResized;
        String cannyResultStr = screenshotDirectory + cannyResult;
        String cannyOutStr = screenshotDirectory + cannyOut;

        /* Variables for linspace */
        double linStart;
        double linEnd;
        double counter;
        double space;

        /* Read template, convert it to gray and Canny it */
        Mat cannyTemplateMat = Highgui.imread(cannyTemplateStr);

        Imgproc.cvtColor(cannyTemplateMat, cannyTemplateMat, Imgproc.COLOR_BGR2GRAY);
        Highgui.imwrite(cannyTemplateStr, cannyTemplateMat);

        Imgproc.Canny(cannyTemplateMat, cannyTemplateMat, 50, 200);
        Highgui.imwrite(cannyTemplateStr, cannyTemplateMat);

        Mat templateMatchMat = Highgui.imread(cannyTemplateStr);

        /* Get Height and Width of template image */
        int tH = Highgui.imread(cannyTemplateStr).height();
        int tW = Highgui.imread(cannyTemplateStr).width();

        /* Start counter */
        int ctr = 0;

        /* Read image and convert it to gray */
        Mat cannyImageMat = Highgui.imread(cannyImageStr);
        Mat imgGryMat = Highgui.imread(cannyImageStr);

        Imgproc.cvtColor(cannyImageMat, imgGryMat, Imgproc.COLOR_BGR2GRAY);
        Highgui.imwrite(cannyGrayStr, imgGryMat);

        /* Some parameters to use in every turn of for loop */
        double[] found = new double[4];
        Point mLoc = null;
        double mVal = 0;
        float r = 0;

        /* Keep Appium alive */
        _driver2.getOrientation();

        /* Get H and W of grayed image. And multiply the width with scale for multi-scale */
        int gryW = Highgui.imread(cannyGrayStr).width();
        double newWidth = gryW * selectedScale;
        int gryH = Highgui.imread(cannyGrayStr).height();

        /* Change the name for easy use in resizeCanny() function */
        cannyCannyied = cannyGray;

        /* Start resizeCanny function. It resizes the image to Canny and match for later */
        resizeCanny(cannyCannyied, cannyResized, cannyResult, (int) newWidth, gryH, Imgproc.INTER_AREA);

        /* Get H and W of resized image */
        int rszH = Highgui.imread(cannyResizedStr).height();
        int rszW = Highgui.imread(cannyResizedStr).width();

        /* r = (grayed image's width)/(resized image's width) */
        r = gryW / (float) rszW;

        /* Some matrix conversion */
        Mat cannyResizedMat = Highgui.imread(cannyResizedStr);
        String edged = cannyResizedStr;
        Mat edgedMat = Highgui.imread(edged);

        /* Canny and write the image that has been resized */
        Imgproc.Canny(cannyResizedMat, edgedMat, 50, 200);
        Highgui.imwrite(cannyResultStr, edgedMat);

        /* Some matrix conversions */
        Mat cannyResultMat = Highgui.imread(cannyResultStr);
        String matchResult = cannyResultStr;
        Mat matchResultMat = Highgui.imread(matchResult);

        Mat matchTemplateMat = Highgui.imread(cannyTemplateStr);

        /* Match Canny'd template and Canny'd image */
        Imgproc.matchTemplate(cannyResultMat, matchTemplateMat, matchResultMat, Imgproc.TM_CCOEFF_NORMED); // was templateMatchMat

        /* Get maximum value and maximum location */
        Core.MinMaxLocResult mmrValues = Core.minMaxLoc(matchResultMat);
        mLoc = mmrValues.maxLoc;
        mVal = mmrValues.maxVal;

        found[0] = mVal;
        found[1] = mLoc.x;
        found[2] = mLoc.y;
        found[3] = (double) r;

        System.out.println("maxVal (selectedScale): " + mVal);

//        if (found[0] < 0.40 || found[0] > 1.00)
//        {
//            log("Match not found!");
//            log("Ending the test!");
//            _driver2.quit();
//        }

        /* After for loop; update maximum location pointers (x,y) with found array to choose/show */
        mLoc.x = found[1];
        mLoc.y = found[2];
        r = (float) found[3];

        /* Found template's edges */
        int startX, startY;
        startX = (int) ((mLoc.x) * r);
        startY = (int) ((mLoc.y) * r);
        int endX, endY;
        endX = (int) ((mLoc.x + tW) * r);
        endY = (int) ((mLoc.y + tH) * r);

        log("startX, startY: " + startX + " : " + startY);

        /*Keep Appium alive*/
        _driver2.getOrientation();

        // Draw rectangle on match.
        Core.rectangle(cannyImageMat, new Point(startX, startY), new Point(endX, endY), new Scalar(0, 0, 255));

        // Write the matched imaged to show if it's true or not.
        log("Writing image as " + cannyOut);
        Highgui.imwrite(cannyOutStr, cannyImageMat);

        if (startX == 0 && startY == 0){
            log("Coordinates: 0,0");
            log("Ending the test!");
            _driver2.quit();
        }

        /* Make your move */
        int tapPointX, tapPointY;
        tapPointX = (startX + endX)/2 ;
        tapPointY = (startY + endY)/2;
        _driver2.tap(1, tapPointX, tapPointY, 250);

        return;
    } //end Canny

    public void actionStations(String fileName, AndroidDriver _driver2) throws Exception
    {
        /* Local */
        String screenshotDirectory = "C:/Users/qa1/Desktop/ms_test";

        /* Remote */
//        String screenshotDirectory = System.getProperty("appium.screenshots.dir", System.getProperty("java.io.tmpdir", ""));

        try
        {
            String jsonFile = screenshotDirectory + "/" + fileName;
            URL link = new URL("https://s3.amazonaws.com/infosfer-ab-test/jsonfiles/" + fileName + ".json");

            /* Download JSON file and read it */
            InputStream in = new BufferedInputStream(link.openStream());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int q = 0;
            while (-1 != (q = in.read(buf)))
            {
                out.write(buf, 0, q);
            }
            out.close();
            in.close();
            byte[] response = out.toByteArray();

            FileOutputStream fos = new FileOutputStream(jsonFile);
            fos.write(response);
            fos.close();
            /* Got JSON */

            log("JSON File has been saved!");

            /* Keep Appium alive */
            _driver2.getOrientation();

            /* Parse JSON file */
            FileReader reader = new FileReader(jsonFile);
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(reader);

            JSONObject jsonObject = (JSONObject) obj;
            JSONArray functionList = (JSONArray) jsonObject.get("Functions");

            /* Get objects from JSON */
            int n = 0;
            while (n < functionList.size())
            {
                JSONObject jObject = (JSONObject) functionList.get(n);

                log("IR action started");

                /* Get necessary variables from JSON */
                String functionName = (String) jObject.get("functionName");
                String ssName = (String) jObject.get("screenshotNameObj");
                String saveImageUrlObj = (String) jObject.get("imageURLObj");
                String saveImageUrl = "http://infosfer-ab-test.s3-website-us-east-1.amazonaws.com/prods/" + (String) jObject.get("imageURLObj") + ".png";
                String saveImageDest = screenshotDirectory + "/" + jObject.get("destinationImageObj") + ".png";
                String cannyTemplate = "/" + (String) jObject.get("templateNameObj") + ".png";
                String cannyImage = "/" + (String) jObject.get("sourceNameObj") + ".png";
                String cannyGray = "/" + (String) jObject.get("grayedSourceObj") + ".png";
                String cannyCannyied = "/" + (String) jObject.get("cannySourceObj") + ".png";
                String cannyResized = "/" + (String) jObject.get("resizedCannyObj") + ".png";
                String cannyResult = "/" + (String) jObject.get("cannyResultObj") + ".png";
                String cannyOut = "/" + (String) jObject.get("outImageObj") + ".png";
                long seconds = (Long) jObject.get("sleepTimeObj");
                int second = (int) seconds;

                /* Ignore native dialog */
//                _driver2.switchTo().alert().dismiss();

                if (functionName.equalsIgnoreCase("selectscale"))
                {
                    takeScreenshot(ssName, _driver2);
                    log("Screenshot captured");
                    saveImage(saveImageUrl, saveImageDest, _driver2);
                    log("Template has been saved from server");
                    CannyForSpaceSelection(cannyTemplate, cannyImage, cannyGray, cannyCannyied, cannyResized, cannyResult, cannyOut, _driver2);
                    log("Scale selected!");
                    log("n value: " + n);
                    log("Action done (IR)");
                    second = second + 1;
                    sleep(second);
                    takeScreenshot("Step" + n, _driver2);
                    n++;
                } //end if

                else
                {

                    if (functionName.equalsIgnoreCase("googleplaylogin"))
                    {
                        _driver2.getOrientation();
                        sleep(5);
                        takeScreenshot("login_step1", _driver2);
                        _driver2.findElement(By.id("com.google.android.gsf.login:id/next_button")).click();
                        sleep(3);

                        takeScreenshot("login_step2", _driver2);
                        _driver2.findElement(By.id("com.google.android.gsf.login:id/username_edit")).sendKeys("ekrem.erol@infosfer.com");
                        sleep(3);

                        takeScreenshot("login_step3", _driver2);
                        _driver2.findElement(By.id("com.google.android.gsf.login:id/password_edit")).sendKeys("pinXmoQ12");
                        sleep(3);

                        takeScreenshot("login_step4", _driver2);
                        _driver2.findElement(By.id("com.google.android.gsf.login:id/next_button")).click();
                        sleep(3);

                        takeScreenshot("login_step5", _driver2);
                        _driver2.findElement(By.id("android:id/button1")).click();
                        sleep(5);

                        takeScreenshot("login_step6", _driver2);
                        _driver2.findElement(By.id("com.google.android.gsf.login:id/agree_backup")).click();
                        sleep(3);

                        takeScreenshot("login_step7", _driver2);
                        _driver2.findElement(By.id("com.google.android.gsf.login:id/next_button")).click();
                        sleep(15);
                        _driver2.getOrientation();
                        sleep(15);
                        _driver2.getOrientation();
                        sleep(15);
                        _driver2.getOrientation();
                        sleep(15);
                        _driver2.getOrientation();
                        sleep(15);
                        _driver2.getOrientation();
                        sleep(15);
                        _driver2.getOrientation();

                        takeScreenshot("LogInsuccess!", _driver2);

                        _driver2.pressKeyCode(AndroidKeyCode.BACK);
                        log("Ad Closed");
                        takeScreenshot("AdClosed", _driver2);
                        sleep(5);

                        n++;

                    } //end if

                    else
                    {
                        takeScreenshot(ssName, _driver2);
                        log("Screenshot captured");
                        saveImage(saveImageUrl, saveImageDest, _driver2);
                        log("Template has been saved from server");
                        Canny(cannyTemplate, cannyImage, cannyGray, cannyCannyied, cannyResized, cannyResult, cannyOut, _driver2);
                        log("n value: " + n);
                        log("Action done (IR)");
                        second = second + 1;
                        sleep(second);
                        takeScreenshot("Step" + n, _driver2);
                        n++;
                    } //end else
                } //end else
            } //end while
        } //end try
        catch (Exception e)
        {
            e.printStackTrace();
        }
    } //end actionStation
} //end class


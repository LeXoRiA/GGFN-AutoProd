package Productions;

import io.appium.java_client.android.AndroidDriver;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;

/**
 * Created by ShotGum on 17.8.2016.
 * GitHub: LeXoRiA
 */
public class Test extends Helpers
{
    private AndroidDriver driver;

    @Before
    public void setUp() throws Exception
    {
        /* Local */

        DesiredCapabilities capabilities = new DesiredCapabilities();

        capabilities.setCapability("appium-version", "1.4.13");
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("deviceName", "Android Emulator");
        capabilities.setCapability("platformVersion", "4.1");
        capabilities.setCapability("appPackage", "com.mightygamesgroup.shootyskies");
//        capabilities.setCapability("appPackage", "com.boombitgames.SuperheroPoliceman");
//        capabilities.setCapability("appActivity", "com.prime31.UnityPlayerNativeActivity");
        capabilities.setCapability("appActivity", "com.unity3d.player.UnityPlayerNativeActivity");
        capabilities.setCapability("autoDismissAlerts", true);

        driver = new AndroidDriver(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);

        /* Remote */
//        driver = new AndroidDriver(new URL("http://127.0.0.1:4723/wd/hub"), new DesiredCapabilities());
    }

    @org.junit.Test
    public void Test() throws Exception
    {
        log("Test script started");
        log("Launching application...");
        sleep(15); // Wait while application is launching
        driver.getOrientation();
        log("Application is ready!");

        /* Local */
        String screenshotDirectory = "C:/Users/qa1/Desktop/ms_test";

        /* Remote */
//        String screenshotDirectory = System.getProperty("appium.screenshots.dir", System.getProperty("java.io.tmpdir", ""));

        /* Flasgship */
        actionStations("LanguageText", driver);

        /* Take screenshot to see the last situation */
        takeScreenshot("last", driver);
    }

    @After
    public void tearDown () throws Exception
    {
        log("Closing application");
        driver.quit();
        log("Application closed");
        log("Test done!");
    }
}

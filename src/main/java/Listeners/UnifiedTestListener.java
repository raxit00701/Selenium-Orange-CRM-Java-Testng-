package Listeners;

import java.nio.file.Files;
import Base.BaseTest;
import io.qameta.allure.Allure;
import org.monte.media.Format;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;
import org.openqa.selenium.*;
import org.testng.*;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.monte.media.FormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;

public class UnifiedTestListener implements ITestListener, ISuiteListener {

    private static final String BASE_PATH =
            "C:\\Users\\raxit\\IdeaProjects\\Orange crm\\target";

    private static volatile boolean envWritten = false;

    private ThreadLocal<ScreenRecorder> screenRecorder = new ThreadLocal<>();

    private static final Set<String> browsersUsed =
            ConcurrentHashMap.newKeySet();

    private static final Map<String, Integer> browserTestCount =
            new ConcurrentHashMap<>();

    private static final Map<String, String> testBrowserMap =
            new ConcurrentHashMap<>();

    /* ================= SUITE LEVEL ================= */

    @Override
    public void onStart(ISuite suite) {
        System.out.println("\n" + repeat("=", 80));
        System.out.println("TEST SUITE STARTED: " + suite.getName());

        if (suite.getXmlSuite() != null) {
            System.out.println("Parallel Mode: " + suite.getXmlSuite().getParallel());
            System.out.println("Thread Count : " + suite.getXmlSuite().getThreadCount());
        }

        System.out.println(repeat("=", 80));

        browsersUsed.clear();
        browserTestCount.clear();
        testBrowserMap.clear();
        envWritten = false;
    }

    @Override
    public void onFinish(ISuite suite) {
        System.out.println("\n" + repeat("=", 80));
        System.out.println("TEST SUITE FINISHED: " + suite.getName());
        System.out.println(repeat("=", 80));

        writeAllureEnvironment();
        printExecutionSummary();

        System.out.println(repeat("=", 80) + "\n");
    }

    /* ================= TEST START ================= */

    @Override
    public void onTestStart(ITestResult result) {

        String testName = result.getMethod().getMethodName();
        String threadName = Thread.currentThread().getName();
        String browser = getBrowserName(result);

        if (browser != null) {
            browsersUsed.add(browser.toUpperCase());
            testBrowserMap.put(testName, browser);
            browserTestCount.merge(browser, 1, Integer::sum);
        }

        System.out.println("\n" + repeat("=", 80));
        System.out.println("TEST STARTED : " + testName);
        System.out.println("Browser      : " + browser);
        System.out.println("Thread       : " + threadName);
        System.out.println(repeat("=", 80));

        addTestEnvironmentToAllure(testName, browser, threadName);
        startVideoRecording(testName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = result.getMethod().getMethodName();

        System.out.println("✅ TEST PASSED: " + testName);

        stopVideoRecording(false);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String browser = getBrowserName(result);
        WebDriver driver = getDriver(result);

        System.out.println("\n" + repeat("=", 80));
        System.out.println("❌ TEST FAILED: " + testName);
        System.out.println("Browser: " + browser);
        System.out.println(repeat("=", 80));

        // Add small delay to ensure page is stable
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // CAPTURE SCREENSHOT
        captureAndAttachScreenshot(driver, testName, browser);

        // CAPTURE LOGS
        try {
            String logContent = generateLogContent(testName, browser, result.getThrowable());

            if (logContent != null && !logContent.isEmpty()) {
                saveLogToDisk(testName, browser, logContent);
                attachLogToAllure(logContent);
                System.out.println("📋 Logs captured and attached");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error capturing logs: " + e.getMessage());
            e.printStackTrace();
        }

        stopVideoRecording(true);

        System.out.println(repeat("=", 80) + "\n");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        System.out.println("⏭️ TEST SKIPPED: " + testName);

        stopVideoRecording(false);
    }

    /* ================= DRIVER RETRIEVAL - ENHANCED ================= */

    /**
     * ENHANCED: Try multiple methods to get the driver
     */
    private WebDriver getDriver(ITestResult result) {
        Object testInstance = result.getInstance();

        System.out.println("🔍 Attempting to get driver from: " + testInstance.getClass().getName());

        // Method 1: Instance is BaseTest (recommended)
        if (testInstance instanceof BaseTest) {
            try {
                WebDriver driver = ((BaseTest) testInstance).getDriver();
                if (driver != null) {
                    System.out.println("✅ Driver retrieved from BaseTest: " + driver.getClass().getSimpleName());
                    return driver;
                }
            } catch (Exception e) {
                System.err.println("⚠️ Failed to get driver from BaseTest: " + e.getMessage());
            }
        } else {
            System.err.println("⚠️ Test instance is NOT BaseTest: " + testInstance.getClass().getName());
            System.err.println("   → YOUR TEST CLASS MUST EXTEND BaseTest!");
        }

        // Method 2: Look for 'driver' field using reflection
        try {
            System.out.println("🔄 Attempting reflection to find 'driver' field...");

            // Try direct field
            Field driverField = findDriverField(testInstance.getClass());
            if (driverField != null) {
                driverField.setAccessible(true);
                Object driverObj = driverField.get(testInstance);

                if (driverObj instanceof WebDriver) {
                    System.out.println("✅ Driver found via reflection: " + driverObj.getClass().getSimpleName());
                    return (WebDriver) driverObj;
                } else if (driverObj instanceof ThreadLocal) {
                    ThreadLocal<?> threadLocal = (ThreadLocal<?>) driverObj;
                    Object driver = threadLocal.get();
                    if (driver instanceof WebDriver) {
                        System.out.println("✅ Driver found via ThreadLocal reflection");
                        return (WebDriver) driver;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Reflection failed: " + e.getMessage());
        }

        // Method 3: Look for getDriver() method
        try {
            System.out.println("🔄 Attempting to find getDriver() method...");
            Method getDriverMethod = testInstance.getClass().getMethod("getDriver");
            Object driver = getDriverMethod.invoke(testInstance);

            if (driver instanceof WebDriver) {
                System.out.println("✅ Driver found via getDriver() method");
                return (WebDriver) driver;
            }
        } catch (Exception e) {
            System.err.println("⚠️ getDriver() method not found or failed: " + e.getMessage());
        }

        System.err.println("❌ ALL METHODS FAILED - Could not retrieve driver");
        System.err.println("   → Make sure your test class extends BaseTest");
        System.err.println("   → Verify driver is initialized in @BeforeMethod");

        return null;
    }

    /**
     * Recursively search for 'driver' field in class hierarchy
     */
    private Field findDriverField(Class<?> clazz) {
        if (clazz == null || clazz == Object.class) {
            return null;
        }

        try {
            return clazz.getDeclaredField("driver");
        } catch (NoSuchFieldException e) {
            // Try superclass
            return findDriverField(clazz.getSuperclass());
        }
    }

    /**
     * SINGLE SOURCE OF TRUTH for browser name
     */
    private String getBrowserName(ITestResult result) {
        try {
            String xmlBrowser = result.getTestContext()
                    .getCurrentXmlTest()
                    .getParameter("browser");

            if (xmlBrowser != null && !xmlBrowser.isEmpty()) {
                return xmlBrowser.toLowerCase();
            }
        } catch (Exception ignored) {}

        String systemBrowser = System.getProperty("browser");
        if (systemBrowser != null && !systemBrowser.isEmpty()) {
            return systemBrowser.toLowerCase();
        }

        return "chrome";
    }

    /* ================= SCREENSHOT - ENHANCED ================= */

    private void captureAndAttachScreenshot(WebDriver driver, String testName, String browser) {
        System.out.println("\n📸 Starting screenshot capture...");

        if (driver == null) {
            System.err.println("❌ SCREENSHOT FAILED: Driver is NULL");
            System.err.println("   ❗ YOUR TEST CLASS MUST EXTEND BaseTest!");
            System.err.println("   ❗ Example: public class SearchPageTest extends BaseTest { ... }");
            return;
        }

        System.out.println("✅ Driver is available: " + driver.getClass().getSimpleName());

        byte[] screenshotBytes = null;
        boolean captureSuccess = false;

        // Method 1: Direct TakesScreenshot
        try {
            System.out.println("🔄 Attempting Method 1: Direct TakesScreenshot cast...");

            if (driver instanceof TakesScreenshot) {
                screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                captureSuccess = true;
                System.out.println("✅ Method 1 SUCCESS - Screenshot captured (" + screenshotBytes.length + " bytes)");
            } else {
                System.err.println("⚠️ Driver does not implement TakesScreenshot interface");
            }
        } catch (Exception e) {
            System.err.println("❌ Method 1 FAILED: " + e.getMessage());
        }

        // Method 2: Try Augmenter (for RemoteWebDriver)
        if (!captureSuccess) {
            try {
                System.out.println("🔄 Attempting Method 2: Using Augmenter...");

                WebDriver augmentedDriver = new org.openqa.selenium.remote.Augmenter().augment(driver);
                screenshotBytes = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.BYTES);
                captureSuccess = true;
                System.out.println("✅ Method 2 SUCCESS - Screenshot captured with Augmenter");

            } catch (Exception e) {
                System.err.println("❌ Method 2 FAILED: " + e.getMessage());
            }
        }

        // Method 3: OutputType.FILE fallback
        if (!captureSuccess) {
            try {
                System.out.println("🔄 Attempting Method 3: OutputType.FILE...");

                File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                screenshotBytes = Files.readAllBytes(srcFile.toPath());
                captureSuccess = true;
                System.out.println("✅ Method 3 SUCCESS - Screenshot from FILE");

            } catch (Exception e) {
                System.err.println("❌ Method 3 FAILED: " + e.getMessage());
            }
        }

        if (captureSuccess && screenshotBytes != null && screenshotBytes.length > 0) {
            System.out.println("✅ Screenshot captured successfully (" + screenshotBytes.length + " bytes)");

            try {
                saveScreenshotToDisk(testName, browser, screenshotBytes);
            } catch (Exception e) {
                System.err.println("⚠️ Failed to save screenshot to disk: " + e.getMessage());
            }

            attachScreenshotToAllure(screenshotBytes, browser, testName);

        } else {
            System.err.println("❌ SCREENSHOT CAPTURE COMPLETELY FAILED");
        }
    }

    private void attachScreenshotToAllure(byte[] screenshot, String browser, String testName) {
        if (screenshot == null || screenshot.length == 0) {
            System.err.println("❌ Cannot attach NULL or empty screenshot");
            return;
        }

        boolean attachSuccess = false;

        try {
            System.out.println("🔄 Attaching to Allure (Method 1: InputStream)...");

            Allure.addAttachment(
                    "Screenshot - " + testName + " [" + browser.toUpperCase() + "]",
                    "image/png",
                    new ByteArrayInputStream(screenshot),
                    ".png"
            );

            attachSuccess = true;
            System.out.println("✅ Screenshot attached to Allure via InputStream");

        } catch (Exception e) {
            System.err.println("❌ Allure attachment (Method 1) failed: " + e.getMessage());
        }

        if (!attachSuccess) {
            try {
                System.out.println("🔄 Attaching to Allure (Method 2: byte array)...");

                Allure.getLifecycle().addAttachment(
                        "Screenshot - " + testName,
                        "image/png",
                        "png",
                        screenshot
                );

                System.out.println("✅ Screenshot attached to Allure via byte array");

            } catch (Exception e) {
                System.err.println("❌ Allure attachment (Method 2) failed: " + e.getMessage());
            }
        }
    }

    private void saveScreenshotToDisk(String testName, String browser, byte[] screenshot) {
        try {
            if (screenshot == null || screenshot.length == 0) {
                System.err.println("❌ Cannot save NULL or empty screenshot to disk");
                return;
            }

            File dir = new File(BASE_PATH + "/screenshots/" + browser);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                System.out.println("📁 Created screenshot directory: " + created);
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
            File file = new File(dir, testName + "_" + timestamp + ".png");

            Files.write(file.toPath(), screenshot);

            System.out.println("💾 Screenshot saved to disk: " + file.getAbsolutePath());
            System.out.println("   → File size: " + file.length() + " bytes");

        } catch (Exception e) {
            System.err.println("❌ Failed to save screenshot to disk: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /* ================= LOGS ================= */

    private String generateLogContent(String testName, String browser, Throwable error) {
        StringBuilder sb = new StringBuilder();

        sb.append("=".repeat(80)).append("\n");
        sb.append("TEST FAILURE LOG\n");
        sb.append("=".repeat(80)).append("\n");
        sb.append("Test Name  : ").append(testName).append("\n");
        sb.append("Browser    : ").append(browser).append("\n");
        sb.append("Thread     : ").append(Thread.currentThread().getName()).append("\n");
        sb.append("Timestamp  : ").append(new Date()).append("\n");
        sb.append("=".repeat(80)).append("\n\n");

        if (error != null) {
            sb.append("ERROR MESSAGE:\n");
            sb.append(error.getMessage()).append("\n\n");

            sb.append("STACK TRACE:\n");
            StringWriter sw = new StringWriter();
            error.printStackTrace(new PrintWriter(sw));
            sb.append(sw.toString());
        }

        return sb.toString();
    }

    private void attachLogToAllure(String logContent) {
        try {
            Allure.addAttachment(
                    "Failure Log",
                    "text/plain",
                    new ByteArrayInputStream(logContent.getBytes(StandardCharsets.UTF_8)),
                    ".txt"
            );
        } catch (Exception e) {
            System.err.println("⚠️ Failed to attach log to Allure: " + e.getMessage());
        }
    }

    private void saveLogToDisk(String testName, String browser, String logContent) {
        try {
            File dir = new File(BASE_PATH + "/logs/" + browser);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File file = new File(dir, testName + "_" + timestamp + ".log");

            Files.write(file.toPath(), logContent.getBytes(StandardCharsets.UTF_8));

            System.out.println("💾 Log saved: " + file.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("⚠️ Failed to save log to disk: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /* ================= VIDEO ================= */

    private void startVideoRecording(String testName) {
        try {
            File videoDir = new File(BASE_PATH + "/videos");
            if (!videoDir.exists()) {
                videoDir.mkdirs();
            }

            GraphicsConfiguration gc =
                    GraphicsEnvironment.getLocalGraphicsEnvironment()
                            .getDefaultScreenDevice()
                            .getDefaultConfiguration();

            ScreenRecorder recorder = new ScreenRecorder(
                    gc,
                    gc.getBounds(),
                    new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_AVI),
                    new Format(MediaTypeKey, MediaType.VIDEO,
                            EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                            CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                            DepthKey, 24,
                            FrameRateKey, Rational.valueOf(15),
                            QualityKey, 1.0f,
                            KeyFrameIntervalKey, 15 * 60),
                    new Format(MediaTypeKey, MediaType.VIDEO,
                            EncodingKey, "black",
                            FrameRateKey, Rational.valueOf(30)),
                    null,
                    videoDir
            );

            screenRecorder.set(recorder);
            recorder.start();

            System.out.println("🎥 Video recording started");

        } catch (Exception e) {
            System.err.println("⚠️ Failed to start video recording: " + e.getMessage());
        }
    }

    private void stopVideoRecording(boolean keep) {
        try {
            ScreenRecorder recorder = screenRecorder.get();
            if (recorder != null) {
                recorder.stop();

                System.out.println("🎥 Video recording stopped");

                java.util.List<File> movieFiles = recorder.getCreatedMovieFiles();

                if (movieFiles != null && !movieFiles.isEmpty()) {
                    for (File video : movieFiles) {
                        if (video.exists()) {
                            if (keep) {
                                try (FileInputStream fis = new FileInputStream(video)) {
                                    byte[] videoBytes = fis.readAllBytes();

                                    Allure.addAttachment(
                                            "Test Execution Video",
                                            "video/avi",
                                            new ByteArrayInputStream(videoBytes),
                                            ".avi"
                                    );

                                    System.out.println("🎬 Video attached to Allure: " + video.getName());
                                }

                                System.out.println("💾 Video saved: " + video.getAbsolutePath());

                            } else {
                                if (video.delete()) {
                                    System.out.println("🗑️ Video deleted (test passed)");
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error while stopping video recording: " + e.getMessage());
            e.printStackTrace();
        } finally {
            screenRecorder.remove();
        }
    }

    /* ================= ALLURE ENV ================= */

    private void addTestEnvironmentToAllure(String testName, String browser, String threadName) {
        try {
            String envInfo = String.format(
                    "Test Name : %s\n" +
                            "Browser   : %s\n" +
                            "Thread    : %s\n" +
                            "Start Time: %s\n",
                    testName,
                    browser != null ? browser.toUpperCase() : "N/A",
                    threadName,
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
            );

            Allure.addAttachment("Test Environment", "text/plain", envInfo);
        } catch (Exception e) {
            System.err.println("⚠️ Failed to attach test environment info: " + e.getMessage());
        }
    }

    private synchronized void writeAllureEnvironment() {
        if (envWritten) {
            System.out.println("ℹ️ Environment properties already written");
            return;
        }

        try {
            Properties props = new Properties();

            props.setProperty("Browsers", String.join(", ", browsersUsed));
            props.setProperty("Environment", System.getProperty("env", "test").toUpperCase());
            props.setProperty("OS", System.getProperty("os.name"));
            props.setProperty("Java Version", System.getProperty("java.version"));
            props.setProperty("Execution Time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

            File allureResultsDir = new File("allure-results");
            if (!allureResultsDir.exists()) {
                allureResultsDir.mkdirs();
                System.out.println("📁 Created allure-results directory");
            }

            File envFile = new File(allureResultsDir, "environment.properties");

            try (FileOutputStream fos = new FileOutputStream(envFile)) {
                props.store(fos, "Allure Environment Configuration");
            }

            envWritten = true;

            System.out.println("✅ Environment properties written to: " + envFile.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("⚠️ Failed to write environment properties: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /* ================= SUMMARY ================= */

    private void printExecutionSummary() {
        System.out.println("\n📊 EXECUTION SUMMARY");
        System.out.println(repeat("-", 80));

        if (browsersUsed.isEmpty()) {
            System.out.println("No browsers recorded");
        } else {
            System.out.println("Browsers Used:");
            browsersUsed.forEach(b -> {
                int count = browserTestCount.getOrDefault(b.toLowerCase(), 0);
                System.out.println(String.format(" - %-10s : %d test(s)", b, count));
            });
        }

        System.out.println(repeat("-", 80));
    }

    /* ================= UTILS ================= */

    private String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) sb.append(str);
        return sb.toString();
    }
}
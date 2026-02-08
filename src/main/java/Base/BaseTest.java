package Base;

import Env.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Optional;


import java.time.Duration;

public class BaseTest {

    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    protected EnvConfig env;


    public WebDriver getDriver() {
        return driverThreadLocal.get();
    }

    @Parameters({
            "browser",
            "env",
            "headless",
            "incognito",
            "window"
    })
    @BeforeMethod(alwaysRun = true)
    public void setUp(
            @Optional("chrome") String browser,
            @Optional("test") String envName,
            @Optional("false") String headless,
            @Optional("false") String incognito,
            @Optional("max") String window
    ) {

        // ===== Override TestNG params with Maven (-D) if present =====
        browser   = System.getProperty("browser", browser);
        envName   = System.getProperty("env", envName);
        headless  = System.getProperty("headless", headless);
        incognito = System.getProperty("incognito", incognito);
        window    = System.getProperty("window", window);

        // ================= ENV Selection =================
        if (envName.equalsIgnoreCase("preprod")) {
            env = new PreProdEnv();
        } else if (envName.equalsIgnoreCase("prod")) {
            env = new ProdEnv();
        } else {
            env = new TestEnv(); // default
        }

        WebDriver driver;

        // ================= Browser Selection (NO exe download) =================
        switch (browser.toLowerCase()) {

            case "firefox":
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (Boolean.parseBoolean(headless)) {
                    firefoxOptions.addArguments("--headless");
                }
                driver = new FirefoxDriver(firefoxOptions);
                break;

            case "edge":
                EdgeOptions edgeOptions = new EdgeOptions();
                if (Boolean.parseBoolean(headless)) {
                    edgeOptions.addArguments("--headless=new");
                }
                driver = new EdgeDriver(edgeOptions);
                break;

            default:
                ChromeOptions chromeOptions = new ChromeOptions();
                if (Boolean.parseBoolean(headless)) {
                    chromeOptions.addArguments("--headless=new");
                }
                if (Boolean.parseBoolean(incognito)) {
                    chromeOptions.addArguments("--incognito");
                }
                driver = new ChromeDriver(chromeOptions);
        }

        driverThreadLocal.set(driver);

        // ================= Window Handling =================
        if ("max".equalsIgnoreCase(window)) {
            getDriver().manage().window().maximize();
        } else if ("min".equalsIgnoreCase(window)) {
            getDriver().manage().window().minimize();
        }

        getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        getDriver().manage().timeouts().pageLoadTimeout(Duration.ofSeconds(100));

        // ================= Launch App =================
        getDriver().get(env.getBaseUrl());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (getDriver() != null) {
            getDriver().quit();
            driverThreadLocal.remove();
        }
    }
}

package Base;

import Utils.ApiClient;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import java.time.Duration;
public abstract class API_TEST {

    protected WebDriver driver;
    protected static ApiClient apiClient;

    @BeforeClass(alwaysRun = true)
    public void globalSetup() throws Exception {
        if (apiClient == null) {
            apiClient = new ApiClient();
        }

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        // ⏳ IMPLICIT WAIT (applies to all findElement calls)
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
        // 🔐 Login via API + inject session cookie
        apiClient.injectSessionCookie(driver);
    }

    @AfterClass(alwaysRun = true)
    public void globalTeardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}

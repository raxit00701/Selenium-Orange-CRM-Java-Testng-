package Tests;

import Base.BaseTest;
import pages.LoginPage;
import Utils.CsvUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.time.Duration;
public class LoginPageTest extends BaseTest {

    @DataProvider(name = "loginData")
    public Object[][] getLoginData() {
        Object[][] data = CsvUtils.readCsv("src/test/resources/login.csv");
        System.out.println("Total rows loaded: " + data.length);
        return data;
    }

    @Test(dataProvider = "loginData",groups = {"STABLE"})
    public void loginWithValidCredentials(String username, String password) throws InterruptedException {
        getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(15));


        LoginPage loginPage = new LoginPage(getDriver());

        // Perform login
        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
        loginPage.clickLogin();

        Thread.sleep(2000); // defensive wait

        // Assertions
        if (loginPage.isUsernameErrorVisible()) {
            System.out.println("Login failed: Username required ✅");
            return;
        }

        if (loginPage.isPasswordErrorVisible()) {
            System.out.println("Login failed: Password required ✅");
            return;
        }

        if (loginPage.isInvalidCredsVisible()) {
            System.out.println("Login failed: Invalid credentials ✅");
            return;
        }

        if (loginPage.isDashboardVisible()) {
            System.out.println("Login successful ✅");
            return;
        }

        System.out.println("Unexpected state ❌");
    }
}
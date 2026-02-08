import Base.API_BASE;
import Utils.CsvUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.openqa.selenium.JavascriptExecutor;

import java.time.Duration;

import static org.testng.Assert.assertTrue;

public class AdminPageTest extends API_BASE {

    // Simple pause helper
    private void pause(int millis) throws InterruptedException {
        Thread.sleep(millis);
    }

    // DataProvider to load values from admin.csv
    @DataProvider(name = "adminData")
    public Object[][] getAdminData() {
        Object[][] data = CsvUtils.readCsv("src/test/resources/admin.csv");

        if (data == null || data.length == 0) {
            throw new RuntimeException("admin.csv missing or empty in Jenkins workspace");
        }
        return data;
    }

    @Test(dataProvider = "adminData", groups = {"REG"})
    public void Adminpage(String employeeName, String username, String password) throws InterruptedException {
        // 0. Apply implicit wait
        getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        pause(2000);



        // 1. Click on main menu item
        WebElement menuItem = getDriver().findElement(By.xpath("//a[contains(@class, 'oxd-main-menu-item') and .//span[text()='AdminPageTest']]"));
        assertTrue(menuItem.isDisplayed(), "Menu item should be visible");
        menuItem.click();
        pause(2000);

        // 2. Click on User Management tab
        WebElement userMgmt = getDriver().findElement(By.xpath("//span[contains(@class,'oxd-topbar-body-nav-tab-item') and normalize-space()='User Management']"));
        assertTrue(userMgmt.isDisplayed(), "User Management tab should be visible");
        userMgmt.click();
        pause(2000);

        // 3. Wait until link is displayed
        WebElement navLink = getDriver().findElement(By.cssSelector("a.oxd-topbar-body-nav-tab-link"));
        assertTrue(navLink.isDisplayed(), "Nav link should be visible");
        pause(2000);

        // 4. Click Add button
        WebElement addBtn = getDriver().findElement(By.xpath("//button[contains(@class,'oxd-button') and normalize-space()='Add']"));
        assertTrue(addBtn.isDisplayed(), "Add button should be visible");
        addBtn.click();
        pause(2000);

        // 5. Select AdminPageTest role
        // Selenium Java example
        WebElement dropdown = getDriver().findElement(By.xpath("//div[contains(@class,'oxd-select-text-input') and text()='-- Select --']"));  // or better locator below
        dropdown.click();

// Tiny wait - dropdown usually appears in <300ms
        Thread.sleep(300);   // or use WebDriverWait for visibility of option

// Now select the desired option by exact text
        WebElement option = getDriver().findElement(By.xpath("//div[@role='listbox']//span[text()='AdminPageTest']"));
        option.click();

        // 6. Enter employee name
        WebElement empInput = getDriver().findElement(By.cssSelector("input[placeholder='Type for hints...']"));
        assertTrue(empInput.isDisplayed(), "Employee input should be visible");
        empInput.sendKeys(employeeName);
        pause(8000);

        // After entering username
        // After entering username
        JavascriptExecutor js = (JavascriptExecutor) getDriver();

// First, get the element at those coordinates
        WebElement target = (WebElement) js.executeScript(
                "return document.elementFromPoint(arguments[0], arguments[1]);", 960, 383
        );
        System.out.println("Hard click target: " + target.getTagName() + " | text: " + target.getText());
        js.executeScript("arguments[0].click();", target);

// Now perform the hard click
        js.executeScript("arguments[0].click();", target);
        pause(2000);

// Verify expected outcome (example: a popup or new field appears)
        String clickedText = (String) js.executeScript(
                "return document.elementFromPoint(arguments[0], arguments[1]).innerText;", 955, 220
        );
        System.out.println("Clicked element text: " + clickedText);




        // 7. Select Enabled status
        // Selenium Java example
        WebElement dropdown2 = getDriver().findElement(By.xpath("//label[normalize-space(.) = 'Status']/following::div[contains(@class, 'oxd-select-wrapper')][1]"));  // or better locator below
        dropdown2.click();

// Tiny wait - dropdown usually appears in <300ms
        //Thread.sleep(300);   // or use WebDriverWait for visibility of option

// Now select the desired option by exact text
        WebElement option2 = getDriver().findElement(By.xpath("//div[@role='listbox']//div[contains(@class, 'oxd-select-option')]//span[text()='Enabled']"));
        option2.click();

        // 8. Enter username
        WebElement usernameInput = getDriver().findElement(By.xpath("//label[normalize-space(.)='Username']/following::input[contains(@class,'oxd-input')][1]"));
        assertTrue(usernameInput.isDisplayed(), "Username input should be visible");
        usernameInput.sendKeys(username);
        pause(2000);



        // 9. Enter password
        WebElement passwordInput = getDriver().findElement(By.cssSelector("div > input.oxd-input[type='password']"));
        assertTrue(passwordInput.isDisplayed(), "Password input should be visible");
        passwordInput.sendKeys(password);
        pause(2000);

        // 10. Enter confirm password
        WebElement confirmPasswordInput = getDriver().findElement(By.cssSelector("div > input.oxd-input.oxd-input--active[type='password']"));
        assertTrue(confirmPasswordInput.isDisplayed(), "Confirm password input should be visible");
        confirmPasswordInput.sendKeys(password);
        pause(2000);

        // 11. Submit form
        WebElement submitBtn = getDriver().findElement(By.cssSelector("button[type='submit'].oxd-button--secondary"));
        assertTrue(submitBtn.isDisplayed(), "Submit button should be visible");
        submitBtn.click();
        pause(2000);
    }
}
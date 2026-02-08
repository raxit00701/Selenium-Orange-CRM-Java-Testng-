package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class LoginPage {
    private WebDriver driver;

    // Locators
    private By usernameField = By.cssSelector("input[placeholder='Username']");
    private By passwordField = By.cssSelector("input[placeholder='Password']");
    private By loginButton   = By.cssSelector("button[type='submit']");

    private By usernameError = By.cssSelector("span.oxd-input-field-error-message:nth-of-type(1)");
    private By passwordError = By.cssSelector("span.oxd-input-field-error-message");
    private By invalidCreds  = By.cssSelector("p.oxd-text.oxd-alert-content-text");
    private By dashboard     = By.cssSelector("h6.oxd-topbar-header-breadcrumb-module");

    // Constructor
    public LoginPage(WebDriver driver) {
        this.driver = driver;
    }

    // Actions
    public void enterUsername(String username) {
        WebElement userInput = driver.findElement(usernameField);
        userInput.click();
        userInput.clear();
        userInput.sendKeys(username);
    }

    public void enterPassword(String password) {
        WebElement passInput = driver.findElement(passwordField);
        passInput.click();
        passInput.clear();
        passInput.sendKeys(password);
    }

    public void clickLogin() {
        driver.findElement(loginButton).click();
    }

    // Validation helpers
    public boolean isUsernameErrorVisible() {
        return isElementDisplayed(usernameError);
    }

    public boolean isPasswordErrorVisible() {
        return isElementDisplayed(passwordError);
    }

    public boolean isInvalidCredsVisible() {
        return isElementDisplayed(invalidCreds);
    }

    public boolean isDashboardVisible() {
        return isElementDisplayed(dashboard);
    }

    // Safe element check
    private boolean isElementDisplayed(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
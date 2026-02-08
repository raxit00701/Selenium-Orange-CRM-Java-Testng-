package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class DashboardPage {
    private WebDriver driver;

    // Locators
    private By dashboardHeading = By.xpath("//h6");
    private By timeAtWork       = By.xpath("//p[contains(@class,'oxd-text--p') and text()='Time at Work']");
    private By quickLaunch      = By.xpath("//p[normalize-space()='Assign Leave']");
    private By myActions        = By.xpath("//div[contains(@class,'orangehrm-dashboard-widget')][.//p[text()='My Actions']]");
    private By employeesOnLeave = By.xpath("//p[contains(@class,'oxd-text--p') and text()='Employees on Leave Today']");
    private By subUnit          = By.xpath("//p[text()='Employee Distribution by Sub Unit']");
    private By locationWidget   = By.xpath("//p[contains(normalize-space(),'Employee Distribution by Location')]");
    private By chartLegend      = By.xpath("//ul[contains(@class,'oxd-chart-legend')]//span[contains(@class,'oxd-text--span')]");
    private By texasLegend      = By.xpath("//ul[contains(@class,'oxd-chart-legend')]//span[@title='Texas R&D']");
    private By nyLegend         = By.xpath("//ul[contains(@class,'oxd-chart-legend')]//span[@title='New York Sales Office']");
    private By unassignedLegend = By.xpath("//ul[contains(@class,'oxd-chart-legend')]//span[@title='Unassigned']");

    // Constructor
    public DashboardPage(WebDriver driver) {
        this.driver = driver;
    }

    // Actions / Validations
    public String getDashboardHeading() {
        return driver.findElement(dashboardHeading).getText().trim();
    }

    public boolean isTimeAtWorkVisible() {
        return isElementDisplayed(timeAtWork);
    }

    public boolean isQuickLaunchVisible() {
        return isElementDisplayed(quickLaunch);
    }

    public boolean isMyActionsVisible() {
        return isElementDisplayed(myActions);
    }

    public boolean isEmployeesOnLeaveVisible() {
        return isElementDisplayed(employeesOnLeave);
    }

    public boolean isSubUnitVisible() {
        return isElementDisplayed(subUnit);
    }

    public boolean isLocationWidgetVisible() {
        return isElementDisplayed(locationWidget);
    }

    public boolean isChartLegendVisible() {
        return isElementDisplayed(chartLegend);
    }

    public boolean isTexasLegendVisible() {
        return isElementDisplayed(texasLegend);
    }

    public boolean isNYLegendVisible() {
        return isElementDisplayed(nyLegend);
    }

    public boolean isUnassignedLegendVisible() {
        return isElementDisplayed(unassignedLegend);
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
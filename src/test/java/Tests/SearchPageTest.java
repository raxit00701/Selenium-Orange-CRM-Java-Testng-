package Tests;

import Base.API_BASE;
import Utils.CsvUtils;
import org.openqa.selenium.By;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;

import static org.testng.Assert.assertTrue;

public class SearchPageTest extends API_BASE {

    // Simple pause helper
    private void pause(int millis) throws InterruptedException {
        Thread.sleep(millis);
    }

    @DataProvider(name = "searchData")
    public Object[][] getSearchData() {
        return CsvUtils.readCsv("C:\\Users\\raxit\\IdeaProjects\\Orange crm\\src\\test\\resources\\search.csv");
    }

    @Test(dataProvider = "searchData",groups = {"SMOKE"})
    public void Searchfunctionality(String searchTerm) throws InterruptedException {
        // Implicit wait
        getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        pause(2000);

        // Step 1: Click on menu button
        getDriver().findElement(By.cssSelector("button.oxd-main-menu-button")).click();


        // Step 1: Click on menu button
        getDriver().findElement(By.cssSelector("button.oxd-main-menu-button")).click();
        assertTrue(getDriver().findElement(By.cssSelector("button.oxd-main-menu-button")).isDisplayed(),
                "Menu button should be displayed");

        // Step 2: Verify menu items
        String menuText = getDriver().findElement(By.cssSelector("ul.oxd-main-menu span.oxd-main-menu-item--name")).getText();
        System.out.println("Menu item text: " + menuText);
        assertTrue(menuText.length() > 0, "Menu item text should not be empty");

        // Step 3: Click on search input
        getDriver().findElement(By.cssSelector("input.oxd-input")).click();

        // Step 4: Enter search term from CSV
        getDriver().findElement(By.cssSelector("input.oxd-input")).sendKeys(searchTerm);
        pause(4000);

        // Step 5 & 6: Validate search results
        if (getDriver().findElements(By.cssSelector("span.oxd-main-menu-item--name")).size() > 0) {
            System.out.println("Item found for search term '" + searchTerm + "' ✅");
        } else {
            System.out.println("No search result found for '" + searchTerm + "' ✅");
        }
    }
}
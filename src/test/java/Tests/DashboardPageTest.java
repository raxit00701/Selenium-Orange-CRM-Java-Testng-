package Tests;

import Base.API_BASE;
import pages.DashboardPage;
import org.testng.annotations.Test;

import java.time.Duration;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class DashboardPageTest extends API_BASE {

    private void pause(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test(groups = {"REG"})
    public void verifyHomepageWidgets() {
        getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        pause(2000);

        DashboardPage dashboardPage = new DashboardPage(getDriver());

        // Verify DashboardPageTest heading
        String heading = dashboardPage.getDashboardHeading();
        System.out.println("Dashboard heading: " + heading);
        assertEquals(heading, "Dashboard", "Dashboard text is incorrect");

        // Verify widgets
        assertTrue(dashboardPage.isTimeAtWorkVisible(), "Time at Work not visible");
        assertTrue(dashboardPage.isQuickLaunchVisible(), "Quick Launch not visible");
        assertTrue(dashboardPage.isMyActionsVisible(), "My Actions not visible");
        assertTrue(dashboardPage.isEmployeesOnLeaveVisible(), "Employees on Leave Today not visible");
        assertTrue(dashboardPage.isSubUnitVisible(), "Sub Unit not visible");
        assertTrue(dashboardPage.isLocationWidgetVisible(), "Location widget not visible");
        assertTrue(dashboardPage.isChartLegendVisible(), "Chart legend not visible");
        assertTrue(dashboardPage.isTexasLegendVisible(), "Texas R&D legend not visible");
        assertTrue(dashboardPage.isNYLegendVisible(), "NY Sales Office legend not visible");
        assertTrue(dashboardPage.isUnassignedLegendVisible(), "Unassigned legend not visible");

        System.out.println("All dashboard widgets verified ✅");
    }
}
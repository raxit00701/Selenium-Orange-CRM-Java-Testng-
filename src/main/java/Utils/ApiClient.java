package Utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.JavascriptExecutor;

public class ApiClient {

    private static final String BASE_URL       = "https://opensource-demo.orangehrmlive.com";
    private static final String LOGIN_PAGE_URL = BASE_URL + "/web/index.php/auth/login";
    private static final String LOGIN_POST_URL = BASE_URL + "/web/index.php/auth/validate";
    private static final String DASHBOARD_URL  = BASE_URL + "/web/index.php/dashboard/index";

    private static final String USERNAME = "Admin";
    private static final String PASSWORD = "admin123";

    /**
     * Authenticates the browser session.
     * Leaves navigation decision + element waits to the test layer.
     */
    public void injectSessionCookie(WebDriver driver) {

        JavascriptExecutor js = (JavascriptExecutor) driver;

        // 1️⃣ Load login page (Vue renders CSRF token)
        driver.get(LOGIN_PAGE_URL);

        String csrfToken = waitForCsrfToken(js);

        // 2️⃣ Perform login via browser fetch()
        String script =
                "var body = '_token=' + encodeURIComponent(arguments[0])" +
                        " + '&username=' + encodeURIComponent(arguments[1])" +
                        " + '&password=' + encodeURIComponent(arguments[2]);" +
                        "return fetch(arguments[3], {" +
                        " method: 'POST'," +
                        " headers: { 'Content-Type': 'application/x-www-form-urlencoded' }," +
                        " body: body," +
                        " redirect: 'manual'" +
                        "}).then(r => r.status);";

        long status = ((Number) js.executeScript(
                script, csrfToken, USERNAME, PASSWORD, LOGIN_POST_URL
        )).longValue();

        if (status != 302 && status != 0) {
            throw new RuntimeException("Login failed. Status: " + status);
        }

        // 3️⃣ Just navigate — NO waits, NO element logic
        driver.get(DASHBOARD_URL);
    }

    private String waitForCsrfToken(JavascriptExecutor js) {
        long timeout = 10_000;
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < timeout) {
            Object token = js.executeScript(
                    "var el = document.querySelector('input[name=\"_token\"]');" +
                            "return el && el.value ? el.value : null;"
            );
            if (token != null) {
                return token.toString();
            }
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        }

        throw new RuntimeException("Timed out waiting for CSRF token");
    }
}

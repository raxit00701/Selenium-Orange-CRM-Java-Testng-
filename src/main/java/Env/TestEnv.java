package Env;

public class TestEnv implements EnvConfig {

    @Override
    public String getBaseUrl() {
        return "https://opensource-demo.orangehrmlive.com/";
    }

    @Override
    public String getEnvName() {
        return "TEST";
    }
}

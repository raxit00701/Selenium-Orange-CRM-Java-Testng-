package Env;

public class PreProdEnv implements EnvConfig {

    @Override
    public String getBaseUrl() {
        return "https://opensource-demo.orangehrmlive.com/";
    }

    @Override
    public String getEnvName() {
        return "PRE-PROD";
    }
}

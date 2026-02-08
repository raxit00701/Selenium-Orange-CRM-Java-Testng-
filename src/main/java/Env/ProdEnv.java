package Env;

public class ProdEnv implements EnvConfig {

    @Override
    public String getBaseUrl() {
        return "https://google.com/";
    }

    @Override
    public String getEnvName() {
        return "PROD";
    }
}

package com.projecki.gyro;

public class GyroServiceEntry {

    public static void main(String[] args) {
        String redisHost = getAndValidateCred("REDIS_HOST");
        String redisPort = getAndValidateCred("REDIS_PORT");
        String redisPass = getAndValidateCred("REDIS_PASS");
        String host = getAndValidateCred("BIND_HOST");
        String port = getAndValidateCred("BIND_PORT");
        String organization = getAndValidateCred("ORGANIZATION");
        GyroService gyroService = new GyroService(new GyroService.Creds(redisHost, redisPort, redisPass, host, port, organization));
    }

    private static String getAndValidateCred(String env) {
        String val = System.getenv(env);
        if (val == null) {
            throw new IllegalStateException(env + " not found in environment");
        }
        return val;
    }
}

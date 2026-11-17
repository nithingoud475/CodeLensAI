package com.codelens.ai;

public class TestRunner {
    public static void main(String[] args) {
        int passed = 0;
        int failed = 0;

        System.out.println("=== RUNNING VERIFICATION TESTS ===");

        // 1. DotenvLoadingTest
        try {
            System.out.print("Running DotenvLoadingTest.testDotenvPropertiesLoaded... ");
            DotenvLoadingTest dotenvTest = new DotenvLoadingTest();
            DotenvLoadingTest.setUp();
            dotenvTest.testDotenvPropertiesLoaded();
            System.out.println("PASSED");
            passed++;

            System.out.print("Running DotenvLoadingTest.testEnvironmentPostProcessorRegistersSource... ");
            dotenvTest.testEnvironmentPostProcessorRegistersSource();
            System.out.println("PASSED");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            t.printStackTrace(System.out);
            failed++;
        }

        // 2. AuthControllerEndpointsTest - testEmailLoginEndpointsRemoved
        try {
            System.out.print("Running AuthControllerEndpointsTest.testEmailLoginEndpointsRemoved... ");
            AuthControllerEndpointsTest authTest = new AuthControllerEndpointsTest();
            authTest.testEmailLoginEndpointsRemoved();
            System.out.println("PASSED");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            t.printStackTrace(System.out);
            failed++;
        }

        // 3. AuthControllerEndpointsTest - testPreservedEndpointsExist
        try {
            System.out.print("Running AuthControllerEndpointsTest.testPreservedEndpointsExist... ");
            AuthControllerEndpointsTest authTest = new AuthControllerEndpointsTest();
            authTest.testPreservedEndpointsExist();
            System.out.println("PASSED");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            t.printStackTrace(System.out);
            failed++;
        }

        // 4. AuthControllerEndpointsTest - testGoogleClientIdEndpointPreservedAndDoesNotExposeSecret
        try {
            System.out.print("Running AuthControllerEndpointsTest.testGoogleClientIdEndpointPreservedAndDoesNotExposeSecret... ");
            AuthControllerEndpointsTest authTest = new AuthControllerEndpointsTest();
            authTest.testGoogleClientIdEndpointPreservedAndDoesNotExposeSecret();
            System.out.println("PASSED");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            t.printStackTrace(System.out);
            failed++;
        }

        // 5. JwtTokenProviderTest - testJwtGenerationAndValidationWithDotenvSecret
        try {
            System.out.print("Running JwtTokenProviderTest.testJwtGenerationAndValidationWithDotenvSecret... ");
            JwtTokenProviderTest jwtTest = new JwtTokenProviderTest();
            JwtTokenProviderTest.setUp();
            jwtTest.testJwtGenerationAndValidationWithDotenvSecret();
            System.out.println("PASSED");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            t.printStackTrace(System.out);
            failed++;
        }

        System.out.println("\n=== TEST SUMMARY ===");
        System.out.println("Total tests: " + (passed + failed));
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);

        if (failed > 0) {
            System.exit(1);
        }
    }
}

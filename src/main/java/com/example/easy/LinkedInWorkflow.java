package com.example.easy;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Main workflow class to orchestrate the LinkedIn automation
 * This combines the LinkedInOpener for login and LinkedInJobsNavigator for job browsing
 */
public class LinkedInWorkflow {
    
    /**
     * Main method to run the complete LinkedIn workflow
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // Declare the driver outside the try block so it's accessible in the finally block
        WebDriver driver = null;
        
        try {
            // Setup WebDriverManager for Chrome
            WebDriverManager.chromedriver().setup();
            
            // Configure Chrome options for stealth mode
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--start-maximized");
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-popup-blocking");
            options.addArguments("--disable-infobars");
            options.setExperimentalOption("excludeSwitches", 
                    new String[]{"enable-automation", "enable-logging"});
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");
            
            // Initialize Chrome driver with stealth options
            driver = new ChromeDriver(options);
            System.out.println("Browser initialized successfully.");
            
            // Step 1: Use LinkedInOpener to log in
            System.out.println("Step 1: Starting LinkedIn login process...");
            boolean loginSuccess = loginToLinkedIn(driver);
            
            // Step 2: If login successful, navigate to Jobs
            if (loginSuccess) {
                System.out.println("Step 2: Starting LinkedIn Jobs navigation...");
                
                // Wait a bit after login to simulate human behavior
                Thread.sleep(3000 + (int)(Math.random() * 2000));
                
                // Navigate to jobs
                LinkedInJobsNavigator.navigateToJobs(driver);
                
                System.out.println("LinkedIn workflow completed successfully!");
            } else {
                System.out.println("Login failed. Cannot proceed to Jobs navigation.");
            }
            
            // Keep the browser open at the end
            System.out.println("Workflow complete. Browser will remain open for 30 seconds.");
            Thread.sleep(30000);
            
        } catch (Exception e) {
            System.out.println("An error occurred during the workflow: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Always close the browser in the finally block
            if (driver != null) {
                driver.quit();
                System.out.println("Browser closed.");
            }
        }
    }
    
    /**
     * Logs into LinkedIn using the credentials and methods from LinkedInOpener
     * @param driver WebDriver instance
     * @return true if login successful, false otherwise
     */
    private static boolean loginToLinkedIn(WebDriver driver) {
        try {
            // Navigate to LinkedIn sign-in page
            driver.get("https://www.linkedin.com/checkpoint/lg/sign-in-another-account");
            System.out.println("LinkedIn sign-in page opened successfully.");
            Thread.sleep(2000);
            
            // Replace with your LinkedIn credentials
            String email = "suhas.dasari194@gmail.com";
            String password = "K17@h13$.Sd";
            
            // This is a simplified version of the login code from LinkedInOpener
            // Find the email field
            org.openqa.selenium.WebElement emailField = driver.findElement(org.openqa.selenium.By.id("username"));
            emailField.clear();
            
            // Type email character by character with human-like delays
            LinkedInOpener.typeHumanLike(driver, emailField, email);
            
            // Random delay between email and password fields
            Thread.sleep(500 + (long)(Math.random() * 1000));
            
            // Find the password field
            org.openqa.selenium.WebElement passwordField = driver.findElement(org.openqa.selenium.By.id("password"));
            passwordField.clear();
            
            // Type password character by character with human-like delays
            LinkedInOpener.typeHumanLike(driver, passwordField, password);
            
            // Random delay after entering password before clicking submit
            Thread.sleep(500 + (long)(Math.random() * 1000));
            
            // Click the sign-in button
            org.openqa.selenium.WebElement signInButton = driver.findElement(org.openqa.selenium.By.xpath("//button[@type='submit']"));
            signInButton.click();
            System.out.println("Login credentials submitted.");
            
            // Wait for authentication
            waitForAuthentication(driver);
            
            // Check if we're logged in
            boolean loggedIn = isLoggedIn(driver);
            if (loggedIn) {
                System.out.println("Successfully logged into LinkedIn!");
                return true;
            } else {
                System.out.println("Login attempt unsuccessful.");
                return false;
            }
            
        } catch (Exception e) {
            System.out.println("Error during login: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Waits for authentication to complete, including mobile verification if needed
     * @param driver WebDriver instance
     * @throws Exception If any error occurs
     */
    private static void waitForAuthentication(WebDriver driver) throws Exception {
        System.out.println("Checking for authentication requirements...");
        Thread.sleep(5000);
        
        // Check if mobile authentication is required
        boolean mobileAuthRequired = !driver.findElements(org.openqa.selenium.By.xpath("//*[contains(text(), 'authentication')]")).isEmpty() ||
                                  !driver.findElements(org.openqa.selenium.By.xpath("//*[contains(text(), 'verification')]")).isEmpty() ||
                                  !driver.findElements(org.openqa.selenium.By.xpath("//*[contains(text(), 'confirm')]")).isEmpty() ||
                                  !driver.findElements(org.openqa.selenium.By.xpath("//*[contains(text(), 'verify')]")).isEmpty() ||
                                  !driver.findElements(org.openqa.selenium.By.xpath("//*[contains(text(), 'device')]")).isEmpty() ||
                                  !driver.findElements(org.openqa.selenium.By.xpath("//*[contains(text(), 'phone')]")).isEmpty();
        
        if (mobileAuthRequired) {
            System.out.println("Mobile authentication required. Please check your mobile device.");
            System.out.println("Waiting for 60 seconds for you to complete authentication...");
            
            // Loop to check for login success while waiting
            int totalWaitTime = 0;
            boolean authSuccess = false;
            while (totalWaitTime < 60000 && !authSuccess) {
                // Wait in smaller increments and check for successful login
                Thread.sleep(5000);
                totalWaitTime += 5000;
                
                System.out.println("Checking login status... (waited " + totalWaitTime/1000 + " seconds)");
                
                if (isLoggedIn(driver)) {
                    System.out.println("Authentication successful! Detected logged-in state.");
                    authSuccess = true;
                    break;
                }
            }
        } else {
            System.out.println("No mobile authentication detected.");
        }
    }
    
    /**
     * Checks if user is currently logged into LinkedIn
     * @param driver WebDriver instance
     * @return true if logged in, false otherwise
     */
    private static boolean isLoggedIn(WebDriver driver) {
        try {
            String currentUrl = driver.getCurrentUrl();
            System.out.println("Current URL: " + currentUrl);
            
            return currentUrl.contains("feed") || 
                  currentUrl.contains("mynetwork") || 
                  currentUrl.contains("messaging") ||
                  currentUrl.contains("notifications") ||
                  currentUrl.contains("jobs") ||
                  !driver.findElements(org.openqa.selenium.By.xpath("//*[contains(@class, 'feed-identity-module')]")).isEmpty() ||
                  !driver.findElements(org.openqa.selenium.By.xpath("//*[contains(@id, 'global-nav')]")).isEmpty() ||
                  !driver.findElements(org.openqa.selenium.By.xpath("//*[contains(@data-test-id, 'nav-settings')]")).isEmpty();
        } catch (Exception e) {
            System.out.println("Error checking login status: " + e.getMessage());
            return false;
        }
    }
}

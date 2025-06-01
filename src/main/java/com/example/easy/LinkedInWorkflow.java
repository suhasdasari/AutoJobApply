package com.example.easy;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;
import com.example.easy.JobApplier.ContactInfo;

import java.util.Scanner;

/**
 * Main workflow class to orchestrate the LinkedIn automation
 * This combines the LinkedInOpener for login, LinkedInJobsNavigator for job browsing,
 * and contact info handling for Easy Apply applications
 */
public class LinkedInWorkflow {
    
    /**
     * Main method to run the complete LinkedIn workflow
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // Declare the driver and contactInfo outside the try block so they're accessible in the finally block
        WebDriver driver = null;
        ContactInfo contactInfo = null;
        Scanner scanner = null;
        
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
                
                // Step 3: Click on the first job and start Easy Apply process
                System.out.println("Step 3: Starting Easy Apply process...");
                
                // Create contact info handler
                contactInfo = new ContactInfo(driver);
                
                // Click on first job and start Easy Apply
                boolean applyStarted = contactInfo.clickFirstJobAndApply();
                
                if (applyStarted) {
                    System.out.println("Successfully started Easy Apply process.");
                    
                    // Handle contact info form
                    boolean contactInfoHandled = contactInfo.handleContactInfo();
                    
                    if (contactInfoHandled) {
                        System.out.println("Contact info section completed successfully.");
                        System.out.println("The application will now wait for you to review and complete the remaining steps.");
                        
                        // Keep the browser open to allow user to complete the application
                        System.out.println("Press Enter to close the browser when done...");
                        scanner = new Scanner(System.in);
                        scanner.nextLine();
                    } else {
                        System.out.println("Failed to handle contact info section. The form may be different than expected.");
                        System.out.println("Check the screenshots saved in the project directory for debugging.");
                    }
                } else {
                    System.out.println("Failed to start Easy Apply process. The Easy Apply button may not have been found.");
                    System.out.println("This job may not support Easy Apply or the button was not detected.");
                    System.out.println("Check the screenshots saved in the project directory for debugging.");
                }
            } else {
                System.out.println("LinkedIn login failed. Cannot proceed with job application.");
                System.out.println("Please check your credentials in the properties file and ensure they are correct.");
            }
            
        } catch (Exception e) {
            System.out.println("Error during workflow execution: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clean up resources
            try {
                // Close the scanner if it was opened
                if (scanner != null) {
                    scanner.close();
                    System.out.println("Scanner closed.");
                }
                
                // Clean up the ContactInfo resources
                if (contactInfo != null) {
                    contactInfo.cleanup();
                    System.out.println("ContactInfo resources cleaned up.");
                }
                
                // Quit the driver
                if (driver != null) {
                    driver.quit();
                    System.out.println("Browser closed.");
                }
            } catch (Exception e) {
                System.out.println("Error during cleanup: " + e.getMessage());
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

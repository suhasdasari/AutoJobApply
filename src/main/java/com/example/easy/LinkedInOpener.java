package com.example.easy;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.util.HashMap;
import java.util.Map;

public class LinkedInOpener {
    
    /**
     * Types text into an element character by character with random delays to simulate human typing
     * 
     * @param driver WebDriver instance
     * @param element The web element to type into
     * @param text The text to type
     * @throws InterruptedException If sleep is interrupted
     */
    public static void typeHumanLike(WebDriver driver, org.openqa.selenium.WebElement element, String text) throws InterruptedException {
        // Human typing patterns are highly variable, so we'll simulate that
        char[] chars = text.toCharArray();
        
        // Set a baseline typing speed that varies per "session"
        // Some people are fast typists, others are slow
        int baselineSpeed = 70 + (int)(Math.random() * 150); // 70-220ms base typing speed
        
        // Define common character groups for realistic typing patterns
        String easyChars = "asdfjkl;"; // Home row keys are typically faster
        String punctuation = ".,!?-_()"; // Punctuation typically causes slight pauses
        String numbers = "0123456789"; // Number typing often requires looking at keyboard
        String shiftChars = "~!@#$%^&*()_+{}|:\"<>?"; // Characters requiring shift key
        
        char previousChar = 0;
        
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            
            // Start with base delay
            long delay = baselineSpeed;
            
            // Adjust delay based on character type
            if (easyChars.indexOf(c) >= 0) {
                // Home row keys are faster
                delay -= 10 + (int)(Math.random() * 40); // Reduce delay by 10-50ms
            } else if (punctuation.indexOf(c) >= 0) {
                // Punctuation causes slight pauses
                delay += 50 + (int)(Math.random() * 100); // Add 50-150ms
            } else if (numbers.indexOf(c) >= 0) {
                // Numbers take longer to type for many people
                delay += 30 + (int)(Math.random() * 70); // Add 30-100ms
            } else if (shiftChars.indexOf(c) >= 0) {
                // Special characters requiring shift key take longer
                delay += 80 + (int)(Math.random() * 120); // Add 80-200ms
            }
            
            // Adjust for repeated characters (usually faster)
            if (previousChar == c) {
                delay -= 20 + (int)(Math.random() * 30); // Faster when repeating the same key
            }
            
            // Randomize by +/- 30% to simulate natural variation
            delay = Math.max(50, delay * (70 + (int)(Math.random() * 60)) / 100);
            
            // Sometimes humans make "bursts" of fast typing
            if (Math.random() < 0.15) { // 15% chance of a typing burst
                delay = Math.max(50, delay / 2);
            }
            
            // Add longer pauses after certain sections
            if (c == '.' || c == '!' || c == '?' || c == '@') {
                delay += 200 + (int)(Math.random() * 300); // Add 200-500ms after domain separators or sentence endings
            }
            
            // Type a single character
            element.sendKeys(String.valueOf(c));
            previousChar = c;
            
            // Special case: @ symbol in email often causes people to pause
            if (c == '@' && i < chars.length - 1) {
                // Add an extra pause of 300-800ms after typing the @ symbol
                Thread.sleep(300 + (long) (Math.random() * 500));
            }

            // Special case: Pause slightly more between domain and TLD
            if (c == '.' && text.contains("@") && i > text.indexOf('@')) {
                // Add a pause of 100-300ms after typing the period in an email domain
                Thread.sleep(100 + (long) (Math.random() * 200));
            }

            // Sleep for the calculated delay before typing the next character
            Thread.sleep(delay);

            // Occasionally take a longer pause as if reviewing what's been typed so far
            if (Math.random() < 0.05) { // 5% chance of a review pause
                Thread.sleep(500 + (long) (Math.random() * 1500)); // 500-2000ms pause
            }
        }
    }

    public static void main(String[] args) {
        // Declare the driver outside the try block so it's accessible in the finally block
        WebDriver driver = null;

        try {
            // Setup WebDriverManager for Chrome
            WebDriverManager.chromedriver().setup();

            
            // Configure Chrome options for stealth mode
            ChromeOptions options = new ChromeOptions();
            
            // Add arguments to make Chrome more stealthy
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--start-maximized");
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-popup-blocking");
            options.addArguments("--disable-infobars");
            
            // Add exclude switches
            options.setExperimentalOption("excludeSwitches", 
                    new String[]{"enable-automation", "enable-logging"});
            
            // Set user agent to a common one
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");
            
            // Add preferences to appear more human-like
            Map<String, Object> prefs = new HashMap<>();
            prefs.put("credentials_enable_service", false);
            prefs.put("profile.password_manager_enabled", false);
            options.setExperimentalOption("prefs", prefs);
            
            // Initialize Chrome driver with stealth options
            driver = new ChromeDriver(options);
            
            // Navigate to LinkedIn sign-in page
            driver.get("https://www.linkedin.com/checkpoint/lg/sign-in-another-account");
            System.out.println("LinkedIn sign-in page opened successfully.");
            Thread.sleep(2000);

            String email = "suhas.dasari194@gmail.com";
            String password = "K17@h13$.Sd";
            
            // Find the email field
            org.openqa.selenium.WebElement emailField = driver.findElement(org.openqa.selenium.By.id("username"));
            emailField.clear();
            
            // Type email character by character with human-like delays
            typeHumanLike(driver, emailField, email);
            
            // Random delay between email and password fields (like a human would pause)
            Thread.sleep(500 + (long)(Math.random() * 1000));
            
            // Find the password field
            org.openqa.selenium.WebElement passwordField = driver.findElement(org.openqa.selenium.By.id("password"));
            passwordField.clear();
            
            // Type password character by character with human-like delays
            typeHumanLike(driver, passwordField, password);
            
            // Random delay after entering password before clicking submit (like a human would pause)
            Thread.sleep(500 + (long)(Math.random() * 1000));
            
            // Click the sign-in button
            org.openqa.selenium.WebElement signInButton = driver.findElement(org.openqa.selenium.By.xpath("//button[@type='submit']"));
            signInButton.click();
            System.out.println("Login credentials submitted.");
            
            // Wait a moment for the page to update after login attempt
            Thread.sleep(5000);
            
            // Check for invalid credentials error messages - more specific checks to avoid false positives
            boolean invalidCredentials = false;
            try {
                // Take a screenshot for debugging
                try {
                    org.openqa.selenium.OutputType<java.io.File> outputType = org.openqa.selenium.OutputType.FILE;
                    java.io.File screenshot = ((org.openqa.selenium.TakesScreenshot) driver).getScreenshotAs(outputType);
                    java.nio.file.Path destination = java.nio.file.Paths.get("linkedin_credentials_check.png");
                    java.nio.file.Files.copy(screenshot.toPath(), destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Credentials check screenshot saved to: " + destination.toAbsolutePath());
                } catch (Exception e) {
                    System.out.println("Failed to save credentials check screenshot: " + e.getMessage());
                }
                
                // More specific error checks to avoid false positives
                // Only check for explicit credential error messages
                invalidCredentials = !driver.findElements(By.xpath("//*[contains(text(), 'incorrect email or password')]")).isEmpty() ||
                                   !driver.findElements(By.xpath("//*[contains(text(), 'wrong password')]")).isEmpty() ||
                                   !driver.findElements(By.xpath("//*[contains(text(), 'password you provided is incorrect')]")).isEmpty();
                
                // Print current URL for debugging
                System.out.println("Current URL during credential check: " + driver.getCurrentUrl());
                
                // If we've been redirected to the login page again, that's a good indicator of invalid credentials
                if (driver.getCurrentUrl().contains("checkpoint/lg/sign-in") || 
                    driver.getCurrentUrl().contains("checkpoint/rm/sign-in")) {
                    System.out.println("Detected redirect back to login page - potential credential issue");
                    // But only mark as invalid if we also see an error message
                    if (!driver.findElements(By.xpath("//*[contains(@class, 'alert')]")).isEmpty() ||
                        !driver.findElements(By.xpath("//*[contains(@class, 'error')]")).isEmpty()) {
                        invalidCredentials = true;
                    }
                }
                
                System.out.println("Invalid credentials detected: " + invalidCredentials);
            } catch (Exception e) {
                System.out.println("Error checking for invalid credentials: " + e.getMessage());
            }
            
            if (invalidCredentials) {
                System.out.println("ERROR: Invalid credentials provided. Login failed.");
                System.out.println("Please check your email and password and try again.");
                // Take a screenshot of the error for reference
                try {
                    org.openqa.selenium.OutputType<java.io.File> outputType = org.openqa.selenium.OutputType.FILE;
                    java.io.File screenshot = ((org.openqa.selenium.TakesScreenshot) driver).getScreenshotAs(outputType);
                    java.nio.file.Path destination = java.nio.file.Paths.get("linkedin_error.png");
                    java.nio.file.Files.copy(screenshot.toPath(), destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Error screenshot saved to: " + destination.toAbsolutePath());
                } catch (Exception e) {
                    System.out.println("Failed to save error screenshot: " + e.getMessage());
                }
                // Keep browser open a bit longer to see the error, then close it
                System.out.println("Browser will close in 5 seconds...");
                Thread.sleep(5000);
                return;
            }
            
            // If no invalid credentials error, continue checking for mobile authentication
            System.out.println("Checking for mobile authentication prompt...");
            
            // Check if mobile authentication is required
            boolean mobileAuthRequired = false;
            try {
                // Try to find elements that might indicate mobile authentication
                mobileAuthRequired = !driver.findElements(By.xpath("//*[contains(text(), 'authentication')]")).isEmpty() ||
                                   !driver.findElements(By.xpath("//*[contains(text(), 'verification')]")).isEmpty() ||
                                   !driver.findElements(By.xpath("//*[contains(text(), 'confirm')]")).isEmpty() ||
                                   !driver.findElements(By.xpath("//*[contains(text(), 'verify')]")).isEmpty() ||
                                   !driver.findElements(By.xpath("//*[contains(text(), 'device')]")).isEmpty() ||
                                   !driver.findElements(By.xpath("//*[contains(text(), 'phone')]")).isEmpty();
            } catch (Exception e) {
                // If there's an error, assume no mobile auth required
                mobileAuthRequired = false;
            }
            
            if (mobileAuthRequired) {
                System.out.println("Mobile authentication required. Please check your mobile device.");
                System.out.println("Waiting for 60 seconds for you to complete authentication...");
                
                // Loop to check for login success while waiting for authentication
                int totalWaitTime = 0;
                boolean authSuccess = false;
                while (totalWaitTime < 60000 && !authSuccess) {
                    // Wait in smaller increments and check for successful login
                    Thread.sleep(5000);
                    totalWaitTime += 5000;
                    
                    System.out.println("Checking login status... (waited " + totalWaitTime/1000 + " seconds)");
                    
                    // Take a screenshot for debugging
                    try {
                        org.openqa.selenium.OutputType<java.io.File> outputType = org.openqa.selenium.OutputType.FILE;
                        java.io.File screenshot = ((org.openqa.selenium.TakesScreenshot) driver).getScreenshotAs(outputType);
                        java.nio.file.Path destination = java.nio.file.Paths.get("linkedin_auth_progress_" + totalWaitTime/1000 + "s.png");
                        java.nio.file.Files.copy(screenshot.toPath(), destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Progress screenshot saved to: " + destination.toAbsolutePath());
                    } catch (Exception e) {
                        System.out.println("Failed to save progress screenshot: " + e.getMessage());
                    }
                    
                    // Check if we're logged in using multiple methods
                    String currentUrl = driver.getCurrentUrl();
                    System.out.println("Current URL: " + currentUrl);
                    
                    if (currentUrl.contains("feed") || 
                        currentUrl.contains("mynetwork") || 
                        currentUrl.contains("messaging") ||
                        currentUrl.contains("notifications") ||
                        currentUrl.contains("jobs") ||
                        !driver.findElements(By.xpath("//*[contains(@class, 'feed-identity-module')]")).isEmpty() ||
                        !driver.findElements(By.xpath("//*[contains(@id, 'global-nav')]")).isEmpty() ||
                        !driver.findElements(By.xpath("//*[contains(@data-test-id, 'nav-settings')]")).isEmpty()) {
                        
                        System.out.println("Authentication successful! Detected logged-in state.");
                        authSuccess = true;
                        break;
                    }
                }
                
                if (authSuccess) {
                    System.out.println("Successfully authenticated and logged in!");
                } else {
                    System.out.println("Authentication wait time completed. Continuing regardless...");
                }
            } else {
                System.out.println("No mobile authentication detected.");
            }
            
            // Final wait to give page time to fully load
            Thread.sleep(5000);
            
            // Check if we're logged in by looking for elements that would only be present when logged in
            boolean loggedIn = driver.getCurrentUrl().contains("feed") || 
                              driver.getCurrentUrl().contains("mynetwork") || 
                              driver.getCurrentUrl().contains("messaging") ||
                              driver.getCurrentUrl().contains("notifications") ||
                              driver.getCurrentUrl().contains("jobs") ||
                              !driver.findElements(By.xpath("//*[contains(@class, 'feed-identity-module')]")).isEmpty() ||
                              !driver.findElements(By.xpath("//*[contains(@id, 'global-nav')]")).isEmpty() ||
                              !driver.findElements(By.xpath("//*[contains(@data-test-id, 'nav-settings')]")).isEmpty();
                              
            // Additional debugging
            System.out.println("Final URL after authentication flow: " + driver.getCurrentUrl());
            
            if (loggedIn) {
                System.out.println("Login successful! You are now logged into LinkedIn.");
            } else {
                System.out.println("Login status uncertain. Please check the browser.");
            }
            
            // Keep the browser open for a while to show the results
            System.out.println("Browser will remain open for 30 seconds to view the result.");
            Thread.sleep(30000);
        } catch (org.openqa.selenium.NoSuchElementException e) {
            System.out.println("Error: Could not find expected login elements. The LinkedIn page structure might have changed.");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Always close the browser in the finally block
            if (driver != null) {
                driver.quit();
                System.out.println("Browser closed.");
            }
        }
    }
}
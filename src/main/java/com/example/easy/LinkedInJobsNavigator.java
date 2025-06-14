package com.example.easy;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Class to navigate LinkedIn Jobs section with human-like interactions
 * Includes robust methods to find and interact with the "Show all" button
 */
public class LinkedInJobsNavigator {
    
    private static final Random random = new Random();
    
    /**
     * Navigates to the LinkedIn Jobs page and interacts with it in a human-like manner
     * @param driver The WebDriver instance with LinkedIn already logged in
     * @throws Exception If any errors occur during navigation
     */
    public static void navigateToJobs(WebDriver driver) throws Exception {
        // Ensure we're on LinkedIn before proceeding
        if (!driver.getCurrentUrl().contains("linkedin.com")) {
            throw new IllegalStateException("Not on LinkedIn. Current URL: " + driver.getCurrentUrl());
        }
        
        System.out.println("Starting LinkedIn Jobs navigation...");
        
        // Random wait to simulate looking at the feed first
        humanWait(3000, 7000);
        
        // Take a screenshot before navigation
        takeScreenshot(driver, "before_jobs_navigation.png");
        
        // Find the Jobs navigation button using various selectors
        WebElement jobsButton = null;
        try {
            // Try multiple selectors to find the Jobs button
            List<By> possibleSelectors = List.of(
                By.xpath("//a[@href='/jobs/' and contains(@data-link-to, 'jobs')]"),
                By.xpath("//a[contains(@href, '/jobs/') and contains(., 'Jobs')]"),
                By.xpath("//span[text()='Jobs']/ancestor::a"),
                By.xpath("//li[contains(@class, 'nav')]/a[contains(@href, '/jobs')]"),
                By.xpath("//nav//a[contains(@href, '/jobs')]")
            );
            
            // Try each selector until we find one that works
            for (By selector : possibleSelectors) {
                List<WebElement> elements = driver.findElements(selector);
                if (!elements.isEmpty()) {
                    // If we found elements, return the first visible one
                    for (WebElement element : elements) {
                        if (element.isDisplayed()) {
                            jobsButton = element;
                            break;
                        }
                    }
                }
                if (jobsButton != null) break;
            }
        } catch (Exception e) {
            System.out.println("Error finding Jobs button: " + e.getMessage());
        }
        
        if (jobsButton == null) {
            System.out.println("Could not find Jobs button. Taking a screenshot and attempting an alternative method...");
            takeScreenshot(driver, "jobs_button_not_found.png");
            
            // Try an alternative method - direct URL navigation with human-like delay
            humanScrollDown(driver);
            humanWait(1000, 3000);
            driver.navigate().to("https://www.linkedin.com/jobs/");
            System.out.println("Navigated directly to jobs page via URL");
            humanWait(2000, 4000);
        } else {
            // Move to Jobs button like a human (with random path)
            System.out.println("Jobs button found. Moving cursor to it...");
            moveMouseLikeHuman(driver, jobsButton);
            
            // Click on the Jobs button
            System.out.println("Clicking on Jobs button...");
            jobsButton.click();
            
            // Wait for page to load
            humanWait(2000, 5000);
            System.out.println("Navigated to Jobs page");
        }
        
        // Take a screenshot after navigation
        takeScreenshot(driver, "after_jobs_navigation.png");
        
        // Once on the Jobs page, try to find and click the "Show all" button
        try {
            System.out.println("Looking for 'Show all' button in LinkedIn Jobs page...");
            
            // Scroll down a bit to ensure elements are visible
            humanScrollDown(driver);
            humanWait(1000, 2000);
            
            // Try to find and click the "Show all" button
            boolean showAllButtonClicked = findAndClickShowAllButton(driver);
            
            if (showAllButtonClicked) {
                System.out.println("Successfully clicked 'Show all' button");
            } else {
                System.out.println("Could not find or click 'Show all' button. Continuing with available jobs.");
            }
            
            // Enter job role and location
            enterJobSearchCriteria(driver);
            
        } catch (Exception e) {
            System.out.println("Error while finding or clicking 'Show all' button: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Enters job search criteria (role and location) on the LinkedIn Jobs page
     * Uses values from the configuration file
     * 
     * @param driver WebDriver instance
     * @throws Exception If any error occurs
     */
    private static void enterJobSearchCriteria(WebDriver driver) throws Exception {
        System.out.println("Entering job search criteria...");
        
        // Get job role and location from configuration
        String jobRole = LinkedInConfigLoader.getJobRole();
        String jobLocation = LinkedInConfigLoader.getJobLocation();
        
        // Get filter options
        String sortBy = LinkedInConfigLoader.getSortByOption();
        String jobType = LinkedInConfigLoader.getJobTypeOption();
        String expLevel = LinkedInConfigLoader.getExperienceLevelOption();
        boolean easyApply = LinkedInConfigLoader.isEasyApplyEnabled();
        String remoteOption = LinkedInConfigLoader.getRemoteFilterOption();
        boolean remote = remoteOption != null && !remoteOption.isEmpty();
        
        System.out.println("Job role: " + jobRole);
        System.out.println("Job location: " + jobLocation);
        System.out.println("Filters - Sort by: " + sortBy + ", Job type: " + jobType + ", Experience level: " + expLevel);
        System.out.println("Easy apply: " + easyApply + ", Remote: " + remote);
        
        // Find the job role input field
        WebElement roleInput = findSearchInputField(driver, "role", Arrays.asList(
            By.xpath("//input[contains(@id, 'jobs-search-box-keyword-id')]"),
            By.xpath("//input[contains(@aria-label, 'Search by title')]"),
            By.xpath("//input[contains(@aria-label, 'job title')]"),
            By.xpath("//input[contains(@placeholder, 'job title')]"),
            By.xpath("//input[contains(@placeholder, 'Search by title')]"),
            By.xpath("//div[contains(@class, 'jobs-search-box')]//*[contains(@class, 'jobs-search-box__text-input')][1]"),
            By.xpath("//form[contains(@class, 'jobs-search-box')]//input[@type='text'][1]")
        ));
        
        // Find the job location input field
        WebElement locationInput = findSearchInputField(driver, "location", Arrays.asList(
            By.xpath("//input[contains(@id, 'jobs-search-box-location-id')]"),
            By.xpath("//input[contains(@aria-label, 'location')]"),
            By.xpath("//input[contains(@placeholder, 'location')]"),
            By.xpath("//div[contains(@class, 'jobs-search-box')]//*[contains(@class, 'jobs-search-box__text-input')][2]"),
            By.xpath("//form[contains(@class, 'jobs-search-box')]//input[@type='text'][2]")
        ));
        
        // Clear and fill job role with human-like typing
        if (roleInput != null) {
            roleInput.clear();
            humanWait(500, 1000);
            typeHumanLike(driver, roleInput, jobRole);
            humanWait(800, 1500);
        } else {
            System.out.println("Could not find job role input field");
        }
        
        // Clear and fill job location with human-like typing
        if (locationInput != null) {
            locationInput.clear();
            humanWait(500, 1000);
            typeHumanLike(driver, locationInput, jobLocation);
            humanWait(800, 1500);
        } else {
            System.out.println("Could not find job location input field");
        }
        
        // Find and click the search button with multiple attempts
        System.out.println("Looking for search button...");
        boolean searchSubmitted = false;
        
        // Method 1: Try finding and clicking the visible search button
        WebElement searchButton = findSearchButton(driver);
        if (searchButton != null) {
            try {
                System.out.println("Found search button. Moving mouse to it...");
                moveMouseLikeHuman(driver, searchButton);
                humanWait(300, 800);
                
                // Take screenshot before clicking
                takeScreenshot(driver, "before_search_button_click.png");
                
                // Click with both standard and JavaScript methods for reliability
                try {
                    searchButton.click();
                    System.out.println("Clicked search button via standard click");
                } catch (Exception e) {
                    System.out.println("Standard click failed: " + e.getMessage() + ". Trying JavaScript click...");
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    js.executeScript("arguments[0].click();", searchButton);
                    System.out.println("Clicked search button via JavaScript");
                }
                
                searchSubmitted = true;
            } catch (Exception e) {
                System.out.println("Error clicking search button: " + e.getMessage());
            }
        }
        
        // Method 2: If button click didn't work, try finding the form and submitting it
        if (!searchSubmitted) {
            try {
                System.out.println("Trying to submit the search form...");
                JavascriptExecutor js = (JavascriptExecutor) driver;
                Object result = js.executeScript(
                    "const form = document.querySelector('form.jobs-search-box') || " +
                    "document.querySelector('form[role="+"\"search\""+"]'); " +
                    "if (form) { form.submit(); return true; } else { return false; }"
                );
                
                if (result instanceof Boolean && (Boolean) result) {
                    System.out.println("Submitted search form via JavaScript");
                    searchSubmitted = true;
                }
            } catch (Exception e) {
                System.out.println("Error submitting form: " + e.getMessage());
            }
        }
        
        // Method 3: If neither worked, press Enter on the location field
        if (!searchSubmitted && locationInput != null) {
            try {
                System.out.println("Trying to press Enter on location field...");
                locationInput.sendKeys(Keys.ENTER);
                System.out.println("Pressed Enter on location field");
                searchSubmitted = true;
            } catch (Exception e) {
                System.out.println("Error pressing Enter on location field: " + e.getMessage());
            }
        }
        
        // Wait for search results to load if any method worked
        if (searchSubmitted) {
            System.out.println("Search submitted. Waiting for results to load...");
            humanWait(3000, 5000);
            takeScreenshot(driver, "after_job_search.png");
            
            // Apply filters to search results
            applyJobFilters(driver);
        } else {
            System.out.println("WARNING: All search submission methods failed");
            takeScreenshot(driver, "search_submission_failed.png");
        }
    }
    
    /**
     * Apply job filters on the search results page based on configuration settings
     * Instead of applying filters through UI (which causes filters to disappear),
     * this method constructs a LinkedIn search URL with all filter parameters directly
     * 
     * @param driver WebDriver instance
     * @throws Exception If any error occurs
     */
    private static void applyJobFilters(WebDriver driver) throws Exception {
        System.out.println("Applying job filters via direct URL parameters...");
        
        // Get filter options from configuration
        String sortBy = LinkedInConfigLoader.getSortByOption();
        String jobType = LinkedInConfigLoader.getJobTypeOption();
        String expLevel = LinkedInConfigLoader.getExperienceLevelOption();
        boolean easyApply = LinkedInConfigLoader.isEasyApplyEnabled();
        String remoteOption = LinkedInConfigLoader.getRemoteFilterOption();
        
        // Take a screenshot before applying filters
        takeScreenshot(driver, "before_filters.png");
        
        // Get the current URL as a base
        String currentUrl = driver.getCurrentUrl();
        System.out.println("Current URL: " + currentUrl);
        
        // Check if we're on a LinkedIn search results page
        if (!currentUrl.contains("linkedin.com/jobs/")) {
            System.out.println("Not on LinkedIn jobs search page. URL: " + currentUrl);
            return;
        }
        
        // Start building the filtered URL
        StringBuilder urlBuilder = new StringBuilder();
        
        // Extract base URL (before any ?) and query parameters
        int queryParamIndex = currentUrl.indexOf('?');
        String baseUrl;
        
        if (queryParamIndex >= 0) {
            baseUrl = currentUrl.substring(0, queryParamIndex);
        } else {
            baseUrl = currentUrl;
        }
        
        urlBuilder.append(baseUrl).append('?');
        
        // Add filter parameters
        List<String> params = new ArrayList<>();
        
        // 1. Add f_AL (Easy Apply) parameter if enabled
        if (easyApply) {
            params.add("f_AL=true");
        }
        
        // 2. Add f_WT (remote/onsite/hybrid) parameter if specified
        if (remoteOption != null && !remoteOption.isEmpty()) {
            // Map property file values to LinkedIn URL parameter values
            String remoteParam;
            switch (remoteOption.toLowerCase()) {
                case "remote":
                    remoteParam = "2"; // LinkedIn's code for remote
                    break;
                case "onsite":
                    remoteParam = "1"; // LinkedIn's code for onsite
                    break;
                case "hybrid":
                    remoteParam = "3"; // LinkedIn's code for hybrid
                    break;
                default:
                    remoteParam = "2"; // Default to remote
            }
            params.add("f_WT=" + remoteParam);
        }
        
        // 3. Add f_TPR (date posted) parameter if specified
        if (sortBy != null && !sortBy.isEmpty()) {
            String dateParam;
            switch (sortBy.toLowerCase()) {
                case "day":
                    dateParam = "r86400"; // Past 24 hours
                    break;
                case "week":
                    dateParam = "r604800"; // Past week
                    break;
                case "month":
                    dateParam = "r2592000"; // Past month
                    break;
                default:
                    dateParam = "r604800"; // Default to past week
            }
            params.add("f_TPR=" + dateParam);
        }
        
        // 4. Add f_E (experience level) parameter if specified
        if (expLevel != null && !expLevel.isEmpty()) {
            String expParam;
            switch (expLevel.toLowerCase()) {
                case "internship":
                    expParam = "1"; // Internship
                    break;
                case "entry_level":
                    expParam = "2"; // Entry level
                    break;
                case "associate":
                    expParam = "3"; // Associate
                    break;
                case "mid_senior_level":
                    expParam = "4"; // Mid-Senior level
                    break;
                case "director":
                    expParam = "5"; // Director
                    break;
                default:
                    expParam = "2,3,4"; // Default to multiple levels
            }
            params.add("f_E=" + expParam);
        }
        
        // 5. Add f_JT (job type) parameter if specified
        if (jobType != null && !jobType.isEmpty()) {
            String typeParam;
            switch (jobType.toLowerCase()) {
                case "full_time":
                    typeParam = "F"; // Full time
                    break;
                case "part_time":
                    typeParam = "P"; // Part time
                    break;
                case "contract":
                    typeParam = "C"; // Contract
                    break;
                case "temporary":
                    typeParam = "T"; // Temporary
                    break;
                case "volunteer":
                    typeParam = "V"; // Volunteer
                    break;
                default:
                    typeParam = "F"; // Default to full time
            }
            params.add("f_JT=" + typeParam);
        }
        
        // Preserve any job role and location from the original URL
        if (queryParamIndex >= 0) {
            String queryParams = currentUrl.substring(queryParamIndex + 1);
            String[] pairs = queryParams.split("&");
            for (String pair : pairs) {
                if (pair.startsWith("keywords=") || pair.startsWith("location=") || 
                    pair.startsWith("geoId=") || pair.startsWith("position=")) {
                    params.add(pair);
                }
            }
        }
        
        // Sort parameters for deterministic URL construction
        Collections.sort(params);
        
        // Join all parameters with &
        urlBuilder.append(String.join("&", params));
        
        // Add any other necessary parameters
        urlBuilder.append("&sortBy=R"); // Sort by relevance
        
        // Construct the final URL
        String filteredUrl = urlBuilder.toString();
        System.out.println("Navigating to filtered URL: " + filteredUrl);
        
        // Navigate to the filtered URL
        driver.navigate().to(filteredUrl);
        
        // Wait for page to load
        humanWait(3000, 5000);
        
        // Take a screenshot after applying all filters
        takeScreenshot(driver, "after_filters_applied.png");
        
        System.out.println("All job filters applied successfully via URL parameters");
    }
    
    /**
     * Apply Easy Apply filter on the search results page
     * 
     * @param driver WebDriver instance
     * @throws Exception If any error occurs
     */
    private static void applyEasyApplyFilter(WebDriver driver) throws Exception {
        System.out.println("Applying Easy Apply filter...");
        
        // Try different selectors for the Easy Apply filter
        List<By> easyApplySelectors = Arrays.asList(
            By.xpath("//button[contains(., 'Easy Apply')]"),
            By.xpath("//label[contains(., 'Easy Apply')]/ancestor::button"),
            By.xpath("//span[text()='Easy Apply']/ancestor::button"),
            By.xpath("//div[contains(@class, 'filter')][contains(., 'Easy Apply')]"),
            By.xpath("//div[contains(@class, 'filter-button')][contains(., 'Easy Apply')]"),
            By.xpath("//button[@aria-label='Easy Apply filter']"),
            By.xpath("//button[contains(@id, 'easy-apply-filter')]"),
            By.cssSelector("button.easy-apply-filter")
        );
        
        // Try to find and click the Easy Apply filter
        clickFilterOption(driver, easyApplySelectors, "Easy Apply");
    }
    
    /**
     * Apply Remote filter on the search results page with support for multiple options
     * 
     * @param driver WebDriver instance
     * @param option Remote option (remote, onsite, hybrid)
     * @throws Exception If any error occurs
     */
    private static void applyRemoteFilter(WebDriver driver, String option) throws Exception {
        System.out.println("Applying Remote filter with option: " + option + "...");
        
        // Map the property file values to the actual UI options
        String uiOption = option.toLowerCase();
        if (uiOption.equals("onsite")) {
            uiOption = "On-site";
        } else if (uiOption.equals("remote")) {
            uiOption = "Remote";
        } else if (uiOption.equals("hybrid")) {
            uiOption = "Hybrid";
        } else {
            System.out.println("Unknown remote option: " + option + ". Using 'Remote' as default.");
            uiOption = "Remote";
        }
        
        // Try different selectors for the Remote filter dropdown
        List<By> remoteDropdownSelectors = Arrays.asList(
            By.xpath("//button[contains(., 'Remote')]"),
            By.xpath("//label[contains(., 'Remote')]/ancestor::button"),
            By.xpath("//span[text()='Remote']/ancestor::button"),
            By.xpath("//div[contains(@class, 'filter')][contains(., 'Remote')]"),
            By.xpath("//div[contains(@class, 'filter-button')][contains(., 'Remote')]"),
            By.xpath("//button[@aria-label='Remote filter']"),
            By.xpath("//button[contains(@id, 'remote-filter')]"),
            By.cssSelector("button.remote-filter")
        );
        
        // Try to find and click the Remote filter dropdown
        WebElement dropdown = findAndClickFilterDropdown(driver, remoteDropdownSelectors, "Remote");
        
        if (dropdown != null) {
            // Wait for the dropdown menu to appear
            humanWait(500, 1000);
            
            // Try to click the specific option
            List<By> optionSelectors = Arrays.asList(
                By.xpath("//label[contains(., '" + uiOption + "')]"),
                By.xpath("//span[contains(text(), '" + uiOption + "')]/ancestor::label"),
                By.xpath("//div[contains(@class, 'dropdown-option')][contains(., '" + uiOption + "')]"),
                By.xpath("//input[@type='checkbox']/following-sibling::*[contains(text(), '" + uiOption + "')]/ancestor::label"),
                By.xpath("//div[@role='checkbox'][contains(., '" + uiOption + "')]"),
                By.xpath("//div[@role='radio'][contains(., '" + uiOption + "')]")
            );
            
            boolean clicked = clickFilterOption(driver, optionSelectors, uiOption);
            
            if (clicked) {
                System.out.println("Selected " + uiOption + " option successfully");
                
                // Look for and click the Show results or Apply button
                List<By> showResultsSelectors = Arrays.asList(
                    By.xpath("//button[contains(., 'Show result')]"),
                    By.xpath("//button[contains(., 'Apply')]"),
                    By.xpath("//div[@role='dialog']//button[contains(., 'Show')]"),
                    By.xpath("//div[@role='dialog']//button[contains(., 'Apply')]"),
                    By.cssSelector("button.apply-button"),
                    By.cssSelector("button.results-button")
                );
                
                boolean resultsClicked = clickFilterOption(driver, showResultsSelectors, "Show results");
                if (!resultsClicked) {
                    System.out.println("Could not find Show results button. Filter may apply automatically.");
                }
            } else {
                System.out.println("Could not select " + uiOption + " option");
            }
        } else {
            System.out.println("Could not find Remote filter dropdown");
        }
        
        // Wait for filters to apply
        humanWait(1000, 2000);
    }
    
    /**
     * Apply Date Posted filter on the search results page
     * 
     * @param driver WebDriver instance
     * @param option Date option (day, week, month)
     * @throws Exception If any error occurs
     */
    private static void applyDatePostedFilter(WebDriver driver, String option) throws Exception {
        System.out.println("Applying Date Posted filter: " + option);
        
        // Map option to display text
        String displayText;
        switch (option.toLowerCase()) {
            case "day":
                displayText = "Past 24 hours";
                break;
            case "week":
                displayText = "Past week";
                break;
            case "month":
                displayText = "Past month";
                break;
            default:
                System.out.println("Invalid date posted option: " + option);
                return;
        }
        
        // First, find and click the Date Posted dropdown
        List<By> datePostedSelectors = Arrays.asList(
            By.xpath("//button[contains(., 'Date posted')]"),
            By.xpath("//label[contains(., 'Date posted')]/ancestor::button"),
            By.xpath("//span[text()='Date posted']/ancestor::button"),
            By.xpath("//div[contains(@class, 'filter')][contains(., 'Date posted')]"),
            By.xpath("//button[@aria-label='Date posted filter']"),
            By.xpath("//button[contains(@id, 'date-posted-filter')]"),
            By.cssSelector("button.date-posted-filter")
        );
        
        // Click the dropdown
        WebElement datePostedDropdown = findAndClickFilterDropdown(driver, datePostedSelectors, "Date posted");
        if (datePostedDropdown == null) {
            System.out.println("Could not find Date posted dropdown");
            return;
        }
        
        // Wait for dropdown to appear
        humanWait(1000, 2000);
        
        // Find and click the option
        List<By> optionSelectors = Arrays.asList(
            By.xpath("//label[contains(., '" + displayText + "')]"),
            By.xpath("//span[contains(text(), '" + displayText + "')]/ancestor::label"),
            By.xpath("//div[contains(@role, 'radio')][contains(., '" + displayText + "')]"),
            By.xpath("//div[contains(@class, 'dropdown-option')][contains(., '" + displayText + "')]")
        );
        
        clickFilterOption(driver, optionSelectors, displayText);
        
        // Wait and click Apply button if needed
        applyFilterIfNeeded(driver);
    }
    
    /**
     * Apply Experience Level filter on the search results page
     * 
     * @param driver WebDriver instance
     * @param option Experience level (internship, entry_level, associate, mid_senior_level, director)
     * @throws Exception If any error occurs
     */
    private static void applyExperienceLevelFilter(WebDriver driver, String option) throws Exception {
        System.out.println("Applying Experience Level filter: " + option);
        
        // Map option to display text
        String displayText;
        switch (option.toLowerCase()) {
            case "internship":
                displayText = "Internship";
                break;
            case "entry_level":
                displayText = "Entry level";
                break;
            case "associate":
                displayText = "Associate";
                break;
            case "mid_senior_level":
                displayText = "Mid-Senior level";
                break;
            case "director":
                displayText = "Director";
                break;
            default:
                System.out.println("Invalid experience level option: " + option);
                return;
        }
        
        // First, find and click the Experience Level dropdown
        List<By> experienceLevelSelectors = Arrays.asList(
            By.xpath("//button[contains(., 'Experience level')]"),
            By.xpath("//label[contains(., 'Experience level')]/ancestor::button"),
            By.xpath("//span[text()='Experience level']/ancestor::button"),
            By.xpath("//div[contains(@class, 'filter')][contains(., 'Experience level')]"),
            By.xpath("//button[@aria-label='Experience level filter']"),
            By.xpath("//button[contains(@id, 'experience-level-filter')]"),
            By.cssSelector("button.experience-level-filter")
        );
        
        // Click the dropdown
        WebElement experienceLevelDropdown = findAndClickFilterDropdown(driver, experienceLevelSelectors, "Experience level");
        if (experienceLevelDropdown == null) {
            System.out.println("Could not find Experience level dropdown");
            return;
        }
        
        // Wait for dropdown to appear
        humanWait(1000, 2000);
        
        // Find and click the option
        List<By> optionSelectors = Arrays.asList(
            By.xpath("//label[contains(., '" + displayText + "')]"),
            By.xpath("//span[contains(text(), '" + displayText + "')]/ancestor::label"),
            By.xpath("//div[contains(@role, 'checkbox')][contains(., '" + displayText + "')]"),
            By.xpath("//div[contains(@class, 'dropdown-option')][contains(., '" + displayText + "')]")
        );
        
        clickFilterOption(driver, optionSelectors, displayText);
        
        // Wait and click Apply button if needed
        applyFilterIfNeeded(driver);
    }
    
    /**
     * Apply Job Type filter on the search results page
     * 
     * @param driver WebDriver instance
     * @param option Job type (temporary, contract, volunteer, full_time, part_time)
     * @throws Exception If any error occurs
     */
    private static void applyJobTypeFilter(WebDriver driver, String option) throws Exception {
        System.out.println("Applying Job Type filter: " + option);
        
        // Map option to display text
        String displayText;
        switch (option.toLowerCase()) {
            case "full_time":
                displayText = "Full-time";
                break;
            case "part_time":
                displayText = "Part-time";
                break;
            case "contract":
                displayText = "Contract";
                break;
            case "temporary":
                displayText = "Temporary";
                break;
            case "volunteer":
                displayText = "Volunteer";
                break;
            default:
                System.out.println("Invalid job type option: " + option);
                return;
        }
        
        // First, find and click the Job Type dropdown
        List<By> jobTypeSelectors = Arrays.asList(
            By.xpath("//button[contains(., 'Job type')]"),
            By.xpath("//label[contains(., 'Job type')]/ancestor::button"),
            By.xpath("//span[text()='Job type']/ancestor::button"),
            By.xpath("//div[contains(@class, 'filter')][contains(., 'Job type')]"),
            By.xpath("//button[@aria-label='Job type filter']"),
            By.xpath("//button[contains(@id, 'job-type-filter')]"),
            By.cssSelector("button.job-type-filter")
        );
        
        // Click the dropdown
        WebElement jobTypeDropdown = findAndClickFilterDropdown(driver, jobTypeSelectors, "Job type");
        if (jobTypeDropdown == null) {
            System.out.println("Could not find Job type dropdown");
            return;
        }
        
        // Wait for dropdown to appear
        humanWait(1000, 2000);
        
        // Find and click the option
        List<By> optionSelectors = Arrays.asList(
            By.xpath("//label[contains(., '" + displayText + "')]"),
            By.xpath("//span[contains(text(), '" + displayText + "')]/ancestor::label"),
            By.xpath("//div[contains(@role, 'checkbox')][contains(., '" + displayText + "')]"),
            By.xpath("//div[contains(@class, 'dropdown-option')][contains(., '" + displayText + "')]")
        );
        
        clickFilterOption(driver, optionSelectors, displayText);
        
        // Wait and click Apply button if needed
        applyFilterIfNeeded(driver);
    }
    
    /**
     * Finds a search input field using various selectors
     * 
     * @param driver WebDriver instance
     * @param fieldType Type of field ("role" or "location")
     * @param selectors List of possible selectors
     * @return WebElement if found, null otherwise
     */
    private static WebElement findSearchInputField(WebDriver driver, String fieldType, List<By> selectors) {
        System.out.println("Looking for " + fieldType + " input field...");
        
        for (By selector : selectors) {
            try {
                List<WebElement> elements = driver.findElements(selector);
                for (WebElement element : elements) {
                    if (element.isDisplayed() && element.isEnabled()) {
                        System.out.println("Found " + fieldType + " input field");
                        return element;
                    }
                }
            } catch (Exception e) {
                // Continue to the next selector
            }
        }
        
        // Try a JavaScript approach as a fallback
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Object result = null;
            
            if (fieldType.equals("role")) {
                result = js.executeScript(
                    "return document.querySelector('input[placeholder*="+"\"job title\""+"]') || " +
                    "document.querySelector('input[aria-label*="+"\"title\""+"]') || " +
                    "document.querySelector('form.jobs-search-box input[type="+"\"text\""+"]:first-child');"
                );
            } else if (fieldType.equals("location")) {
                result = js.executeScript(
                    "return document.querySelector('input[placeholder*="+"\"location\""+"]') || " +
                    "document.querySelector('input[aria-label*="+"\"location\""+"]') || " +
                    "document.querySelector('form.jobs-search-box input[type="+"\"text\""+"]:nth-child(2)');"
                );
            }
            
            if (result instanceof WebElement) {
                WebElement element = (WebElement) result;
                if (element.isDisplayed() && element.isEnabled()) {
                    System.out.println("Found " + fieldType + " input field using JavaScript");
                    return element;
                }
            }
        } catch (Exception e) {
            System.out.println("JavaScript approach to find " + fieldType + " input failed: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Finds the search button
     * 
     * @param driver WebDriver instance
     * @return WebElement if found, null otherwise
     */
    private static WebElement findSearchButton(WebDriver driver) {
        List<By> selectors = Arrays.asList(
            By.xpath("//button[contains(@class, 'jobs-search-box__submit')]"),
            By.xpath("//button[contains(@class, 'jobs-search-box__submit-button')]"),
            By.xpath("//button[contains(@aria-label, 'Search')]"),
            By.xpath("//form[contains(@class, 'jobs-search-box')]//button[contains(@type, 'submit')]"),
            By.xpath("//form[contains(@class, 'jobs-search-box')]//button[contains(., 'Search')]"),
            By.cssSelector(".jobs-search-box__submit-button")
        );
        
        for (By selector : selectors) {
            try {
                List<WebElement> elements = driver.findElements(selector);
                for (WebElement element : elements) {
                    if (element.isDisplayed() && element.isEnabled()) {
                        System.out.println("Found search button");
                        return element;
                    }
                }
            } catch (Exception e) {
                // Continue to the next selector
            }
        }
        
        // JavaScript fallback
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Object result = js.executeScript(
                "return document.querySelector('button[type="+"\"submit\""+"]') || " +
                "document.querySelector('button.jobs-search-box__submit') || " +
                "document.querySelector('button.jobs-search-box__submit-button');"
            );
            
            if (result instanceof WebElement) {
                WebElement element = (WebElement) result;
                if (element.isDisplayed() && element.isEnabled()) {
                    System.out.println("Found search button using JavaScript");
                    return element;
                }
            }
        } catch (Exception e) {
            System.out.println("JavaScript approach to find search button failed: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Types text in a human-like manner with variable speed and pauses,
     * but ensuring accuracy of the text entry
     * 
     * @param driver WebDriver instance
     * @param element Element to type into
     * @param text Text to type
     * @throws InterruptedException If interrupted during typing
     */
    private static void typeHumanLike(WebDriver driver, WebElement element, String text) throws InterruptedException {
        System.out.println("Typing \"" + text + "\" with human-like behavior");
        
        // First ensure the element is visible and clickable
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            wait.until(ExpectedConditions.elementToBeClickable(element));
        } catch (Exception e) {
            // Continue anyway if timeout
            System.out.println("Warning: Element may not be fully interactable: " + e.getMessage());
        }
        
        // Focus on the element first
        element.click();
        humanWait(200, 500);
        
        // Clear the field completely using JavaScript for reliability
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].value = '';", element);
            humanWait(300, 700);
        } catch (Exception e) {
            // Fallback to standard clear if JavaScript fails
            element.clear();
            humanWait(300, 700);
        }
        
        // METHOD 1: Type the whole string at once with a realistic delay
        // This is more reliable but still mimics human behavior with the wait time
        try {
            // Send the full text as a single action
            element.sendKeys(text);
            
            // Wait a realistic amount of time as if the user was typing
            int typingTime = calculateTypingTime(text);
            Thread.sleep(typingTime);
            
            // Verify what was typed
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String actualText = (String) js.executeScript("return arguments[0].value;", element);
            
            // If what was typed doesn't match what we wanted, try method 2
            if (!text.equals(actualText)) {
                System.out.println("Text entry mismatch. Trying character-by-character approach...");
                js.executeScript("arguments[0].value = '';", element);
                humanWait(300, 700);
                typeCharacterByCharacter(driver, element, text);
            }
        } catch (Exception e) {
            System.out.println("Error in bulk typing: " + e.getMessage() + ". Falling back to character-by-character.");
            element.clear();
            humanWait(300, 700);
            typeCharacterByCharacter(driver, element, text);
        }
        
        // Pause after typing as if reviewing what was typed
        humanWait(500, 1200);
    }
    
    /**
     * Calculate a realistic typing time based on text length and complexity
     * 
     * @param text Text to type
     * @return Milliseconds it would take a human to type this text
     */
    private static int calculateTypingTime(String text) {
        // Average human typing speed is about 40 WPM or ~200 characters per minute
        // That's about 300ms per character on average
        int baseTimePerChar = 300;
        return text.length() * baseTimePerChar + random.nextInt(1000); // Add some randomness
    }
    
    /**
     * Types text character by character with variable speed
     * This is a fallback method that's less likely to cause errors but may be slower
     * 
     * @param driver WebDriver instance
     * @param element Element to type into
     * @param text Text to type
     * @throws InterruptedException If interrupted during typing
     */
    private static void typeCharacterByCharacter(WebDriver driver, WebElement element, String text) throws InterruptedException {
        System.out.println("Using character-by-character typing for accuracy");
        
        // Focus the element again
        element.click();
        humanWait(200, 400);
        
        // Type one character at a time with verification
        StringBuilder enteredText = new StringBuilder();
        
        for (char c : text.toCharArray()) {
            // Send just this character
            element.sendKeys(Character.toString(c));
            enteredText.append(c);
            
            // Brief pause between keystrokes (70-150ms is realistic)
            Thread.sleep(70 + random.nextInt(80));
            
            // Occasionally pause longer as if thinking
            if (random.nextInt(20) == 0) {
                Thread.sleep(200 + random.nextInt(300));
            }
        }
    }
    
    /**
     * Specialized method to find and click the "Show all" button in the LinkedIn Jobs page
     * This method uses multiple strategies to find the button:
     * 1. Modern UI selectors
     * 2. Progressive scrolling to reveal lazy-loaded elements
     * 3. JavaScript execution to find buttons with relevant text
     * 
     * @param driver WebDriver instance
     * @return true if the button was found and clicked, false otherwise
     * @throws Exception If any error occurs
     */
    private static boolean findAndClickShowAllButton(WebDriver driver) throws Exception {
        System.out.println("Starting advanced search for 'Show all' button...");
        
        // Take a screenshot before attempting to find the button
        takeScreenshot(driver, "before_finding_show_all.png");
        
        // Strategy 1: Try various CSS and XPath selectors
        List<By> showAllSelectors = Arrays.asList(
            // Top job picks section selectors (based on the screenshot)
            By.xpath("//div[contains(., 'Top job picks for you')]//a[contains(text(), 'Show all')]"),
            By.xpath("//div[contains(., 'Top job picks for you')]//a[contains(., 'Show all')]"),
            By.xpath("//a[contains(text(), 'Show all') and contains(@aria-label, 'job')]"),
            By.xpath("//a[text()='Show all →']"),
            By.xpath("//a[contains(., 'Show all') and contains(., '→')]"),
            By.cssSelector("a[aria-label*='Show all jobs']"),

            // Generic link selectors (for "Show all" links rather than buttons)
            By.xpath("//a[contains(text(), 'Show all')]"),
            By.xpath("//a[normalize-space()='Show all']"),
            By.xpath("//a[contains(., 'Show all →')]"),
            
            // Common text-based selectors for buttons
            By.xpath("//button[contains(text(), 'Show all')]"),
            By.xpath("//button[normalize-space()='Show all']"),
            By.xpath("//span[text()='Show all']/parent::button"),
            By.xpath("//span[contains(text(), 'Show all')]/parent::button"),
            
            // CSS class-based selectors (assuming LinkedIn uses specific classes)
            By.cssSelector("button.show-all-button"),
            By.cssSelector("button.jobs-show-all-button"),
            By.cssSelector("button.jobs-s-see-more-button"),
            By.cssSelector("a.show-all-link"),
            By.cssSelector("a.jobs-show-all-link"),
            
            // Attribute-based selectors
            By.xpath("//button[@data-control-name='show_more_jobs']"),
            By.xpath("//button[@data-control-name='all_jobs']"),
            By.xpath("//a[@data-control-name='show_more_jobs']"),
            By.xpath("//a[@data-control-name='all_jobs']"),
            
            // Contextual selectors based on surrounding elements
            By.xpath("//div[contains(@class, 'jobs-search-results')]//*[contains(text(), 'Show all')]/ancestor::button"),
            By.xpath("//section[contains(@class, 'jobs-search')]//button[contains(., 'Show all')]"),
            By.xpath("//div[contains(@class, 'jobs-home-top-card')]//*[contains(text(), 'Show all')]"),
            
            // Modern UI selectors (LinkedIn frequently updates their UI)
            By.xpath("//div[contains(@class, 'jobs-search-results-list')]//button[contains(., 'Show all')]"),
            By.xpath("//div[contains(@class, 'jobs-search-two-pane')]//button[contains(., 'Show all')]"),
            
            // Button with an aria-label
            By.xpath("//button[@aria-label='Show all jobs']"),
            By.xpath("//button[@aria-label='See more jobs']"),
            By.xpath("//a[@aria-label='Show all jobs']"),
            By.xpath("//a[@aria-label='See more jobs']"),
            
            // Generic "see more" or "show more" buttons that might be used instead
            By.xpath("//button[contains(text(), 'See more')]"),
            By.xpath("//button[contains(text(), 'Show more')]"),
            By.xpath("//a[contains(text(), 'See more')]"),
            By.xpath("//a[contains(text(), 'Show more')]")
        );
        
        // Try each selector with a short timeout
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
        
        for (By selector : showAllSelectors) {
            try {
                WebElement button = shortWait.until(ExpectedConditions.elementToBeClickable(selector));
                System.out.println("Found 'Show all' button with selector: " + selector);
                
                // Move to the button like a human
                moveMouseLikeHuman(driver, button);
                
                // Click the button
                button.click();
                
                // Wait for the results to load
                humanWait(1500, 3000);
                
                // Take a screenshot after clicking
                takeScreenshot(driver, "after_show_all_click.png");
                
                return true;
            } catch (Exception e) {
                // Continue to the next selector
            }
        }
        
        // Strategy 2: Progressive scrolling to reveal lazy-loaded elements
        System.out.println("Initial selectors did not work. Trying progressive scrolling approach...");
        
        for (int scrollAttempt = 0; scrollAttempt < 5; scrollAttempt++) {
            // Scroll down incrementally
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.scrollBy(0, " + (300 + random.nextInt(200)) + ")");
            
            humanWait(800, 1500);
            
            // After scrolling, try the selectors again
            for (By selector : showAllSelectors) {
                try {
                    List<WebElement> elements = driver.findElements(selector);
                    if (!elements.isEmpty()) {
                        for (WebElement button : elements) {
                            if (button.isDisplayed() && button.isEnabled()) {
                                System.out.println("Found 'Show all' button after scrolling with selector: " + selector);
                                
                                // Move to the button like a human
                                moveMouseLikeHuman(driver, button);
                                
                                // Click the button
                                button.click();
                                
                                // Wait for the results to load
                                humanWait(1500, 3000);
                                
                                // Take a screenshot after clicking
                                takeScreenshot(driver, "after_show_all_click_with_scroll.png");
                                
                                return true;
                            }
                        }
                    }
                } catch (Exception e) {
                    // Continue to the next selector
                }
            }
        }
        
        // Strategy 3: Use JavaScript to find buttons by text content
        System.out.println("Trying JavaScript approach to find 'Show all' button...");
        
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // Execute JavaScript to find buttons containing "Show all" text
            Object result = js.executeScript(
                "return Array.from(document.querySelectorAll('button')).filter(btn => " +
                "btn.innerText.includes('Show all') || " +
                "btn.innerText.includes('Show more') || " +
                "btn.innerText.includes('See more') || " +
                "btn.textContent.includes('Show all') || " +
                "btn.textContent.includes('Show more') || " +
                "btn.textContent.includes('See more'));"
            );
            
            if (result != null) {
                @SuppressWarnings("unchecked")
                List<WebElement> buttons = (List<WebElement>) result;
                
                if (!buttons.isEmpty()) {
                    WebElement showAllButton = buttons.get(0);
                    System.out.println("Found 'Show all' button via JavaScript");
                    
                    // Click using JavaScript (more reliable for hidden/partially visible elements)
                    js.executeScript("arguments[0].click();", showAllButton);
                    
                    // Wait for the results to load
                    humanWait(1500, 3000);
                    
                    // Take a screenshot after clicking
                    takeScreenshot(driver, "after_show_all_click_js.png");
                    
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println("JavaScript approach failed: " + e.getMessage());
        }
        
        // Strategy 4: Try clicking on any button in the jobs section as a last resort
        System.out.println("Trying to find any job-related button as a last resort...");
        try {
            List<WebElement> allButtons = driver.findElements(By.xpath(
                "//div[contains(@class, 'jobs') or contains(@id, 'jobs')]//button"));
            
            for (WebElement button : allButtons) {
                if (button.isDisplayed() && button.isEnabled()) {
                    String buttonText = button.getText().toLowerCase();
                    if (buttonText.contains("show") || buttonText.contains("see") || buttonText.contains("more") || buttonText.contains("all")) {
                        System.out.println("Found a potential 'Show all' button alternative: " + buttonText);
                        
                        // Move to the button like a human
                        moveMouseLikeHuman(driver, button);
                        
                        // Click the button
                        button.click();
                        
                        // Wait for the results to load
                        humanWait(1500, 3000);
                        
                        // Take a screenshot after clicking
                        takeScreenshot(driver, "after_alternative_button_click.png");
                        
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Last resort approach failed: " + e.getMessage());
        }
        
        System.out.println("All approaches to find 'Show all' button failed.");
        takeScreenshot(driver, "show_all_not_found.png");
        return false;
    }
    
    /**
     * Moves the mouse in a human-like manner to the target element
     * @param driver WebDriver instance
     * @param element Target element to move to
     * @throws Exception If any errors occur
     */
    private static void moveMouseLikeHuman(WebDriver driver, WebElement element) throws Exception {
        try {
            // Create an Actions instance for mouse movement
            Actions actions = new Actions(driver);
            
            // Get the location of the element
            int targetX = element.getLocation().getX();
            int targetY = element.getLocation().getY();
            
            // Add some randomness to the target (not exactly center of element)
            targetX += element.getSize().getWidth() / 2 + (random.nextInt(10) - 5);
            targetY += element.getSize().getHeight() / 2 + (random.nextInt(10) - 5);
            
            // Move in a slightly curved path instead of a straight line
            for (int i = 0; i < 3; i++) {
                int intermediateX = targetX + (random.nextInt(40) - 20);
                int intermediateY = targetY + (random.nextInt(40) - 20);
                
                actions.moveByOffset(intermediateX, intermediateY).pause(Duration.ofMillis(random.nextInt(100) + 50)).perform();
            }
            
            // Finally move to the actual target
            actions.moveToElement(element).pause(Duration.ofMillis(random.nextInt(300) + 200)).perform();
            
            // Small pause before clicking
            humanWait(200, 500);
            
        } catch (Exception e) {
            System.out.println("Error during mouse movement: " + e.getMessage());
            // Fall back to JavaScript hover if actions fail
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
            humanWait(300, 700);
        }
    }
    
    /**
     * Simulates human-like scrolling down the page
     * @param driver WebDriver instance
     * @throws Exception If any error occurs
     */
    private static void humanScrollDown(WebDriver driver) throws Exception {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        
        // Get the height of the visible window
        Long windowHeight = (Long) js.executeScript("return window.innerHeight");
        
        // Calculate a random number of scroll steps (between 2 and 5)
        int scrollSteps = 2 + random.nextInt(4);
        
        for (int i = 0; i < scrollSteps; i++) {
            // Calculate a random scroll distance for this step
            int scrollDistance = windowHeight.intValue() / 3 + random.nextInt(windowHeight.intValue() / 3);
            
            // Scroll down smoothly
            js.executeScript("window.scrollBy({top: " + scrollDistance + ", behavior: 'smooth'});");
            
            // Pause between scrolls like a human would
            humanWait(800, 1500);
        }
    }
    
    /**
     * Helper method to find and click a filter dropdown button
     * 
     * @param driver WebDriver instance
     * @param selectors List of possible selectors to try
     * @param filterName Name of the filter for logging
     * @return The WebElement that was clicked, or null if none was found
     * @throws Exception If any error occurs
     */
    private static WebElement findAndClickFilterDropdown(WebDriver driver, List<By> selectors, String filterName) throws Exception {
        System.out.println("Looking for " + filterName + " dropdown...");
        
        // Try each selector until we find one that works
        for (By selector : selectors) {
            try {
                List<WebElement> elements = driver.findElements(selector);
                for (WebElement element : elements) {
                    if (element.isDisplayed() && element.isEnabled()) {
                        // Take screenshot before clicking
                        takeScreenshot(driver, filterName.replaceAll("\\s+", "_").toLowerCase() + "_dropdown_before_click.png");
                        
                        // Move mouse to element and click
                        System.out.println("Found " + filterName + " dropdown. Moving mouse to it...");
                        moveMouseLikeHuman(driver, element);
                        humanWait(300, 800);
                        
                        try {
                            element.click();
                            System.out.println("Clicked " + filterName + " dropdown using standard click");
                        } catch (Exception e) {
                            System.out.println("Standard click failed for " + filterName + ". Trying JavaScript click...");
                            JavascriptExecutor js = (JavascriptExecutor) driver;
                            js.executeScript("arguments[0].click();", element);
                            System.out.println("Clicked " + filterName + " dropdown using JavaScript");
                        }
                        
                        humanWait(500, 1000);
                        takeScreenshot(driver, filterName.replaceAll("\\s+", "_").toLowerCase() + "_dropdown_after_click.png");
                        
                        return element;
                    }
                }
            } catch (Exception e) {
                // Continue to the next selector
            }
        }
        
        // Try JavaScript to find buttons by text content
        try {
            System.out.println("Trying JavaScript approach to find " + filterName + " dropdown...");
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            String jsCode = "return Array.from(document.querySelectorAll('button,div[role="+"\"button\""+"]'))." +
                          "filter(el => el.textContent.includes('" + filterName + "') && " +
                          "getComputedStyle(el).display !== 'none' && " +
                          "getComputedStyle(el).visibility !== 'hidden')[0];"; 
            
            Object result = js.executeScript(jsCode);
            
            if (result instanceof WebElement) {
                WebElement element = (WebElement) result;
                
                // Take screenshot before clicking
                takeScreenshot(driver, filterName.replaceAll("\\s+", "_").toLowerCase() + "_dropdown_js_before_click.png");
                
                // Click using JavaScript
                js.executeScript("arguments[0].click();", element);
                System.out.println("Found and clicked " + filterName + " dropdown using JavaScript");
                
                humanWait(500, 1000);
                takeScreenshot(driver, filterName.replaceAll("\\s+", "_").toLowerCase() + "_dropdown_js_after_click.png");
                
                return element;
            }
        } catch (Exception e) {
            System.out.println("JavaScript approach failed for " + filterName + ": " + e.getMessage());
        }
        
        System.out.println("Could not find " + filterName + " dropdown");
        return null;
    }
    
    /**
     * Helper method to click a filter option
     * 
     * @param driver WebDriver instance
     * @param selectors List of possible selectors to try
     * @param optionName Name of the option for logging
     * @return true if option was clicked, false otherwise
     * @throws Exception If any error occurs
     */
    private static boolean clickFilterOption(WebDriver driver, List<By> selectors, String optionName) throws Exception {
        System.out.println("Looking for " + optionName + " option...");
        
        // Try each selector until we find one that works
        for (By selector : selectors) {
            try {
                List<WebElement> elements = driver.findElements(selector);
                for (WebElement element : elements) {
                    if (element.isDisplayed() && element.isEnabled()) {
                        // Take screenshot before clicking
                        takeScreenshot(driver, optionName.replaceAll("\\s+", "_").toLowerCase() + "_option_before_click.png");
                        
                        // Move mouse to element and click
                        System.out.println("Found " + optionName + " option. Moving mouse to it...");
                        moveMouseLikeHuman(driver, element);
                        humanWait(300, 800);
                        
                        try {
                            element.click();
                            System.out.println("Clicked " + optionName + " option using standard click");
                        } catch (Exception e) {
                            System.out.println("Standard click failed for " + optionName + ". Trying JavaScript click...");
                            JavascriptExecutor js = (JavascriptExecutor) driver;
                            js.executeScript("arguments[0].click();", element);
                            System.out.println("Clicked " + optionName + " option using JavaScript");
                        }
                        
                        humanWait(500, 1000);
                        takeScreenshot(driver, optionName.replaceAll("\\s+", "_").toLowerCase() + "_option_after_click.png");
                        
                        return true;
                    }
                }
            } catch (Exception e) {
                // Continue to the next selector
            }
        }
        
        // Try JavaScript to find elements by text content
        try {
            System.out.println("Trying JavaScript approach to find " + optionName + " option...");
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            String jsCode = "return Array.from(document.querySelectorAll('label,div[role="+"\"checkbox\""+"],div[role="+"\"radio\""+"],span,div'))." +
                          "filter(el => el.textContent.includes('" + optionName + "') && " +
                          "getComputedStyle(el).display !== 'none' && " +
                          "getComputedStyle(el).visibility !== 'hidden')[0];"; 
            
            Object result = js.executeScript(jsCode);
            
            if (result instanceof WebElement) {
                WebElement element = (WebElement) result;
                
                // Take screenshot before clicking
                takeScreenshot(driver, optionName.replaceAll("\\s+", "_").toLowerCase() + "_option_js_before_click.png");
                
                // Click using JavaScript
                js.executeScript("arguments[0].click();", element);
                System.out.println("Found and clicked " + optionName + " option using JavaScript");
                
                humanWait(500, 1000);
                takeScreenshot(driver, optionName.replaceAll("\\s+", "_").toLowerCase() + "_option_js_after_click.png");
                
                return true;
            }
        } catch (Exception e) {
            System.out.println("JavaScript approach failed for " + optionName + ": " + e.getMessage());
        }
        
        System.out.println("Could not find " + optionName + " option");
        return false;
    }
    
    /**
     * Helper method to click the Apply button if needed after selecting filter options
     * 
     * @param driver WebDriver instance
     * @throws Exception If any error occurs
     */
    private static void applyFilterIfNeeded(WebDriver driver) throws Exception {
        System.out.println("Looking for Apply button...");
        
        // Try different selectors for the Apply button
        List<By> applyButtonSelectors = Arrays.asList(
            By.xpath("//button[contains(., 'Apply')]"),
            By.xpath("//button[text()='Apply']"),
            By.xpath("//div[@role='dialog']//button[contains(., 'Apply')]"),
            By.xpath("//div[@role='dialog']//button[contains(@id, 'apply')]"),
            By.xpath("//button[@data-control-name='filter_pill_apply']"),
            By.cssSelector("button.artdeco-button--primary")
        );
        
        // Wait a moment for any dialog to fully appear
        humanWait(500, 1000);
        
        // Try each selector until we find one that works
        boolean clicked = false;
        for (By selector : applyButtonSelectors) {
            try {
                List<WebElement> elements = driver.findElements(selector);
                for (WebElement element : elements) {
                    if (element.isDisplayed() && element.isEnabled()) {
                        // Take screenshot before clicking
                        takeScreenshot(driver, "apply_button_before_click.png");
                        
                        // Move mouse to element and click
                        System.out.println("Found Apply button. Moving mouse to it...");
                        moveMouseLikeHuman(driver, element);
                        humanWait(300, 800);
                        
                        try {
                            element.click();
                            System.out.println("Clicked Apply button using standard click");
                            clicked = true;
                        } catch (Exception e) {
                            System.out.println("Standard click failed for Apply button. Trying JavaScript click...");
                            JavascriptExecutor js = (JavascriptExecutor) driver;
                            js.executeScript("arguments[0].click();", element);
                            System.out.println("Clicked Apply button using JavaScript");
                            clicked = true;
                        }
                        
                        humanWait(500, 1000);
                        takeScreenshot(driver, "apply_button_after_click.png");
                        
                        break;
                    }
                }
                if (clicked) break;
            } catch (Exception e) {
                // Continue to the next selector
            }
        }
        
        // If we didn't find an Apply button, maybe it's not needed (some filters apply directly)
        if (!clicked) {
            System.out.println("Could not find Apply button. It might not be needed for this filter.");
        }
        
        // Wait for filters to apply and page to update
        humanWait(1000, 2000);
    }
    
    /**
     * Takes a screenshot and saves it to the specified filename
     * @param driver WebDriver instance
     * @param filename Name of the screenshot file
     */
    private static void takeScreenshot(WebDriver driver, String filename) {
        try {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path destination = Paths.get(filename);
            Files.copy(screenshot.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Screenshot saved to: " + destination.toAbsolutePath());
        } catch (Exception e) {
            System.out.println("Failed to save screenshot: " + e.getMessage());
        }
    }
    
    /**
     * Waits for a random time between min and max milliseconds
     * @param minMs Minimum wait time in milliseconds
     * @param maxMs Maximum wait time in milliseconds
     * @throws InterruptedException If the thread is interrupted
     */
    private static void humanWait(int minMs, int maxMs) throws InterruptedException {
        int waitTime = minMs + random.nextInt(maxMs - minMs);
        Thread.sleep(waitTime);
    }
    
    /**
     * Main method to demonstrate functionality
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        WebDriver driver = null;
        
        try {
            // Set up WebDriverManager for Chrome
            WebDriverManager.chromedriver().setup();
            
            // Configure Chrome options for stealth browsing
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--start-maximized");
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-popup-blocking");
            options.addArguments("--disable-infobars");
            
            // Additional stealth options
            Map<String, Object> prefs = new HashMap<>();
            prefs.put("credentials_enable_service", false);
            prefs.put("profile.password_manager_enabled", false);
            options.setExperimentalOption("prefs", prefs);
            options.setExperimentalOption("excludeSwitches", 
                    new String[]{"enable-automation", "enable-logging"});
            
            // Add a realistic user-agent
            options.addArguments("--user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36");
            
            // Initialize Chrome driver with stealth options
            driver = new ChromeDriver(options);
            
            // First go to LinkedIn login
            driver.get("https://www.linkedin.com/");
            System.out.println("LinkedIn opened. Please log in manually.");
            System.out.println("Waiting 30 seconds for manual login...");
            Thread.sleep(30000);
            
            // Check if logged in
            if (driver.getCurrentUrl().contains("feed") || 
                driver.getCurrentUrl().contains("mynetwork") || 
                !driver.findElements(By.xpath("//*[contains(@class, 'feed-identity-module')]")).isEmpty()) {
                
                System.out.println("Login detected. Proceeding to Jobs navigation.");
                // Navigate to Jobs
                navigateToJobs(driver);
            } else {
                System.out.println("Not logged in. Please run LinkedInOpener first or log in manually.");
            }
            
            // Keep browser open for a while
            System.out.println("Job navigation complete. Browser will stay open for 30 seconds.");
            Thread.sleep(30000);
            
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clean up
            if (driver != null) {
                driver.quit();
                System.out.println("Browser closed.");
            }
        }
    }
}

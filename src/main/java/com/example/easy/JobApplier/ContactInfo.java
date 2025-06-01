package com.example.easy.JobApplier;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.JavascriptExecutor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

/**
 * Handles the contact information section of LinkedIn's Easy Apply process.
 * This class allows automation of filling out contact information forms,
 * with capability to save and reuse previously entered information.
 */
public class ContactInfo {
    
    private static final String USER_INFO_FILE = "src/main/resources/user_info.properties";
    private static final Random random = new Random();
    private Properties userInfo;
    private final WebDriver driver;
    private Scanner scanner; // Single scanner instance for the class
    
    /**
     * Constructor initializes the contact info handler with a WebDriver instance
     * and loads any saved user information.
     * 
     * @param driver WebDriver instance for browser automation
     */
    public ContactInfo(WebDriver driver) {
        this.driver = driver;
        userInfo = loadUserInfo();
        this.scanner = new Scanner(System.in); // Initialize scanner
    }
    
    /**
     * Clicks on the first job in the search results and then 
     * clicks the Easy Apply button.
     * 
     * @return true if successfully started the Easy Apply process
     * @throws Exception if any errors occur
     */
    public boolean clickFirstJobAndApply() throws Exception {
        System.out.println("Attempting to click on first job listing...");
        
        try {
            // Wait for job results to load - we'll use this implicitly with WebElements
            
            // Different selectors for job listings
            List<By> jobSelectors = List.of(
                By.cssSelector(".job-card-container"),
                By.cssSelector(".jobs-search-results__list-item"),
                By.xpath("//li[contains(@class, 'jobs-search-results__list-item')]"),
                By.xpath("//div[contains(@class, 'job-card-container')]")
            );
            
            // Try each selector until we find one that works
            WebElement firstJob = null;
            for (By selector : jobSelectors) {
                List<WebElement> jobs = driver.findElements(selector);
                if (!jobs.isEmpty()) {
                    firstJob = jobs.get(0);
                    break;
                }
            }
            
            if (firstJob == null) {
                System.out.println("No job listings found. Taking screenshot for debugging.");
                takeScreenshot(driver, "no_job_listings.png");
                return false;
            }
            
            // Click on the first job
            System.out.println("Found first job listing. Clicking...");
            humanClick(firstJob);
            
            // Wait for job details to load
            Thread.sleep(2000 + random.nextInt(1000));
            
            // Take a screenshot of the job details page for debugging
            takeScreenshot(driver, "job_details_page.png");
            System.out.println("Taking screenshot of job details page to help debugging");
            
            // Allow more time for the page to load completely
            Thread.sleep(3000 + random.nextInt(2000));
            
            // Look for Easy Apply button with more comprehensive selectors
            List<By> easyApplySelectors = List.of(
                // Specific to the button seen in the screenshot
                By.cssSelector("button.artdeco-button--primary:has(span:contains('Easy Apply'))"),
                By.cssSelector(".jobs-apply-button"),
                By.cssSelector("button[data-control-name='jobdetails_topcard_inapply']"),
                // Based on the surrounding HTML structure
                By.xpath("//div[contains(@class, 'jobs-unified-top-card')]//button[contains(., 'Easy Apply')]"),
                By.xpath("//div[contains(@class, 'jobs-details-top-card')]//button[contains(., 'Easy Apply')]"),
                // With LinkedIn logo
                By.xpath("//button[.//li-icon and contains(., 'Easy Apply')]"),
                // Text-based selectors
                By.xpath("//button[contains(.,'Easy Apply')]"),
                By.xpath("//span[text()='Easy Apply']/ancestor::button"),
                // More generic
                By.cssSelector("button.artdeco-button--primary"),
                By.xpath("//div[contains(@class, 'jobs-s-apply')]//button"),
                // Absolute last resort
                By.xpath("//button[contains(@id, 'apply')]"),
                By.xpath("//button[contains(@class, 'apply')]"),
                By.xpath("//a[contains(@class, 'apply')]")
            );
            
            // Try each selector until we find the Easy Apply button
            WebElement easyApplyButton = null;
            for (By selector : easyApplySelectors) {
                try {
                    List<WebElement> buttons = driver.findElements(selector);
                    if (!buttons.isEmpty()) {
                        for (WebElement button : buttons) {
                            if (button.isDisplayed()) {
                                System.out.println("Found Easy Apply button with selector: " + selector.toString());
                                easyApplyButton = button;
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    // Log the exception but continue trying other selectors
                    System.out.println("Error with selector " + selector.toString() + ": " + e.getMessage());
                }
                if (easyApplyButton != null) break;
            }
            
            // If Easy Apply button is still not found, try a more aggressive approach
            if (easyApplyButton == null) {
                System.out.println("Standard selectors failed, trying to find any visible apply button...");
                // Find all buttons on the page
                List<WebElement> allButtons = driver.findElements(By.tagName("button"));
                for (WebElement button : allButtons) {
                    try {
                        if (button.isDisplayed()) {
                            String buttonText = button.getText().toLowerCase();
                            if (buttonText.contains("apply")) {
                                System.out.println("Found button with 'apply' text: " + buttonText);
                                easyApplyButton = button;
                                break;
                            }
                        }
                    } catch (Exception e) {
                        // Ignore and continue
                    }
                }
            }
            
            if (easyApplyButton == null) {
                System.out.println("Easy Apply button not found. Taking screenshot for debugging.");
                takeScreenshot(driver, "easy_apply_not_found.png");
                System.out.println("The Easy Apply button should be present above the job description.");
                System.out.println("Please check the screenshot to see what might have gone wrong.");
                return false;
            }
            
            // Click the Easy Apply button
            System.out.println("Found Easy Apply button. Clicking...");
            takeScreenshot(driver, "before_click_easy_apply.png");
            
            // Try multiple approaches to click the button
            boolean clickSuccess = false;
            
            try {
                // First approach: Scroll and use JavaScript click
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", easyApplyButton);
                Thread.sleep(1000);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", easyApplyButton);
                System.out.println("Clicked Easy Apply button via JavaScript");
                clickSuccess = true;
            } catch (Exception e) {
                System.out.println("First click approach failed: " + e.getMessage());
            }
            
            // Second approach: Try using Actions class if first approach failed
            if (!clickSuccess) {
                try {
                    org.openqa.selenium.interactions.Actions actions = new org.openqa.selenium.interactions.Actions(driver);
                    actions.moveToElement(easyApplyButton).click().build().perform();
                    System.out.println("Clicked Easy Apply button via Actions");
                    clickSuccess = true;
                } catch (Exception e) {
                    System.out.println("Second click approach failed: " + e.getMessage());
                }
            }
            
            // Third approach: Standard click as last resort
            if (!clickSuccess) {
                try {
                    easyApplyButton.click();
                    System.out.println("Clicked Easy Apply button via standard click");
                    clickSuccess = true;
                } catch (Exception e) {
                    System.out.println("Third click approach failed: " + e.getMessage());
                }
            }
            
            if (!clickSuccess) {
                System.out.println("All click methods failed. Taking screenshot for debugging.");
                takeScreenshot(driver, "easy_apply_click_failed.png");
                System.out.println("Could not click on the Easy Apply button despite finding it.");
                return false;
            }
            
            // Wait longer for application form to load
            System.out.println("Waiting for application form to load...");
            Thread.sleep(4000 + random.nextInt(2000));
            
            // Take a screenshot after clicking Easy Apply
            takeScreenshot(driver, "after_easy_apply_click.png");
            
            return true;
            
        } catch (Exception e) {
            System.out.println("Error clicking on first job and starting Easy Apply: " + e.getMessage());
            takeScreenshot(driver, "error_starting_application.png");
            throw e;
        }
    }
    
    /**
     * Handles the contact information section of the application.
     * Identifies form fields, fills them in if empty, and proceeds to the next step.
     * 
     * @return true if contact info was successfully filled out
     * @throws Exception if any errors occur
     */
    public boolean handleContactInfo() throws Exception {
        // Take a screenshot at the beginning of contact info handling
        takeScreenshot(driver, "contact_info_start.png");
        System.out.println("Starting contact info handling. Taking screenshot for reference.");
        
        // Wait for form to be fully loaded
        Thread.sleep(3000);
        System.out.println("Handling contact information form...");
        
        try {
            // First verify that we're on the contact info popup/modal
            boolean contactInfoPopupFound = false;
            
            // Check for contact info popup by different methods
            List<By> contactInfoSelectors = List.of(
                By.xpath("//h3[contains(text(), 'Contact info')]"),
                By.xpath("//div[contains(@class, 'jobs-easy-apply-modal')]"),
                By.xpath("//div[contains(@aria-labelledby, 'jobs-easy-apply')]"),
                By.xpath("//div[contains(@role, 'dialog')][.//h3]"),
                By.cssSelector("div.artdeco-modal__content")
            );
            
            for (By selector : contactInfoSelectors) {
                List<WebElement> elements = driver.findElements(selector);
                if (!elements.isEmpty()) {
                    System.out.println("Found contact info popup using selector: " + selector);
                    contactInfoPopupFound = true;
                    break;
                }
            }
            
            if (!contactInfoPopupFound) {
                System.out.println("Could not detect contact info popup. Taking screenshot for debugging.");
                takeScreenshot(driver, "contact_popup_not_found.png");
                return false;
            }
            
            System.out.println("Contact info form detected. Processing fields...");
            
            // Process text fields: First name, Last name, Location
            processTextField("First name", "firstName");
            processTextField("Last name", "lastName");
            processTextField("Location", "location");
            
            // Process phone country code dropdown
            processPhoneCountryCode();
            
            // Process email dropdown
            processEmailDropdown();
            
            // Take a screenshot after filling the form
            takeScreenshot(driver, "contact_info_filled.png");
            
            // Look for a Next/Continue button to proceed
            List<By> nextButtonSelectors = List.of(
                By.xpath("//button[contains(text(), 'Next')]"),
                By.xpath("//button[contains(text(), 'Continue')]"),
                By.xpath("//button[contains(text(), 'Review')]"),
                By.xpath("//button[contains(text(), 'Submit')]"),
                By.xpath("//button[contains(@aria-label, 'Continue to next step')]"),
                By.cssSelector("button.artdeco-button--primary")
            );
            
            // Try each selector until we find a Next button
            WebElement nextButton = null;
            for (By selector : nextButtonSelectors) {
                List<WebElement> buttons = driver.findElements(selector);
                if (!buttons.isEmpty()) {
                    for (WebElement button : buttons) {
                        if (button.isDisplayed() && button.isEnabled()) {
                            nextButton = button;
                            break;
                        }
                    }
                }
                if (nextButton != null) break;
            }
            
            if (nextButton != null) {
                System.out.println("Found Next button: " + nextButton.getText() + ". Clicking to proceed...");
                humanClick(nextButton);
                return true;
            } else {
                System.out.println("Next button not found. Taking screenshot for debugging.");
                takeScreenshot(driver, "next_button_not_found.png");
                return false;
            }
            
        } catch (Exception e) {
            System.out.println("Error handling contact info: " + e.getMessage());
            e.printStackTrace();
            takeScreenshot(driver, "error_handling_contact_info.png");
            return false;
        }
    }
    
    /**
     * Process a text field by finding it, checking if it's empty, and filling it if needed.
     * 
     * @param labelText The label text to look for
     * @param propertyName The property name to use in the user info file
     * @throws Exception if any errors occur
     */
    private void processTextField(String labelText, String propertyName) throws Exception {
        // Try multiple ways to find the field
        WebElement field = findInputField(labelText);
        
        if (field != null) {
            processTextField(field, propertyName, labelText);
        } else {
            System.out.println("Could not find " + labelText + " field using standard selectors.");
            
            // Try looking for the label as a WebElement and then find its input
            List<WebElement> labels = driver.findElements(
                By.xpath("//label[contains(text(), '" + labelText + "')] | //span[contains(text(), '" + labelText + "')]"));
            
            if (!labels.isEmpty()) {
                for (WebElement label : labels) {
                    if (label.isDisplayed()) {
                        WebElement associatedInput = findInputForLabel(label);
                        if (associatedInput != null) {
                            System.out.println("Found " + labelText + " field using label-input association.");
                            processTextField(associatedInput, propertyName, labelText);
                            return;
                        }
                    }
                }
            }
            
            System.out.println("Could not find " + labelText + " field using any method.");
        }
    }
    
    /**
     * Process a text field by checking if it's empty and filling it if needed.
     * 
     * @param field The WebElement input field to process
     * @param propertyName The property name to use in the user info file
     * @param labelText The human-readable label for this field
     * @throws Exception if any errors occur
     */
    private void processTextField(WebElement field, String propertyName, String labelText) throws Exception {
        // Check if the field is already filled
        String currentValue = field.getDomProperty("value");
        
        if (currentValue == null || currentValue.trim().isEmpty()) {
            // Field is empty, we need to fill it
            String savedValue = userInfo.getProperty(propertyName);
            
            if (savedValue != null && !savedValue.trim().isEmpty()) {
                // We have a saved value, use it
                System.out.println("Using saved value for " + labelText + ": " + savedValue);
                typeHumanLike(field, savedValue);
            } else {
                // No saved value, ask the user
                System.out.println("Need to get " + labelText + " from user.");
                String userValue = askUserForInput(labelText);
                typeHumanLike(field, userValue);
                
                // Save the value for future use
                userInfo.setProperty(propertyName, userValue);
                saveUserInfo();
            }
        } else {
            System.out.println(labelText + " is already filled with: " + currentValue);
        }
    }
    
    /**
     * Process phone country code dropdown if present.
     * 
     * @throws Exception if any errors occur
     */
    private void processPhoneCountryCode() throws Exception {
        // Look for country code dropdown
        List<By> countryCodeSelectors = List.of(
            By.xpath("//label[contains(text(), 'Phone country code')]/..//select"),
            By.xpath("//span[contains(text(), 'country code')]/..//select"),
            By.xpath("//select[contains(@id, 'phoneCountry')]"),
            By.xpath("//select[contains(@aria-label, 'country code')]")
        );
        
        // Try each selector until we find the dropdown
        WebElement countryCodeDropdown = null;
        for (By selector : countryCodeSelectors) {
            List<WebElement> dropdowns = driver.findElements(selector);
            if (!dropdowns.isEmpty()) {
                countryCodeDropdown = dropdowns.get(0);
                break;
            }
        }
        
        if (countryCodeDropdown != null) {
            // Check if a selection has already been made
            Select select = new Select(countryCodeDropdown);
            WebElement selectedOption = select.getFirstSelectedOption();
            String currentSelection = selectedOption.getText();
            
            // If the default selection isn't meaningful, we need to make a selection
            if (currentSelection == null || currentSelection.trim().isEmpty() || 
                currentSelection.equals("Select a country/region") || 
                currentSelection.equals("--")) {
                
                // Use saved value if available
                String savedCountryCode = userInfo.getProperty("phoneCountryCode");
                
                if (savedCountryCode != null && !savedCountryCode.trim().isEmpty()) {
                    // Try to select the saved country code
                    try {
                        select.selectByVisibleText(savedCountryCode);
                        System.out.println("Selected phone country code: " + savedCountryCode);
                    } catch (Exception e) {
                        System.out.println("Could not select saved country code. Asking user.");
                        selectCountryCodeWithUserInput(select);
                    }
                } else {
                    // No saved value, ask the user
                    selectCountryCodeWithUserInput(select);
                }
            } else {
                System.out.println("Phone country code already selected: " + currentSelection);
            }
        } else {
            System.out.println("No phone country code dropdown found.");
        }
    }
    
    /**
     * Ask the user to select a country code from the dropdown.
     * 
     * @param select The Select element representing the dropdown
     * @throws Exception if any errors occur
     */
    private void selectCountryCodeWithUserInput(Select select) throws Exception {
        // Get all options from the dropdown
        List<WebElement> options = select.getOptions();
        
        System.out.println("Please select a phone country code from the following options:");
        for (int i = 0; i < options.size(); i++) {
            System.out.println((i + 1) + ". " + options.get(i).getText());
        }
        
        // Get user input using the class scanner instance
        System.out.print("Enter the number of your selection: ");
        int selection;
        try {
            selection = scanner.nextInt();
            scanner.nextLine(); // Consume the newline
            
            if (selection > 0 && selection <= options.size()) {
                WebElement selectedOption = options.get(selection - 1);
                String selectedText = selectedOption.getText();
                select.selectByVisibleText(selectedText);
                
                // Save the selection for future use
                userInfo.setProperty("phoneCountryCode", selectedText);
                saveUserInfo();
                
                System.out.println("Selected phone country code: " + selectedText);
            } else {
                System.out.println("Invalid selection. Using default.");
            }
        } catch (Exception e) {
            System.out.println("Invalid input. Using default.");
            scanner.nextLine(); // Consume the invalid input
        }
    }
    
    /**
     * Process email dropdown if present, selecting the first option.
     * 
     * @throws Exception if any errors occur
     */
    private void processEmailDropdown() throws Exception {
        // Look for email dropdown
        List<By> emailSelectors = List.of(
            By.xpath("//label[contains(text(), 'Email')]/..//select"),
            By.xpath("//span[contains(text(), 'Email address')]/..//select"),
            By.xpath("//select[contains(@id, 'email')]"),
            By.xpath("//select[contains(@aria-label, 'email')]")
        );
        
        // Try each selector until we find the dropdown
        WebElement emailDropdown = null;
        for (By selector : emailSelectors) {
            List<WebElement> dropdowns = driver.findElements(selector);
            if (!dropdowns.isEmpty()) {
                emailDropdown = dropdowns.get(0);
                break;
            }
        }
        
        if (emailDropdown != null) {
            // Select the first option (top email) as requested
            Select select = new Select(emailDropdown);
            List<WebElement> options = select.getOptions();
            
            if (!options.isEmpty()) {
                // Skip the first option if it's a placeholder like "Select an email"
                int indexToSelect = 0;
                if (options.size() > 1) {
                    String firstOptionText = options.get(0).getText();
                    if (firstOptionText.contains("Select") || firstOptionText.equals("--")) {
                        indexToSelect = 1;
                    }
                }
                
                if (indexToSelect < options.size()) {
                    select.selectByIndex(indexToSelect);
                    System.out.println("Selected email: " + options.get(indexToSelect).getText());
                }
            }
        } else {
            System.out.println("No email dropdown found. It might be a text field instead.");
            
            // Try to find email as a text field
            WebElement emailField = findInputField("Email");
            if (emailField != null) {
                // Check if the field is already filled
                // Note: getAttribute is deprecated but still widely used in Selenium
                // as the recommended alternative varies by browser implementation
                String currentValue = emailField.getDomProperty("value");
                
                if (currentValue == null || currentValue.trim().isEmpty()) {
                    // Field is empty, we need to fill it
                    String savedValue = userInfo.getProperty("email");
                    
                    if (savedValue != null && !savedValue.trim().isEmpty()) {
                        // We have a saved value, use it
                        System.out.println("Using saved email: " + savedValue);
                        typeHumanLike(emailField, savedValue);
                    } else {
                        // No saved value, ask the user
                        String userValue = askUserForInput("Email address");
                        typeHumanLike(emailField, userValue);
                        
                        // Save the value for future use
                        userInfo.setProperty("email", userValue);
                        saveUserInfo();
                    }
                } else {
                    System.out.println("Email field is already filled with: " + currentValue);
                }
            }
        }
    }
    
    /**
     * Finds an input field based on its label text.
     * 
     * @param labelText The label text to look for
     * @return WebElement for the input field or null if not found
     */
    private WebElement findInputField(String labelText) {
        List<By> inputSelectors = List.of(
            By.xpath("//label[contains(text(), '" + labelText + "')]/..//input"),
            By.xpath("//label[contains(text(), '" + labelText + "')]/following-sibling::div//input"),
            By.xpath("//label[contains(text(), '" + labelText + "')]/following::input"),
            By.xpath("//span[contains(text(), '" + labelText + "')]/..//input"),
            By.xpath("//input[contains(@id, '" + labelText.toLowerCase().replace(" ", "") + "')]"),
            By.xpath("//input[contains(@aria-label, '" + labelText + "')]")
        );
        
        WebElement inputField = null;
        for (By selector : inputSelectors) {
            List<WebElement> inputs = driver.findElements(selector);
            if (!inputs.isEmpty()) {
                for (WebElement input : inputs) {
                    if (input.isDisplayed()) {
                        inputField = input;
                        System.out.println("Found " + labelText + " field using selector: " + selector);
                        break;
                    }
                }
            }
            if (inputField != null) break;
        }
        
        return inputField;
    }
    
    /**
     * Finds an input field associated with a given label element.
     * This is useful for handling dynamically generated forms where the relationship
     * between labels and inputs might not follow a consistent pattern.
     * 
     * @param label The label WebElement
     * @return WebElement for the associated input field or null if not found
     */
    private WebElement findInputForLabel(WebElement label) {
        WebElement input = null;
        
        try {
            // Try to get the 'for' attribute of the label
            // Using getDomAttribute instead of deprecated getAttribute
            String forAttribute = label.getDomAttribute("for");
            if (forAttribute != null && !forAttribute.isEmpty()) {
                // Try to find the input by id matching the 'for' attribute
                List<WebElement> matchingInputs = driver.findElements(By.id(forAttribute));
                if (!matchingInputs.isEmpty()) {
                    input = matchingInputs.get(0);
                    System.out.println("Found input for label using 'for' attribute: " + forAttribute);
                    return input;
                }
            }
            
            // Try various XPath strategies to find the associated input
            List<By> inputSelectors = List.of(
                By.xpath("./following-sibling::div//input"),
                By.xpath("./following-sibling::input"),
                By.xpath("./parent::div/following-sibling::div//input"),
                By.xpath("./ancestor::div[contains(@class, 'form-field')]//input"),
                By.xpath("./ancestor::div[1]//input")
            );
            
            for (By selector : inputSelectors) {
                List<WebElement> inputs = label.findElements(selector);
                if (!inputs.isEmpty()) {
                    for (WebElement foundInput : inputs) {
                        if (foundInput.isDisplayed() && foundInput.isEnabled()) {
                            input = foundInput;
                            System.out.println("Found input for label using selector: " + selector);
                            break;
                        }
                    }
                }
                if (input != null) break;
            }
            
            // If still not found, try looking for inputs nearby in the DOM
            if (input == null) {
                WebElement parentDiv = label.findElement(By.xpath("./ancestor::div[position()=1 or position()=2 or position()=3]"));
                List<WebElement> allInputsInParent = parentDiv.findElements(By.tagName("input"));
                if (!allInputsInParent.isEmpty()) {
                    for (WebElement possibleInput : allInputsInParent) {
                        if (possibleInput.isDisplayed() && possibleInput.isEnabled()) {
                            input = possibleInput;
                            System.out.println("Found input for label by searching parent divs");
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error finding input for label: " + e.getMessage());
        }
        
        return input;
    }
    
    /**
     * Ask the user for input via the console.
     * 
     * @param fieldName The name of the field to get input for
     * @return The user's input
     */
    private String askUserForInput(String fieldName) {
        System.out.print("Please enter your " + fieldName + ": ");
        return scanner.nextLine(); // Using the class scanner instance
    }
    
    /**
     * Load user information from properties file.
     * 
     * @return Properties object with user info
     */
    private Properties loadUserInfo() {
        Properties props = new Properties();
        Path path = Paths.get(USER_INFO_FILE);
        
        if (Files.exists(path)) {
            try (FileInputStream input = new FileInputStream(path.toFile())) {
                props.load(input);
                System.out.println("User info loaded successfully from: " + path.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("Warning: Could not load user info file: " + e.getMessage());
            }
        } else {
            System.out.println("User info file not found. Will create a new one when needed.");
        }
        
        return props;
    }
    
    /**
     * Save user information to properties file.
     */
    private void saveUserInfo() {
        Path path = Paths.get(USER_INFO_FILE);
        
        try {
            // Create parent directories if they don't exist
            Path parentDir = path.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            
            // Save the properties file
            try (FileOutputStream output = new FileOutputStream(path.toFile())) {
                userInfo.store(output, "LinkedIn Easy Apply User Information");
                System.out.println("User info saved successfully to: " + path.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error: Could not save user info file: " + e.getMessage());
        }
    }
    
    /**
     * Type text into an element character by character with random delays to simulate human typing.
     * 
     * @param element The WebElement to type into
     * @param text The text to type
     * @throws InterruptedException if sleep is interrupted
     */
    private void typeHumanLike(WebElement element, String text) throws InterruptedException {
        // Clear the field first (if it's not empty)
        // Note: getDomProperty is used instead of getAttribute (which is deprecated)
        String currentValue = element.getDomProperty("value");
        if (currentValue != null && !currentValue.isEmpty()) {
            element.clear();
            Thread.sleep(300 + random.nextInt(200));
        }
        
        // Type each character with a random delay
        for (char c : text.toCharArray()) {
            element.sendKeys(String.valueOf(c));
            Thread.sleep(50 + random.nextInt(100));
        }
        
        // Pause at the end as a human would
        Thread.sleep(500 + random.nextInt(500));
    }
    
    /**
     * Click on an element in a human-like way.
     * 
     * @param element The WebElement to click
     * @throws InterruptedException if sleep is interrupted
     */
    private void humanClick(WebElement element) throws InterruptedException {
        try {
            // First try a normal click
            element.click();
        } catch (Exception e) {
            System.out.println("Standard click failed, trying JavaScript click");
            // If normal click fails, try JavaScript click
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].click();", element);
        }
        
        // Pause after clicking as a human would
        Thread.sleep(1000 + random.nextInt(1000));
    }
    
    /**
     * Take a screenshot for debugging purposes.
     * 
     * @param driver WebDriver instance
     * @param filename Name of the screenshot file
     */
    private void takeScreenshot(WebDriver driver, String filename) {
        try {
            org.openqa.selenium.OutputType<java.io.File> outputType = org.openqa.selenium.OutputType.FILE;
            java.io.File screenshot = ((org.openqa.selenium.TakesScreenshot) driver).getScreenshotAs(outputType);
            Path destination = Paths.get(filename);
            Files.copy(screenshot.toPath(), destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Screenshot saved to: " + destination.toAbsolutePath());
        } catch (Exception e) {
            System.out.println("Failed to save screenshot: " + e.getMessage());
        }
    }
    
    /**
     * Cleanup resources when this class is no longer needed.
     * Should be called when the workflow is completed.
     */
    public void cleanup() {
        if (scanner != null) {
            scanner.close();
            scanner = null;
        }
    }
}

package com.example.easy;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Utility class to load LinkedIn configuration properties from file
 * and provide secure access to sensitive credentials
 */
public class LinkedInConfigLoader {
    
    private static final String DEFAULT_PROPERTIES_PATH = "src/main/resources/linkedin.properties";
    private static Properties properties = null;
    
    /**
     * Load LinkedIn properties from the default properties file
     * 
     * @return Properties object containing LinkedIn configuration
     * @throws IOException If properties file cannot be read
     */
    public static synchronized Properties loadProperties() throws IOException {
        return loadProperties(DEFAULT_PROPERTIES_PATH);
    }
    
    /**
     * Load LinkedIn properties from a specified properties file
     * 
     * @param propertiesPath Path to the properties file
     * @return Properties object containing LinkedIn configuration
     * @throws IOException If properties file cannot be read
     */
    public static synchronized Properties loadProperties(String propertiesPath) throws IOException {
        if (properties == null) {
            properties = new Properties();
            Path path = Paths.get(propertiesPath);
            
            if (Files.exists(path)) {
                try (InputStream input = new FileInputStream(path.toFile())) {
                    properties.load(input);
                    System.out.println("LinkedIn properties loaded successfully from: " + path.toAbsolutePath());
                }
            } else {
                throw new IOException("LinkedIn properties file not found: " + path.toAbsolutePath());
            }
        }
        
        return properties;
    }
    
    /**
     * Get a property value with default fallback
     * 
     * @param key Property key
     * @param defaultValue Default value if property is not found
     * @return Property value or default if not found
     */
    public static String getProperty(String key, String defaultValue) {
        try {
            Properties props = loadProperties();
            return props.getProperty(key, defaultValue);
        } catch (IOException e) {
            System.err.println("Warning: Could not load properties file. Using default value for " + key);
            return defaultValue;
        }
    }
    
    /**
     * Get email from properties
     * 
     * @return LinkedIn email
     */
    public static String getEmail() {
        return getProperty("linkedin.email", "");
    }
    
    /**
     * Get password from properties
     * 
     * @return LinkedIn password
     */
    public static String getPassword() {
        return getProperty("linkedin.password", "");
    }
    
    /**
     * Get job role from properties
     * 
     * @return Job role to search for
     */
    public static String getJobRole() {
        return getProperty("linkedin.job.role", "Software Engineer");
    }
    
    /**
     * Get job location from properties
     * 
     * @return Job location to search for
     */
    public static String getJobLocation() {
        return getProperty("linkedin.job.location", "San Francisco, CA");
    }
    
    /**
     * Check if manual login is enabled
     * 
     * @return true if manual login is enabled
     */
    public static boolean isManualLoginEnabled() {
        return Boolean.parseBoolean(getProperty("linkedin.wait.manual.login", "true"));
    }
    
    /**
     * Get manual login wait time in seconds
     * 
     * @return Wait time in seconds
     */
    public static int getManualLoginWaitSeconds() {
        try {
            return Integer.parseInt(getProperty("linkedin.wait.manual.login.seconds", "30"));
        } catch (NumberFormatException e) {
            return 30;
        }
    }
    
    /**
     * Check if headless browser mode is enabled
     * 
     * @return true if headless mode is enabled
     */
    public static boolean isHeadlessModeEnabled() {
        return Boolean.parseBoolean(getProperty("linkedin.browser.headless", "false"));
    }
    
    /**
     * Get sort by option from properties
     * Options: day, week, month
     * 
     * @return sort by option or empty string if not specified
     */
    public static String getSortByOption() {
        return getProperty("linkedin.filter.sort_by", "");
    }
    
    /**
     * Get job type filter option from properties
     * Options: temporary, contract, volunteer, full_time, part_time
     * 
     * @return job type option or empty string if not specified
     */
    public static String getJobTypeOption() {
        return getProperty("linkedin.filter.job_type", "");
    }
    
    /**
     * Get experience level filter option from properties
     * Options: internship, entry_level, associate, mid_senior_level, director
     * 
     * @return experience level option or empty string if not specified
     */
    public static String getExperienceLevelOption() {
        return getProperty("linkedin.filter.exp_level", "");
    }
    
    /**
     * Check if easy apply filter is enabled
     * 
     * @return true if easy apply filter is enabled
     */
    public static boolean isEasyApplyEnabled() {
        return Boolean.parseBoolean(getProperty("linkedin.filter.easy_apply", "false"));
    }
    
    /**
     * Get remote filter option from properties
     * Options: remote, onsite, hybrid
     * 
     * @return remote filter option or empty string if not specified
     */
    public static String getRemoteFilterOption() {
        return getProperty("linkedin.filter.remote", "");
    }
}

# LinkedIn Job Search Automation

A Java application that automates LinkedIn job searches and applications using Selenium WebDriver.

## Features

- **Stealth Browser Configuration**: Uses advanced techniques to avoid detection as an automated browser
- **Human-like Interactions**: Simulates realistic human behavior with variable typing speeds and natural mouse movements
- **Comprehensive Job Filtering**: Supports multiple filter options including:
  - Easy Apply
  - Remote/On-site/Hybrid options
  - Experience level
  - Job type
  - Date posted

## Setup

1. Clone this repository
2. Copy `src/main/resources/linkedin.properties.template` to `src/main/resources/linkedin.properties`
3. Edit the properties file with your LinkedIn credentials and preferred job search parameters
4. Run using Maven: `mvn spring-boot:run`

## Configuration Options

All search parameters and filters can be configured in the `linkedin.properties` file:

- `linkedin.email`, `linkedin.password`: Your LinkedIn credentials
- `linkedin.job.role`: Job title to search for
- `linkedin.job.location`: Location for job search
- `linkedin.filter.sort_by`: Sort by time period (day, week, month)
- `linkedin.filter.job_type`: Job type (full_time, part_time, contract, etc.)
- `linkedin.filter.exp_level`: Experience level (entry_level, mid_senior_level, etc.)
- `linkedin.filter.easy_apply`: Whether to filter for Easy Apply jobs (true/false)
- `linkedin.filter.remote`: Remote option (remote, onsite, hybrid)

## Security

- Never commit your `linkedin.properties` file with real credentials
- The file is included in `.gitignore` to prevent accidental commits

## Technical Details

Built with:
- Java 17
- Selenium WebDriver
- Spring Boot
- WebDriverManager

## License

MIT

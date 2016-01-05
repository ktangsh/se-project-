# se-project-
Software Engineering Project 

Summary of Project Requirements: 

Project-for-2015(**Requirement Change**)
(Redirected from Project)
Contents [hide] 
1 Criteria 
2 Project Scope 
3 Project Deadline 
4 Project Process
5 Presentation Requirements
5.1 PM Review (Week 7, In-Class)
5.2 User Acceptance Test (UAT) (Week 12, In-Class)
5.3 Final Presentation (Week 14, In-Class)
6 Meeting with Supervisors
7 Requirements Overview
7.1 Functionality Overview
8 Understanding Terms and Data
8.1 Glossary Of Terms
8.1.1 MAC_Address
8.1.2 app_id
8.1.3 location_id
8.1.4 Semantic Place
8.1.5 Duration of interest/Processing window
8.2 Understanding the app usage data
8.3 Understanding the location data
8.4 Display of the results
8.5 Parsing CSV files
9 Detailed Logic Specification
9.1 Login (Blue)
9.2 Bootstrap (Blue)
9.2.1 Bootstrap with app usage & demographics data
9.2.2 Common Validations for all data files
9.2.3 Validation Definitions
9.2.4 File-specific Validations
9.2.4.1 demographics.csv
9.2.4.2 app-lookup.csv
9.2.4.3 app.csv
9.2.5 Upload additional app usage & demographics data
9.3 Basic App Usage Report (Blue)
9.3.1 Breakdown by usage time category
9.3.2 Breakdown by usage time category and demographics
9.3.3 Breakdown by app category
9.3.4 Diurnal pattern of app usage time
9.4 Top-k App Usage Report (Blue)
9.4.1 Top-k most used apps (given a school)
9.4.2 Top-k students with most app usage (given an app category)
9.4.3 Top-k schools with most app usage (given an app category)
9.5 Smartphone Overuse Report (Blue)
9.6 Dual Interfaces (Web UI and Web Services) (Blue)
9.7 Loading Location Data (Green)
9.7.1 Bootstrap with location data
9.7.1.1 location.csv
9.7.1.2 location-lookup.csv
9.7.2 Upload additional location data
9.8 Deletion of location data (Green)
9.9 Smartphone Usage Heatmap (Green)
9.10 Social Activeness Report (Green)
9.11 Advanced Smartphone Overuse Report (Red)
9.12 Graphical UIs (Heatmap and Chart) (Red)
9.13 Use of framework (Black)
9.14 Fast execution of queries (Black)
10 Web Service Requirements
10.1 Overview
10.2 JSON Basics
10.2.1 JSON Values
10.2.2 Ordering
10.2.3 Whitespace
10.3 Common Validations for JSON requests
10.4 General Input Definitions
10.5 General Output Definitions
10.6 Function-Specific Input/Output and Validations
10.6.1 Authenticate
10.6.2 Bootstrap with app usage & demographics data
10.6.3 Upload additional app usage & demographics data
10.6.4 Basic App Usage Report
10.6.4.1 Breakdown by usage time category
10.6.4.2 Breakdown by usage time category and demographics
10.6.4.3 Breakdown by app category
10.6.4.4 Diurnal pattern of app usage time
10.6.5 Top-k App Usage Report
10.6.5.1 Top-k most used apps
10.6.5.2 Top-k students with most app usage
10.6.5.3 Top-k schools with most app usage
10.6.6 Smartphone Overuse Report
10.6.7 Bootstrap with location data
10.6.8 Deletion of location data
10.6.8.1 location-delete.csv
10.6.8.2 Using web service
10.6.9 Smartphone Usage Heatmap
10.6.10 Social Activeness Report
10.6.11 Advanced Smartphone Overuse Report
11 Changes To Project Requirements (OH NO!!!)
11.1 Change to demographics.csv & Breakdown by usage time category and demographics Report
11.2 Handling of columns in the bootstrap files
11.3 Performance Requirements
11.4 Location delete
12 Other Tips
12.1 Using Libraries and External Code
12.2 Sample Data
12.3 Piracy
12.4 Presentation Guidelines
12.5 GIT
12.6 Project Guidelines & Feedback from PM Review Presentation
12.7 Questions

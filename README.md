# Global Banking Payment | Mobile Banking System

**I led a groundbreaking project implementing a comprehensive CI/CD pipeline for Global Digital Payment System | Mobile Banking System, supporting both local currency operations with microservice architecture, revolutionizing our development efficiency and deployment processes.**

 ## Micro-Service Architecture With Spring Security, GoLang and Python

### 🔗 Branch Per Microservice Strategy
-  microservice was meticulously managed with its own branch, optimizing version control, collaboration, and maintenance.

### 🛠️ Jenkinsfile Integration
- Utilized Jenkins to integrate Jenkinsfile into each microservice branch, enabling tailored pipelines and enhancing our CI/CD workflows.

### 🔁 Automated Pipeline Triggering
- Implemented multibranch generic webhook triggers for automated microservice CI pipeline activation, reducing manual intervention and accelerating feedback loops.

### 🏗️ Docker Image CI Pipelines
- Developing 12 CI pipelines dedicated to Docker image building ensured consistency and reliability in our application's image deployment.

### 🚢 Seamless Docker Image Pushing
- Automated Docker image pushing to our repository post-building, eliminating manual steps and expediting artifact delivery.

### 🌐 Unified ArgoCD CD Pipeline
- Streamlined deployment with a single ArgoCD-based CD pipeline for all microservices, leveraging GitOps principles to ensure consistency, minimize deployment complexities, and enable automated, declarative updates across environments.
    - Implement Kubernetes Authentication Mechanisms:
        - 🔒 Secure Authentication (OIDC & Service Account Tokens)
            - Switched from static Kubernetes tokens to OIDC authentication (k8s-oidc-token)
            - Reduced privilege exposure by limiting the scope of service accounts.
        - 🔑 Enhanced Jenkins Security:
            - Used Jenkins' Credentials Plugin for secure authentication.
            - Implemented RBAC & audit logging for better access control.
            - Kept Jenkins plugins updated to mitigate security risks.
        - 🛡️ API Security Enhancements:
            - Enforced HTTPS for secure data transmission.
            - Implemented OAuth 2.0 authentication for API security.
            - Added input validation, rate limiting, and encryption for data security.

## Project Details & Tools
1. AWS Infrastructure: We leveraged AWS EC2 (Ubuntu 20. T2.large) for development environments and EKS Cluster deployment.
2. Automated Setup: Utilizing AWS CLI, kubectl, and eksctl, we streamlined EKS Cluster creation and configuration.
3. Jenkins Integration: We integrated Jenkins with essential plugins (Docker, Kubernetes) for robust CI/CD workflows.
4. Docker and Kubernetes Orchestration: We orchestrated Docker image builds, repository management, and Kubernetes deployments with seamless integration.
5. ArgoCD Pipelines: Developing efficient ArgoCD pipelines with webhook triggers, GitOps workflows, and Kubernetes configurations, we leveraged automated sync, declarative deployments, and multi-environment management to optimize microservice deployments and ensure seamless CI/CD integration.


# Service 1
### Authentication Service

- Built the authentication, authorization and verification with Spring Boot & Spring Security and mysql database. 
  
## Introduction
> Spring Boot Security provides mechanisms to secure applications and APIs or endpoints. Both JWT (JSON Web Tokens) and OAuth2 are widely used for authentication and authorization. In this project we take advantage of this mechanisms spring boot provides us.

## Quick Start
> This Authentication service Handles User authentication and authorization which enables Users to access and operate on the entire application e.g "Deposit money into their wallet which need to be authenticated with a specific user details, Transfer to bank or another user in the platform using their username, pay bills and the rest.
- How this work is User sign-Up with require detials ['firstname, lastname, email, username, password, gender, telephone, country, city']
- Request will validate and Store user into the database and email/sms message will be send to the **_notification-service_** where RabbitMQ is configured to process messages through Different routes, Queues in asynchronously compact.
- If user verify their account through the email message they received, request will be send to  **_Wallet-service_** to create User wallet account for that user and  user account will Open allowing user to login. 
- Note every users is treated Uniquely and that makes jwt Unique to user and also makes it easy to authentication with spring security Authentication testing against the username in the encryted in the jwt and username username by pass or any datails user may pass along with their request.
- This service also enable users to turn on 2Fa-Authentication process for additional security to their wallet so every time user try to login, OTP Keys will be send to user name from the database and once user provide the OTP key and verified user will be process login success.
- This Service enable user to change password when logged-In, reset password if forgotten has RabbitMQ message process, including 2FA-Authentication.
- Change profile by uploading profile picture where i implemented file-handler. 

# Folder Structure 
```
C:.
├───.mvn
│   └───wrapper
├───src
│   ├───main
│   │   ├───java
│   │   │   └───pesco
│   │   │       └───authentication_service
│   │   │           ├───clients
│   │   │           ├───configurations    
│   │   │           ├───controllers       
│   │   │           ├───dtos
│   │   │           ├───enums
│   │   │           ├───exceptions        
│   │   │           ├───models
│   │   │           ├───payloads
│   │   │           ├───repositories      
│   │   │           ├───responses
│   │   │           ├───services
│   │   │           ├───servicesImplementation
│   │   │           └───utils
│   │   └───resources
│   │       ├───static
│   │       │   ├───css
│   │       │   ├───image
│   │       │   └───js
│   │       └───templates
│   │           └───error
│   └───test
│       └───java
│           └───pesco
│               └───authentication_service
└───target
    ├───classes
    │   ├───pesco
    │   │   └───authentication_service
    │   │       ├───clients
    │   │       ├───configurations
    │   │       ├───controllers
    │   │       ├───dtos
    │   │       ├───enums
    │   │       ├───exceptions
    │   │       ├───models
    │   │       ├───payloads
    │   │       ├───repositories
    │   │       ├───responses
    │   │       ├───services
    │   │       ├───servicesImplementation
    │   │       └───utils
    │   ├───static
    │   │   ├───css
    │   │   └───js
    │   └───templates
    │       └───error
    ├───generated-sources
    │   └───annotations
    ├───generated-test-sources
    │   └───test-annotations
    ├───maven-archiver
    ├───maven-status
    │   └───maven-compiler-plugin
    │       ├───compile
    │       │   └───default-compile
    │       └───testCompile
    │           └───default-testCompile
    ├───surefire-reports
    └───test-classes
        └───pesco
            └───authentication_service
```

## What are the challenges encounter from the stated project (if any)?

- Challenge(1): Seamlessly integrating Spring Security, Go services, and Spring Boot for a unified user experience while maintaining consistent data flow across platforms.
- Challenge(2): Ensuring secure authentication and authorization across multiple services (Spring Security for authentication, Go for trading). Token sharing and validation in a distributed architecture are critical.

## **How were you able to overcome it?**
- Solution(1): Use message brokers like RabbitMQ on authentication service and Kafka on deposit/withdraw wallet to enable smooth communication between services and ensure robust API documentation for cross-language compatibility.
- Solution(2): Implement a centralized token service (using OAuth2/JWT) to ensure uniform security policies across all services.

# Service 2
### Deposit & Withdral Service

> This Service is responsible of creating user wallet on *_USD, EUR, NGN, GBP, JPY, AUD, CAD, CHF, CNY,  INR_* where User can make deposit  in their currency and withdral/transfer. 

## Introduction
> **Authentication Service** provides JSON Web Tokens for users during login process that enable them to operate on their wallet.

## Quick Start
> This service forcus on designing wallet balance, deposit action, withdraw action and communicating with *_History service_*

#### How This Work?
- First time user login first time into their wallet, User will be ask to created transfer pin, this pin consist of 4 digit number of their choice.
- Pin will be use to process transfer or any withdraws for security reasons and user can change it anytime.
- User has `Level 2 Option` to set up 2FA-Authentication, means every transfer or any withdraws `OPT` pin will be send to User email instead of having default PIN.
- To Deposit user has options to choose the payment service they want to use "Paystack or flutterWave" and when deposit request is send application check which platform user select to make their deposit so that the application can user request to the platform requesting credit and debit action should taken. ```Deposit from user bank and credit user wallet in the platform``` .
- Deposit success check and send notification to user about their current wallet balance and action taken with the help of RabbitMQ.
- User can transfer money to another user in same platform using recipient username example ***```{'recipientUsername':'John'}```*** and if transaction is successful application automatically send notification message to both users ***```Debit message to sender and Credit message to the recipient```***.
- User can view their Transaction history, filter by date, days and year.
- User can always print transaction receipt because every transaction generate receipt and save it into the database, User can either download immidiately after transaction is successful or comes back to download it from transaction history. This is keep transparency in the platform service which they provide to customers and helps users to keep track of their transactions.
- Design the system with secure data storage, encryption, and audit trails. Regularly update policies to align with evolving regulations.
- Implement centralized logging (e.g., ELK stack) and distributed tracing (e.g., Zipkin, Jaeger) to diagnose and resolve issues promptly.
- Built middlware Detecting Sophisticated Fraud Patterns with asynchronous processing.
- Use machine learning models to enhance behavioral analysis and detect sophisticated patterns.
- Implement regular audits and updates to ensure compliance with the latest regulations.
- Employ real-time monitoring tools to provide instant alerts and actionable insights for flagged activities.
- Created Mockito Unit testing / Junit testing for every phase of the application.

## Project Structure 

```
C:.
├───.mvn
│   └───wrapper
├───src
│   ├───main
│   │   ├───java
│   │   │   └───pesco
│   │   │       └───wallet_service   
│   │   │           ├───clients      
│   │   │           ├───configuration
│   │   │           ├───controllers  
│   │   │           ├───dtos
│   │   │           ├───enums        
│   │   │           ├───exceptions   
│   │   │           ├───models       
│   │   │           ├───payloads     
│   │   │           ├───respositories        
│   │   │           ├───serviceImplementation
│   │   │           ├───services
│   │   │           └───utils
│   │   └───resources
│   │       ├───static
│   │       └───templates
│   └───test
│       └───java
│           └───pesco
│               └───wallet_service
└───target
    ├───classes
    │   └───pesco
    │       └───wallet_service
    │           ├───clients
    │           ├───configuration
    │           ├───controllers
    │           ├───dtos
    │           ├───enums
    │           ├───exceptions
    │           ├───models
    │           ├───payloads
    │           ├───respositories
    │           ├───serviceImplementation
    │           ├───services
    │           └───utils
    ├───generated-sources
    │   └───annotations
    ├───generated-test-sources
    │   └───test-annotations
    ├───maven-archiver
    ├───maven-status
    │   └───maven-compiler-plugin
    │       ├───compile
    │       │   └───default-compile
    │       └───testCompile
    │           └───default-testCompile
    ├───surefire-reports
    └───test-classes
        └───pesco
            └───wallet_service
```

### Dependencies Injected
- `WebClient.Builder:` Configures the `WebClient` with a base URL and other settings.
- `@Value("${auth-service.base-url}"):` Reads the `auth-service.base-url` property from the application configuration `application.yml` for the API's base URL.
    - Key Points:
      - `Error Handling:` Uses onStatus to handle 4xx and 5xx HTTP errors.
        - For 4xx errors, extracts a detailed error message using `extractDetailsFromError`.
        - Throws `UserNotFoundException` for user-related issues.
       
- One thing you will also notice is we are using `USERDTO class`, Yes
- In microservice application you create alot of `DTO'S` class objects to read data that is why it call **`Data Transfer Objects`** you can say it serve as user representative in this this service which consist both **`User model`** and **`UserRecord model`**

## What are the challenges encounter from the stating of the project?

- `Challenge(1)` Providing real-time updates for crypto prices and transactions, which requires efficient communication between the backend and the front-end.
- `Challenge(2)` Supporting both local currency operations and some foreign currency swapping involves accurate and up-to-date exchange rate handling, which can be resource-intensive.
- `Challenge(3)` The platform must handle high traffic and complex operations like trading, wallet transactions, and blockchain calls without degrading performance.
- `Challenge(4)` Ensuring compliance with local and international regulations for payment systems.
- `Challenge(5)` Detecting Sophisticated Fraud Patterns- Fraudsters often use complex strategies that blend legitimate and illegitimate activities, making detection difficult.

## How were i able to overcome it?

- `Solution(1)` Use WebSockets or server-sent events for real-time updates.
- `Solution(2)` Leverage flutterWave APIs for real-time exchange rates like naira-to-dollar, dollar-to-euro, naira-to-pounds exchange and implement robust error handling to deal with third-party API failures.
- `Solution(3)` Use microservices architecture, load balancing, and database optimization. Incorporate caching for frequently accessed data and monitor system performance.
- `Solution(4)` Design the system with secure data storage, encryption, and audit trails. Regularly update policies to align with evolving regulations
- `Solution(5)` Created middleware for advance security fraud detechtion:
    - Implemented middleware for IP address monitoring and transaction interception based on several criteria
         - **Large Transactions:** Flagged transactions exceeding platform-defined thresholds for further review.
         - **High-Frequency Transactions:** Monitored accounts for unusually high transaction volumes within short time-frames to detect suspicious behavior or could indicate potential money laundering or illegal activity.
         - **Geographic and Risk-Based Monitoring:**  Identified transactions involving high-risk regions/countries or blacklisted wallet addresses to comply with AML regulations.
         - **Behavioral Analysis:** Detected inconsistent behavior, such as large deviations from typical transaction amounts, to prevent fraud.
         - **Multiple Accounts Sharing the Same IP:** Checked for potential sybil attacks by monitoring accounts initiating transactions from the same IP address.
             - **Reason:**
                 - This could be a sign of suspicious activity such as a single entity controlling multiple accounts.
        - **Deposits Followed by Immediate Transfers:** Flagged immediate fund transfers after deposits to prevent potential money laundering activities.
            - **Reason:**
                - This behavior could indicate attempts to obfuscate the origin of the funds (layering phase of money laundering).
        - Implement data Encryption.
        - Implement Event Sourcing to make history difficult to tamper.
  
 # VIEW THE DEPOSIT WITHDRAW TRANSFER SERVICE APPLICATION
 
 -  [Go to Authentication Service branch](https://github.com/davidakpele/banking-system-microservice-with-deployment/tree/main/authentication-service)
 - [Go to Deposit Service branch](https://github.com/davidakpele/banking-system-microservice-with-deployment/tree/main/deposit-service)
 - [Go to Wallet Service](https://github.com/davidakpele/banking-system-microservice-with-deployment/tree/main/wallet-service)
 - [Go Withdrawal Service](https://github.com/davidakpele/banking-system-microservice-with-deployment/tree/main/withdrawal-service)
 -  [Go to History Service](https://github.com/davidakpele/banking-system-microservice-with-deployment/tree/main/history-service)
 - [Go to Bank Collection Service](https://github.com/davidakpele/banking-system-microservice-with-deployment/tree/main/bank-collection-service)
 - [Go to Black Listed Wallet Service](https://github.com/davidakpele/banking-system-microservice-with-deployment/tree/main/blacklist-service)
 - [Go to Wallet Maintenance Service](https://github.com/davidakpele/banking-system-microservice-with-deployment/tree/main/maintenance-service)
 - [Go to Beneficiary Service](https://github.com/davidakpele/banking-system-microservice-with-deployment/tree/main/beneficiary-service)
 - [Go to Notification Service](https://github.com/davidakpele/banking-system-microservice-with-deployment/tree/main/notification-service)
 - [Go to Revenue Service](https://github.com/davidakpele/banking-system-microservice-with-deployment/tree/main/revenue-service)

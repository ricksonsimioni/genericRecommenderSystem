# Generic Recommender System 

This project is a generic recommender system implemented in Java using the framework Apache Mahout. The system leverages EMF models and EOL scripts to provide tailored recommendations based on user inputs and preferences aiming to support the configuration of such systems.

## Project Structure

The project is organized into several key directories:

### Source Code

- **Main Java Class**
  - `src/main/java/genericRecommenderSystem/Main.java`
    - This is the main entry point of the application. It contains the logic to run the recommender system.

### Models

- **Metamodels and EMF Models**
  - `src/main/Models/`
    - This directory contains the models and metamodels used by the recommender system.

- **EOL Scripts**
  - `src/main/Models/EOL_scripts/`
    - This directory contains the EOL script used to manipulate and query the EMF models:
      - `dataExtraction.eol`: Script to extract data from the models.

### Build and Dependencies

- **Maven Configuration**
  - `pom.xml`
    - Maven configuration file that lists the dependencies and build configurations for the project.

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- Maven 3.6.0 or higher
- Eclipse Modeling Framework (EMF)
- Eclipse Epsilon 

### Building the Project

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/genericRecommenderSystem.git
   cd genericRecommenderSystem
2. Build the project using Maven:
   ```bash
    mvn clean install
3. Running the application:
  - Navigate to the src/main/java/genericRecommenderSystem/ directory.
  - Run the Main.java file using Eclipse.

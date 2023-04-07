## Requirements

For building and running the application you need:
- [JDK 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- Mysql Database 5.5 minimum

# Runnig this project

1. Clone this project https://github.com/webmasterfayz/customerDataImport.git 
2. create databases schema in mysql - **customer_db**
3. edit **username** and **password** in **applicaton.properties** file ,location: customerDataImport/src/main/resources/application.properties
4. add customer data file location: customerDataImport/src/main/resources/data/ AND Filename: customerList.txt
5. Import the project using intellij idea IDE and  run as Java Application
6. browse context path : /dataImport and wait 15 min after that check the database and csv location: customerDataImport/src/main/resources/data/
## Intro
CSV2JDBC is a jdbc driver proxy which can aggregate features to any other JDBC driver,
currently supported features are:

### Import CSV file from client machine to a database Table
The following statement will create table `MY_TABLE` with all columns existing in `file.csv` then import 
the data to the table.
```sql
CSV2J COPY MY_TABLE 
FROM '/tmp/file.csv' 
WITH CSV HEADER CREATE_TABLE DELIMITER ','
```

### Export Select statement to a CSV file
The following statement will extract the select statement to  `file.csv` 
```sql
CSV2J COPY (
  SELECT 1 AS ID, 10.99 AS AMOUNT, TIMESTAMP '2022-01-31 23:59:58.987' AS DAT_CREATION
  UNION ALL
  SELECT 2 AS ID, 7.50 AS AMOUNT, TIMESTAMP '2023-01-31 21:59:58.987' AS DAT_CREATION
) TO '/tmp/file.csv' WITH CSV HEADER
```

## Using

### SQL client software application
CSV2JDBC can be used in tools like DBeaver, SquirrelSQL, Oracle SQL Developer,
or any other SQL client software application which supports jdbc drivers, use the information bellow to connect:

* The driver: Add csv2jdbc.jar and the target database which will be used
* The driver class: `com.mageddo.csv2jdbc.Csv2JdbcDriver`
* Connection URL: 
  * Template: `jdbc:csv2jdbc:${TARGET_DB_JDBC_URL}?delegateDriverClassName=${TARGET_DATABSE_DRIVER_CLASS_NAME}`
  * H2 example: `jdbc:csv2jdbc:h2:mem:testdb?delegateDriverClassName=org.h2.Driver` 
  * Postgres example: `jdbc:postgresql://localhost:5432/db?currentSchema=public&delegateDriverClassName=org.postgresql.Driver` 
* Username and password: As usual

### Configuring in your app 

Configuring Dependency

```groovy
implementation 'com.mageddo.csv2jdbc:csv2jdbc:0.2.1'
implementation 'com.h2database:h2:0.2.0' // or any other database driver you want
```

Setup the Driver and loading a csv to TEMP_TABLE, H2 Database example:

```java
public static void main(String[] args) throws Exception {

  // connecting to the database using the csv2jdbc
  Class.forName("com.mageddo.csv2jdbc.Csv2JdbcDriver");
  final String jdbcUrl = "jdbc:csv2jdbc:h2:mem:testdb?delegateDriverClassName=org.h2.Driver";
  final var conn = DriverManager.getConnection(jdbcUrl, "SA", "");

  // creating a temp csv to make a sample import
  final var csvFile = Files.createTempFile("file", ".csv");
  Files.writeString(csvFile, "ID,NAME\n1,Ana\n2,Mario");

  // Importing the csv file to the TEMP_TABLE1
  try (Statement stm = conn.createStatement()) {
    final String sql = String.format("CSV2J COPY TEMP_TABLE1 FROM '%s' WITH CSV HEADER CREATE_TABLE", csvFile);
    final int affected = stm.executeUpdate(sql);
    System.out.println("> Updated: " + affected);
  }
  
  // Selecting and printing the imported data
  System.out.println("> Data inserted:");
  try(
      PreparedStatement stm = conn.prepareStatement("SELECT * FROM TEMP_TABLE1");
      ResultSet rs = stm.executeQuery()
  ){
    while (rs.next()){
      System.out.printf("id=%s, name=%s%n", rs.getString("ID"), rs.getString("NAME"));
    }
  }
}

/*

> Updated: 2
> Data inserted:
id=1, name=Ana
id=2, name=Mario

*/
```


## Building the project yourself

### Build and install dep locally

```bash
./gradlew clean build publishToMavenLocal
```

### Build, Publish to Sonatype and Release

```bash
./gradlew clean release build publishToMavenCentral closeAndReleaseMavenCentralStagingRepository
```

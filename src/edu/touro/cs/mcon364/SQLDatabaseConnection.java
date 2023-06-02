package edu.touro.cs.mcon364;


// https://docs.microsoft.com/en-us/sql/connect/jdbc/step-3-proof-of-concept-connecting-to-sql-using-java?view=sql-server-2017
import java.sql.*;
import java.util.Map;
import java.util.Set;
//https://docs.microsoft.com/en-us/sql/connect/jdbc/download-microsoft-jdbc-driver-for-sql-server?view=sql-server-ver15
//https://docs.oracle.com/javase/tutorial/jdbc/basics/prepared.html
//https://stackoverflow.com/questions/1582161/how-does-a-preparedstatement-avoid-or-prevent-sql-injection


/**
 * Class which deals with the making, adding and removing from the AWS Data Base.
 * Class is a Singleton because the C-tor makes a new table which I only want to happen once per program.
 * TODO: Drop table method which is called at the start of each scrape in order to remove all the data from prev scrapes
 * @author Daniel Crespin
 *
 */

public class SQLDatabaseConnection {
    private final String TABLE_NAME = "Emails";
    private static SQLDatabaseConnection instance;
    private String connectionUrl;
    private Connection connection;
    // Connect to your database.
    // Replace server name, username, and password with your credentials



    public static SQLDatabaseConnection getInstance() throws ClassNotFoundException {
        if (instance == null) { //Double check locking for the singleton instantiation
            synchronized (SQLDatabaseConnection.class) {
                if (instance == null) {
                    instance = new SQLDatabaseConnection();
                }
            }
        }
        return instance;
    }

    private SQLDatabaseConnection() throws ClassNotFoundException {
        Map<String, String> env = System.getenv();
        String endpoint = env.get("Endpoint"); // TODO: Get var with the following code: env.get()
        String admin = env.get("Admin");
        String password = env.get("Password");
        System.out.println(endpoint);
        connectionUrl = // specifies how to connect to the database
                "jdbc:sqlserver://" + endpoint + ";"
                        + "database=Crespin_Daniel;"
                        + admin + ";"//TODO: set env variable
                        + password + ";" //TODO: set env variable
                        + "encrypt=false;"
                        + "trustServerCertificate=false;"
                        + "loginTimeout=30;";
        ResultSet resultSet = null;
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        createEmailsTable(connectionUrl);


    }

    private void createEmailsTable(String connectionUrl) {
        try (Connection connection = DriverManager.getConnection(connectionUrl);
             Statement statement = connection.createStatement()) {
            String createTableSql = "IF OBJECT_ID('" + TABLE_NAME + "', 'U') IS NULL BEGIN CREATE TABLE " + TABLE_NAME + " ("
                    + "EmailID INT IDENTITY(1,1) PRIMARY KEY,"
                    + "EmailAdress VARCHAR(255) NOT NULL,"
                    + "Source VARCHAR(255) NOT NULL,"
                    + "TIMESTAMP DATETIME NOT NULL"
                    + "); END";
            statement.execute(createTableSql);
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
    }





    public boolean insertEmails(Set<EmailData> emails) {
        String insertSql = "INSERT INTO " + TABLE_NAME + " (EmailAdress, Source, TIMESTAMP) VALUES (?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(connectionUrl);
             PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
            for (EmailData email : emails) {
                preparedStatement.setString(1, email.getEmail());
                preparedStatement.setString(2, email.getHrefSource());
                preparedStatement.setTimestamp(3, email.getTimeSaved());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public void clearDataFromTables() throws SQLException {
        String sqlClearTable = "DELETE FROM " + TABLE_NAME;
        try (Connection connection = DriverManager.getConnection(connectionUrl);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sqlClearTable);
        }
    }

}









//        String insertSql2 = "INSERT INTO Students (FirstName, LastName) VALUES (?, ?);";
//        try (Connection connection = DriverManager.getConnection(connectionUrl);
//             PreparedStatement prepsInsertProduct = connection.prepareStatement(insertSql2, Statement.RETURN_GENERATED_KEYS);) {
//            {
//                prepsInsertProduct.setString(1,"Var");
//                prepsInsertProduct.setString(2,"Eg");
//                prepsInsertProduct.execute();
//
//                resultSet = prepsInsertProduct.getGeneratedKeys();
//                while (resultSet.next()) {
//                    System.out.println(resultSet.getInt(1));
//                }
//            }} catch (SQLException throwables) {
//            throwables.printStackTrace();
//        }

//        try (Connection connection = DriverManager.getConnection(connectionUrl); // AutoCloseable
//             Statement statement = connection.createStatement();)
//        {
//            // Create and execute a SELECT SQL statement.
//            String selectSql = "INSERT INTO table_name (example1)" +
//                    "VALUES (value1);"; // Guardrails
//            resultSet = statement.executeQuery(selectSql);
//
//            // Print results from select statement
//            while (resultSet.next()) {
//                System.out.println(resultSet.getString(2) + " " + resultSet.getString(3));
//            }
//        }
//        catch (SQLException e) {
//            e.printStackTrace();
//        }

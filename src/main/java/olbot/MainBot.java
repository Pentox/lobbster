package olbot;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MainBot {

	static IDiscordClient client;
	static final String DRIVER = "com.mysql.jdbc.Driver";
	static String DATABASE_ADDRESS;
	static String USERNAME;
	static String PASSWORD;
	static final String MAX_POOL = "250";

	public static void main(String[] args) {
	DATABASE_ADDRESS = System.getenv("DATABASE_ADDRESS");
	USERNAME = System.getenv("USERNAME");
	PASSWORD = System.getenv("PASSWORD");
	client  = InitBot.createClient(System.getenv("TOKEN"), true); // Gets the client object (from the first example)
	EventDispatcher dispatcher = client.getDispatcher(); // Gets the EventDispatcher instance for this client instance

	
		try {
			connect();
		dispatcher.registerListener(new Handler()); // Registers the IListener example class from above
	}
	catch (Exception ex) {
		ex.printStackTrace();
		System.out.println("An error occured while connecting to the database. Please"
				+ " make sure your entered the correct credentials.");
	}
}

static Connection connection;
    static Properties properties;
    
    // create properties
    private static Properties getProperties () {
        if (properties == null) {
            properties = new Properties();
            properties.setProperty("user", USERNAME);
            properties.setProperty("password", PASSWORD);
            properties.setProperty("MaxPooledStatements", MAX_POOL);
        }
        return properties;
    }

    // connect database
    public static Connection connect () throws SQLException, ClassNotFoundException{
        if (connection == null) {
        	Class.forName(DRIVER);
            connection = DriverManager.getConnection(DATABASE_ADDRESS, getProperties());
        }
        return connection;
    }

    // disconnect database
    public static void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

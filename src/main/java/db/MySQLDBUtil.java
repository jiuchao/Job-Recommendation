package db;

public class MySQLDBUtil {
	private static final String INSTANCE = "YOURINSTANCE";
	private static final String PORT_NUM = "YOURPORTNUMBER";
	public static final String DB_NAME = "YOURDBNAME";
	private static final String USERNAME = "YOURUSERNAME";
	private static final String PASSWORD = "YOURPASSWORD";
	public static final String URL = "jdbc:mysql://"
			+ INSTANCE + ":" + PORT_NUM + "/" + DB_NAME
			+ "?user=" + USERNAME + "&password=" + PASSWORD
			+ "&autoReconnect=true&serverTimezone=UTC";
}

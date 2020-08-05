package it.unimo.crime_analysis;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class DBManager {
	public static final String JDBCURL = "jdbc:postgresql://localhost:5532/crime_news?"
			+ "user=crime_news_ro&password=crime_news_ro";
			//+ "&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=CET";
	protected PreparedStatement statement;
	protected Connection connection;

	public DBManager(String JDBCURL) throws ClassNotFoundException, SQLException {
		connection = DriverManager.getConnection(JDBCURL);
		//statement = connection.createStatement();
		String sql = "SELECT title, description, text FROM crime_news.news LIMIT 2500;";
		statement = connection.prepareStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
		statement.setQueryTimeout(30); 
	}

	public ResultSet executeQuery() throws SQLException {
		return statement.executeQuery();
	}

	public int executeUpdate() throws SQLException {
		return statement.executeUpdate();
	}

	public void close() throws SQLException {
		if (connection != null) {
			statement.close();
			connection.close();
		}
	}
}

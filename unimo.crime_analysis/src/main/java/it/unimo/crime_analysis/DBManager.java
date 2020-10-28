package it.unimo.crime_analysis;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class DBManager {
	public static final String JDBCURL = "jdbc:postgresql://localhost:5532/crime_news?"
			+ "user=crime_news_ro&password=crime_news_ro";
	protected Statement statement;
	protected Connection connection;

	public DBManager(String JDBCURL) throws ClassNotFoundException, SQLException {
		connection = DriverManager.getConnection(JDBCURL);
		statement = connection.createStatement();
		statement.setQueryTimeout(30); 
	}

	public ResultSet executeQuery() throws SQLException {
		String sql = "SELECT title, description, text, date, tag, municipality FROM crime_news.news;";
		return statement.executeQuery(sql);
	}

	public int executeUpdate(String update) throws SQLException {
		return statement.executeUpdate(update);
	}

	public void close() throws SQLException {
		if (connection != null) {
			statement.close();
			connection.close();
		}
	}
}

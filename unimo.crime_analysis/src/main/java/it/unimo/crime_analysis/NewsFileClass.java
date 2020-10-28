package it.unimo.crime_analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NewsFileClass {
	private DBManager db;
	private List<Notice> crimes;
	private File file;

	public NewsFileClass(){
		file = new File("news.txt");
		crimes = dbExtraction();
		createFormattedFile(file);
	}

	public List<Notice> getCrimes() {
		return crimes;
	}

	public File getFile() {
		return file;
	}

	private void dbConnection() {
		try {
			db = new DBManager(DBManager.JDBCURL);
		} catch (ClassNotFoundException e) {
			System.out.println("Missign lib...");
			throw new RuntimeException();
		} catch (SQLException e) {
			System.out.println("Error trying to connect to db");
			e.printStackTrace();
		}
	}

	private List<Notice> dbExtraction() {
		dbConnection();
		ResultSet rs = null;
		crimes = new ArrayList<Notice>();
		try {
			rs = db.executeQuery();
			while(rs.next()) {
				crimes.add((new Notice(rs.getString("title"), rs.getString("description"),
						rs.getString("text"), rs.getDate("date"), rs.getString("tag"), rs.getString("municipality"))));
			}
			rs.close();
			db.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return crimes;
	}

	private void createFormattedFile(File file) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		@SuppressWarnings("resource")
		BufferedWriter bw = new BufferedWriter(fw);
		for (Notice notice : crimes) {
			try {
				bw.write(notice + "\n");
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException();
			}
		}
		try {
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}

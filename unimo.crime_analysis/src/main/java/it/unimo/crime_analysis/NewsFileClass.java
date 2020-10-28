package it.unimo.crime_analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NewsFileClass {
	private DBManager db;
	private List<Notizia> crimes;
	private File file;
	
	public NewsFileClass(){
		file = new File("news.txt");
		if (!file.exists()) {
			crimes = dbExtraction();
			createFormattedFile(file);
		}
		else
			readFormattedFile(file);
	}
	
	public List<Notizia> getCrimes() {
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

	private List<Notizia> dbExtraction() {
		dbConnection();
		ResultSet rs = null;
		crimes = new ArrayList<Notizia>();
		try {
			rs = db.executeQuery();
			while(rs.next()) {
				crimes.add((new Notizia(rs.getString("title"), 
						rs.getString("description"), rs.getString("text"))));
			}
			rs.close();
			db.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return crimes;
	}

	public void createFormattedFile(File file) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		@SuppressWarnings("resource")
		BufferedWriter bw = new BufferedWriter(fw);
		for (Notizia notice : crimes) {
			try {
				bw.write(notice.toString());
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
	
	public void readFormattedFile(File file) {
		FileReader fr = null;
		try {
			fr = new FileReader(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		BufferedReader br = new BufferedReader(fr);
		String all;
		try {
			while((all = br.readLine()) != null) {
				crimes.add(new Notizia(all));
			}
			br.close();
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

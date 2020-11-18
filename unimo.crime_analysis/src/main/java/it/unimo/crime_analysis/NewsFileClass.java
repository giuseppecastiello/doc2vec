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
		crimes = dbNewsExtraction();
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
			System.out.println("Missing PostgreSql jar...");
			throw new RuntimeException();
		} catch (SQLException e) {
			System.out.println("Error trying to connect to db");
			e.printStackTrace();
		}
	}

	private List<Notice> dbNewsExtraction() {
		dbConnection();
		ResultSet rs = null;
		crimes = new ArrayList<Notice>();
		String sql = "SELECT title, description, text, date, tag, municipality, date_event, id FROM crime_news.news  join crime_news.link on crime_news.news.url = crime_news.link.url WHERE EXTRACT(YEAR FROM date) < 2020 OR EXTRACT(MONTH FROM date) < 7;";
		try {
			rs = db.executeQuery(sql);
			while(rs.next()) {
				crimes.add((new Notice(rs.getString("title"), rs.getString("description"), rs.getString("text"), 
						rs.getDate("date"), rs.getString("tag"), rs.getString("municipality"), rs.getDate("date_event"), rs.getInt("id"))));
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return crimes;
	}

	public List<DuplicateCouple> dbDuplicateExtraction() {
		dbConnection();
		ResultSet rs = null;
		List<DuplicateCouple> dup = new ArrayList<DuplicateCouple>();
		String sql = "SELECT id_news1, id_news2 FROM crime_news.duplicate WHERE id_news1 in (" + 
				"SELECT * FROM idNewsJen2020) and id_news2 in (" +
				"SELECT * FROM idNewsJen2020);";
		try {
			rs = db.executeQuery(sql);
			while(rs.next()) {
				dup.add((new DuplicateCouple(rs.getInt("id_news1"), rs.getInt("id_news2"))));
			}
			rs.close();
			//db.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return dup;
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
	
	public void printDupOfMT(List<DuplicateCouple> dup) {
		int c = 0;
		for (DuplicateCouple dc: dup) {
			ResultSet rs = null;
			try {
				String s = "SELECT n1.newspaper as np1, n2.newspaper as np2"
						+ " FROM (crime_news.link as l1 join crime_news.news as n1 on n1.url = l1.url) join" + 
						" (crime_news.link as l2 join crime_news.news as n2 on n2.url = l2.url) on l1.id <> l2.id"
						+ String.format(" WHERE l1.id = %d and l2.id = %d", dc.id1, dc.id2) + 
						" and lower(n1.newspaper) like 'g%' and lower(n2.newspaper) like 'g%'";
				//System.out.println(s);
				rs = db.executeQuery(s);
				if (rs.next()) {
					System.out.println(dc.id1 + " " + rs.getString("np1") + " " + dc.id2 + " " + rs.getString("np2"));
					c++;
				}	
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println(c);
	}
	
	public void close() {
		try {
			db.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}

package it.unimo.crime_analysis;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GSFile {
	private List<DuplicateCouple> dup;
	
	public GSFile() {
		FileReader fr = null;
		try {
			 fr = new FileReader("GoldStandardFull.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		BufferedReader br = new BufferedReader(fr);
		dup = new ArrayList<DuplicateCouple>();
		String line1 = null, line3 = null;
		try {
			while ((line1 = br.readLine()) != null &&
					br.readLine() != null &&
					(line3 = br.readLine()) != null &&
					br.readLine() != null &&
					br.readLine() != null) {
				StringBuilder sb = new StringBuilder(line1);
				sb.delete(sb.indexOf(" "), sb.length());
				line1 = sb.toString();
				sb = new StringBuilder(line3);
				sb.delete(sb.indexOf(" "), sb.length());
				line3 = sb.toString();
				dup.add(new DuplicateCouple(Integer.parseInt(line1), Integer.parseInt(line3)));
			}
			br.close();
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<DuplicateCouple> getDup() {
		return dup;
	}
}

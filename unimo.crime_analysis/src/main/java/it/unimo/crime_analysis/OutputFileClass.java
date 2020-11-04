package it.unimo.crime_analysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class OutputFileClass {
	private BufferedWriter bw;
	
	public OutputFileClass(String filename) {
		createOutputFile(filename);
	}
	
	private void createOutputFile(String filename) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(filename);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		bw = new BufferedWriter(fw);
	}
	
	public void write(String str) {
		try {
			bw.write(str + "\n");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
	
	public void close() {
		try {
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}

package it.unimo.crime_analysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class OutputFileClass {
	private BufferedWriter bw;
	
	public OutputFileClass() {
		createOutputFile();
	}
	
	private void createOutputFile() {
		FileWriter fw = null;
		try {
			fw = new FileWriter("output.txt");
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

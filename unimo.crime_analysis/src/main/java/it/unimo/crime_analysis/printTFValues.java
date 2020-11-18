package it.unimo.crime_analysis;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class printTFValues {
	static int tp = 0, tn, fp, fn;
	static float p = 0, r = 0, a = 0, f = 0;
	
	private static void printValues(String th, int tp, int fp, int fn, int tn, float p, float r, float a, float f) {
		System.out.println(th);
		System.out.println("tp = " + tp);
		System.out.println("fp = " + fp);
		System.out.println("fn = " + fn);
		System.out.println("tn = " + tn);
		System.out.println(String.format("precision = %.2f", p));
		System.out.println(String.format("recall = %.2f", r));
		System.out.println(String.format("accuracy = %.2f", a));
		System.out.println(String.format("f1-score = %.2f\n", f));
	}
	
	private static void extractValues(String th, BufferedReader br) {
		try {
			String l1, ps, rs, as, fs;
			if ((l1 = br.readLine()) != null &&
					br.readLine() != null &&
					br.readLine() != null &&
					(ps = br.readLine()) != null &&
					(rs = br.readLine()) != null &&
					(as = br.readLine()) != null &&
					(fs = br.readLine()) != null) {
				tp = Integer.parseInt(l1.split(" ")[3]);
				p = Float.parseFloat(ps.split(" ")[1]);
				r = Float.parseFloat(rs.split(" ")[1]);
				a = Float.parseFloat(as.split(" ")[1]);
				f = Float.parseFloat(fs.split(" ")[2]);
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} 
		fp = Math.round((tp / p) - tp);
		fn = Math.round((tp / r) - tp);
		tn = 16653 - tp - fp - fn;
		printValues(th, tp, fp, fn, tn, p, r, a, f);
	}
	
	private static void printTFD2V(double t) {
		String filename = String.format("dati_2D/OutCompare%.2f.txt", t);
		String th = String.format("T = %.2f", t);
		FileReader fr;
		try {
			fr = new FileReader(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		BufferedReader br = new BufferedReader(fr);
		extractValues(th, br);
	}

	private static void printTFD2VF(double t1, double t2) {
		String filename = String.format("dati_3D/OutCompare%.2f_%.2f.txt", t1, t2);
		String th = String.format("T1 = %.2f , T2 = %.2f", t1, t2);
		FileReader fr;
		try {
			fr = new FileReader(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		BufferedReader br = new BufferedReader(fr);
		extractValues(th, br);
	}

	public static void main(String[] args) {
		printTFD2V(0.21);
		printTFD2V(0.25);
		printTFD2V(0.26);
		printTFD2V(0.27);
		printTFD2V(0.28);
		printTFD2V(0.29);
		printTFD2V(0.33);
		printTFD2V(0.40);
		printTFD2VF(0.15, 0.45);
		printTFD2VF(0.20, 0.45);
		printTFD2VF(0.20, 0.40);
		printTFD2VF(0.25, 0.45);
		printTFD2VF(0.25, 0.40);
		printTFD2VF(0.25, 0.35);
		printTFD2VF(0.25, 0.30);
		printTFD2VF(0.25, 0.25);
		printTFD2VF(0.30, 0.45);
		printTFD2VF(0.30, 0.40);
		printTFD2VF(0.30, 0.35);
	}
}

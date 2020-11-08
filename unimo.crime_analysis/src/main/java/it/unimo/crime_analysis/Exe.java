package it.unimo.crime_analysis;
import java.io.File;
import java.io.FileNotFoundException;
/*
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.time.StopWatch;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.AbstractCache;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.documentiterator.LabelsSource;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;

public class Exe {
	static NewsFileClass news = new NewsFileClass();
	static List<Notice> crimes = news.getCrimes();;
	static ParagraphVectors vec;
	static List<DuplicateCouple> dupGS = readGoldStandard();
	static List<DuplicateCouple> duplicatesFromDB = news.dbDuplicateExtraction();
	/*
	private static void createTokenizedFile(List<Notizia> crimes, TokenizerFactory t) {
		FileWriter fw = null;
		try {
			fw = new FileWriter("tokenized.txt");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		@SuppressWarnings("resource")
		BufferedWriter bw = new BufferedWriter(fw);
		for (Notice notice : crimes) {
			try {
				bw.write(t.create(notice.toString()).getTokens().toString() + "\n");
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
	 */

	private static ParagraphVectors trainModel(File input) {
		SentenceIterator iter;
		try {
			iter = new BasicLineIterator(input);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		AbstractCache<VocabWord> cache = new AbstractCache<>();
		LabelsSource source = new LabelsSource("DOC_");
		TokenizerFactory t = new DefaultTokenizerFactory();
		t.setTokenPreProcessor(new CommonPreprocessor());
		//createTokenizedFile(crimes, t);

		ArrayList<String> stopWords = new ArrayList<String>();
		stopWords.addAll(Arrays.asList("-", "+", "*", "/", "«", "»", "“", "”", "'", " "));

		ParagraphVectors vec = new ParagraphVectors.Builder()
				.minWordFrequency(1)
				.iterations(5) //6
				.epochs(2)
				.layerSize(300)
				.stopWords(stopWords)
				.learningRate(0.040)
				.labelsSource(source)
				.windowSize(7) //10 //5
				.iterate(iter)
				.trainWordVectors(false)
				.vocabCache(cache)
				.tokenizerFactory(t)
				.build();

		vec.fit();
		return vec;
	}


	private static List<DuplicateCouple> CrimeAnalysisWithFilters(double threshold_down, double threshold_up, List<Notice> list) {
		double all_similarity;
		//outputFileClass out = new OutputFileClass("OutWFilters.txt");
		List<DuplicateCouple> dup = new ArrayList<DuplicateCouple>();

		for (int i = 0; i < list.size(); i++) {
			Notice ni, nj;
			ni = list.get(i);
			for (int j = i + 1; j < list.size(); j++) {
				nj = list.get(j);
				if (!ni.isInWindowWith(nj))
					continue;
				if (list != crimes)
					all_similarity = vec.similarity("DOC_" + crimes.indexOf(ni), "DOC_" + crimes.indexOf(nj));
				else
					all_similarity = vec.similarity("DOC_" + i, "DOC_" + j);
				if (all_similarity < threshold_down)
					continue;
				if (all_similarity > threshold_up || ni.borderLineCompare(nj, all_similarity, threshold_down, threshold_up)) {

					//out.writeln("DOC_" + i + ", DOC_" + j);
					//out.writeln(ni.toString());
					//out.writeln(nj.toString());
					//out.writeln("all_similarity: " + all_similarity + "\n");
					dup.add(new DuplicateCouple(ni.getId(), nj.getId()));
				}
			}
		}
		//out.close();
		return dup;
	}

	private static List<DuplicateCouple> CrimeAnalysisNoFilters(double threshold, List<Notice> list) {
		double all_similarity;
		//OutputFileClass out = new OutputFileClass("OutNFilters.txt");
		List<DuplicateCouple> dup = new ArrayList<DuplicateCouple>();

		for (int i = 0; i < list.size(); i++) {
			Notice ni, nj;
			ni = list.get(i);
			for (int j = i + 1; j < list.size(); j++) {
				nj = list.get(j);
				if (list != crimes)
					all_similarity = vec.similarity("DOC_" + crimes.indexOf(ni), "DOC_" + crimes.indexOf(nj));
				else
					all_similarity = vec.similarity("DOC_" + i, "DOC_" + j);
				if (all_similarity > threshold) {
					//out.writeln("DOC_" + i + ", DOC_" + j);
					//out.writeln(ni.toString());
					//out.writeln(nj.toString());
					//out.writeln("all_similarity: " + all_similarity + "\n");
					dup.add(new DuplicateCouple(ni.getId(), nj.getId()));
				}
			}
		}
		//out.close();
		return dup;
	}


	private static List<DuplicateCouple> readGoldStandard() {
		return new GSFile().getDup();
	}

	private static void printIndex(String alg, double precision, double recall, double accuracy, double f1Score, OutputFileClass out) {
		out.writeln("\n" + alg + ":");
		out.writeln("Precision: " + precision);
		out.writeln("Recall: " + recall);
		out.writeln("Accuracy: " + accuracy);
		out.writeln("F1 Score: " + f1Score);
	}

	private static void printIndex(String alg, double precision, double recall, double accuracy, double f1Score, long time, OutputFileClass out) {
		printIndex(alg, precision, recall, accuracy, f1Score, out);
		out.writeln("Time: " + time + " ms");
	}

	private static List<Notice> getNewsOfJen2020(){
		List<Notice> newsOfJen2020 = new ArrayList<Notice>();
		for (Notice notice: crimes) {
			if (notice.isInGen2020())
				newsOfJen2020.add(notice);
		}
		return newsOfJen2020;
	}
	
	private static double CompareAlgorithms(double thresholdWF_d, double thresholdWF_u, double thresholdNF, boolean WF) {
		List<Notice> newsOfJen2020 = getNewsOfJen2020();
		StopWatch watch = new StopWatch();
		List<DuplicateCouple> dupWF = null;
		long timeWF = 0L;
		if (WF) {
			watch.start();
			dupWF = CrimeAnalysisWithFilters(thresholdWF_d, thresholdWF_u, newsOfJen2020);
			watch.stop();
			timeWF = watch.getTime();
		}
		long timeNF = 0L;
		List<DuplicateCouple> dupNF = null;
		if (!WF) {
			watch.start();
			dupNF = CrimeAnalysisNoFilters(thresholdNF, newsOfJen2020);
			watch.stop();
			timeNF = watch.getTime();
		}

		List<DuplicateCouple> dupDB = new ArrayList<DuplicateCouple>(duplicatesFromDB);
		int tpWF = 0, tpNF = 0, tpDB = 0;
		int tnWF, tnNF, tnDB;
		int fpWF, fpNF, fpDB;
		int fnWF = 0, fnNF = 0, fnDB = 0;
		String filename;
		if (WF)
			filename = "OutCompare" + thresholdWF_d + "_" + thresholdWF_u + ".txt";
		else
			filename = "OutCompare" + thresholdNF + ".txt";
		OutputFileClass out = new OutputFileClass(filename);
		//out.writeln("id1 \tid2 \tWF\t\tNF\t\tDB");
		for (int i = 0; i < dupGS.size(); i++) {
			boolean wf = false, nf = false, db = false;
			if (WF && dupWF.contains(dupGS.get(i))) {
				dupWF.remove(dupGS.get(i));
				wf = true;
				tpWF++;
			} else
				fnWF++;
			if (!WF && dupNF.contains(dupGS.get(i))) {
				dupNF.remove(dupGS.get(i));
				nf = true;
				tpNF++;
			} else
				fnNF++;
			/*
			if (dupDB.contains(dupGS.get(i))) {
				dupDB.remove(dupGS.get(i));
				db = true;
				tpDB++;
			} else
				fnDB++;
			 */
			//out.writeln(dupGS.get(i) + "\t" + wf + "\t" + nf + "\t" + db);
		}
		if (WF) {
			out.writeln("GS: " + dupGS.size() + " \tWF: " + tpWF);
			fpWF = dupWF.size();
			fpNF = 0;
		}
		else {
			out.writeln("GS: " + dupGS.size() + " \tNF: " + tpNF);
			fpWF = 0;
			fpNF = dupNF.size();
		}
		//fpDB = dupDB.size();
		int tot = newsOfJen2020.size();
		tnWF = tot - tpWF - fnWF - fpWF;
		tnNF = tot - tpNF - fnNF - fpNF;
		//tnDB = tot - tpDB - fnDB - fpDB;
		double precWF = ((double) tpWF) / (tpWF + fpWF), recWF = ((double) tpWF) / (tpWF + fnWF), accWF = ((double) tpWF + tnWF) / tot, f1WF = 2 * precWF * recWF / (precWF + recWF);
		double precNF = ((double) tpNF) / (tpNF + fpNF), recNF = ((double) tpNF) / (tpNF + fnNF), accNF = ((double) tpNF + tnNF) / tot, f1NF = 2 * precNF * recNF / (precNF + recNF);
		//double precDB = ((double) tpDB) / (tpDB + fpDB), recDB = ((double) tpDB) / (tpDB + fnDB), accDB = ((double) tpDB + tnDB) / tot, f1DB = 2 * precDB * recDB / (precDB + recDB);

		if (WF)
			printIndex("CrimeAnalysisWithFilters " + thresholdWF_d + "-" + thresholdWF_u, precWF, recWF, accWF, f1WF, timeWF, out);
		else
			printIndex("CrimeAnalysisNoFilters " + thresholdNF, precNF, recNF, accNF, f1NF, timeNF, out);
		//printIndex("CRIMEDB", precDB, recDB, accDB, f1DB, out);
		out.close();
		if (WF)
			return precWF + recWF + accWF + f1WF;
		else
			return precNF + recNF + accNF + f1NF;
	}

	public static void main(String[] args) {

		vec = trainModel(news.getFile());
		StopWatch watch = new StopWatch();
		watch.start();
		CrimeAnalysisNoFilters(0.29, crimes);
		watch.stop();
		long time = watch.getTime();
		System.out.print(time);
		/*
		double t_d, t_u = 0.54, t = 0.50;
		double t_dm = 0, t_um = 0, t_m = 0;
		double res, max = 0.;
		
		while (t_u >= 0.2) {
			t_d = 0.30;
			if (t_d > t_u)
				t_d = t_u;
			while (t_d >= 0.2) {
				res = CompareAlgorithms(t_d, t_u, t, true);
				if (res > max) {
					t_dm = t_d;
					t_um = t_u;
					max = res;
				}
				t_d -= 0.01;
			}
			t_u -= 0.01;
		}
		System.out.println("MAXWF: " + max);
		System.out.println("td: " + t_dm + "  tu: " + t_um);
		/*
		max = 0.;
		while (t >= 0.2) {
			t_d = 0.30;
			res = CompareAlgorithms(t_d, t_u, t, false);
			if (res > max) {
				t_m = t;
				max = res;
			}
			t -= 0.01;
		}
		System.out.println("MAXNF: " + max);
		System.out.println("t: " + t_m);
		*/
		news.close();
	}
}

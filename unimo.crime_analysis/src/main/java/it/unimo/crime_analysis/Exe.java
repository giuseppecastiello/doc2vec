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
	static List<Notice> crimes;
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
				.sampling(0)
				.build();

		vec.fit();
		return vec;
	}


	private static List<DuplicateCouple> CrimeAnalysisWithFilters(double threshold_down, double threshold_up, boolean onlyJen2020) {
		double all_similarity;
		//outputFileClass out = new OutputFileClass("OutWFilters.txt");
		List<DuplicateCouple> dup = new ArrayList<DuplicateCouple>();

		for (int i = 0; i < crimes.size(); i++) {
			Notice ni, nj;
			ni = crimes.get(i);
			if (onlyJen2020 && !ni.isInGen2020())
				continue;
			for (int j = i + 1; j < crimes.size(); j++) {
				nj = crimes.get(j);
				if ((onlyJen2020 && !nj.isInGen2020()) || !ni.isInWindowWith(nj))
					continue;
				all_similarity = vec.similarity("DOC_" + i, "DOC_" + j);
				if (all_similarity < threshold_down)
					continue;
				if (all_similarity > threshold_up || ni.borderLineCompare(nj, all_similarity, threshold_up)) {

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

	private static List<DuplicateCouple> CrimeAnalysisNoFilters(double threshold, boolean onlyJen2020) {
		double all_similarity;
		//OutputFileClass out = new OutputFileClass("OutNFilters.txt");
		List<DuplicateCouple> dup = new ArrayList<DuplicateCouple>();

		for (int i = 0; i < crimes.size(); i++) {
			Notice ni, nj;
			ni = crimes.get(i);
			if (onlyJen2020 && !ni.isInGen2020())
				continue;
			for (int j = i + 1; j < crimes.size(); j++) {
				nj = crimes.get(j);
				if (onlyJen2020 && !nj.isInGen2020())
					continue;
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

	/*
	private static void CreateGoldStandardFull(){
		File gs = new File("GoldStandardFull.txt");
		if (gs.exists())	return;

		NewsFileClass news = new NewsFileClass(); 
		List<Notice> crimes = news.getCrimes();
		news.close();
		int c = 0;
		OutputFileClass out = new OutputFileClass("GoldStandardFull.txt");
		for (int i = 0; i < crimes.size(); i++) {
			Notice ni, nj;
			ni = crimes.get(i);
			if (!ni.isInGen2020())
				continue;
			for (int j = i + 1; j < crimes.size(); j++) {
				nj = crimes.get(j);
				if (!nj.isInGen2020() || !ni.isInWindowWith(nj))
					continue;
				out.writeln(ni.gsToString());
				out.writeln(nj.gsToString());
				out.writeln("");
				c++;
			}
		}
		out.close();
		System.out.println(c);
	}
	 */
	private static List<DuplicateCouple> readGoldStandard() {
		return new GSFile().getDup();
	}

	/*
	private static void CreateGoldStandard(){
		File gs = new File("GoldStandard.txt");
		if (gs.exists())	return;

		List<DuplicateCouple> dup = readGoldStandard();
		OutputFileClass out = new OutputFileClass("GoldStandard.txt");
		for (int i = 0; i < dup.size(); i++) {
			System.out.println(dup.get(i).id1 + " " + dup.get(i).id2);
			out.writeln(dup.get(i).id1 + " " + dup.get(i).id2);
		}
		out.close();
	}
	 */

	private static void prepareForCrimeAnalysis() {
		crimes = news.getCrimes();
		vec = trainModel(news.getFile());
	}

	private static void printIndex(String alg, double precision, double recall, double accuracy, OutputFileClass out) {
		out.writeln("\n" + alg + ":");
		out.writeln("Precision: " + precision);
		out.writeln("Recall: " + recall);
		out.writeln("Accuracy: " + accuracy);
	}

	private static void printIndex(String alg, double precision, double recall, double accuracy, long time, OutputFileClass out) {
		printIndex(alg, precision, recall, accuracy, out);
		out.writeln("Time: " + time + " ms");
	}

	private static double CompareAlgorithms(double thresholdWF_d, double thresholdWF_u, double thresholdNF, boolean NF) {
		StopWatch watch = new StopWatch();
		watch.start();
		List<DuplicateCouple> dupWF = CrimeAnalysisWithFilters(thresholdWF_d, thresholdWF_u, true);
		watch.stop();
		long timeWF = watch.getTime();
		watch.reset();
		long timeNF = 0L;
		List<DuplicateCouple> dupNF = null;
		if (NF) {
			watch.start();
			dupNF = CrimeAnalysisNoFilters(thresholdNF, true);
			watch.stop();
			timeNF = watch.getTime();
		}
		
		List<DuplicateCouple> dupDB = new ArrayList<DuplicateCouple>(duplicatesFromDB);
		int tpWF = 0, tpNF = 0, tpDB = 0;
		int tnWF, tnNF, tnDB;
		int fpWF, fpNF, fpDB;
		int fnWF = 0, fnNF = 0, fnDB = 0;
		OutputFileClass out = new OutputFileClass("OutCompare" + thresholdWF_d + "_" + thresholdWF_u + "-" + thresholdNF + ".txt");
		//out.writeln("id1 \tid2 \tWF\t\tNF\t\tDB");
		for (int i = 0; i < dupGS.size(); i++) {
			boolean wf = false, nf = false, db = false;
			if (dupWF.contains(dupGS.get(i))) {
				dupWF.remove(dupGS.get(i));
				wf = true;
				tpWF++;
			} else
				fnWF++;
			if (NF && dupNF.contains(dupGS.get(i))) {
				dupNF.remove(dupGS.get(i));
				nf = true;
				tpNF++;
			} else
				fnNF++;
			if (dupDB.contains(dupGS.get(i))) {
				dupDB.remove(dupGS.get(i));
				db = true;
				tpDB++;
			} else
				fnDB++;
			//out.writeln(dupGS.get(i) + "\t" + wf + "\t" + nf + "\t" + db);
		}
		out.writeln("GS: " + dupGS.size() + " \tWF: " + tpWF + " \tNF: " + tpNF + " \tDB: " + tpDB);

		fpWF = dupWF.size();
		fpNF = dupNF.size();
		fpDB = dupDB.size();
		int tot = crimes.size();
		tnWF = tot - tpWF - fnWF;
		tnNF = tot - tpNF - fnNF;
		tnDB = tot - tpDB - fnDB;
		double precWF = ((double) tpWF) / (tpWF + fpWF), recWF = ((double) tpWF) / (tpWF + fnWF), accWF = ((double) tpWF + tnWF) / tot;
		double precNF = ((double) tpNF) / (tpNF + fpNF), recNF = ((double) tpNF) / (tpNF + fnNF), accNF = ((double) tpNF + tnNF) / tot;
		double precDB = ((double) tpDB) / (tpDB + fpDB), recDB = ((double) tpDB) / (tpDB + fnDB), accDB = ((double) tpDB + tnDB) / tot;
		/*
		out.writeln("\nDuplicates out of gold standard (false positive): from CrimeAnalysisWithFilters");
		for (int i = 0; i < dupWF.size(); i++)
			out.writeln(dupWF.get(i).toString());
		out.writeln("\nDuplicates out of gold standard (false positive): from CrimeAnalysisNoFilters");
		for (int i = 0; i < dupNF.size(); i++)
			out.writeln(dupNF.get(i).toString());
		out.writeln("\nDuplicates out of gold standard (false positive): from CRIMEDB");
		for (int i = 0; i < dupDB.size(); i++)
			out.writeln(dupDB.get(i).toString());
		 */
		printIndex("CrimeAnalysisWithFilters " + thresholdWF_d + "-" + thresholdWF_u, precWF, recWF, accWF, timeWF, out);
		printIndex("CrimeAnalysisNoFilters " + thresholdNF, precNF, recNF, accNF, timeNF, out);
		printIndex("CRIMEDB", precDB, recDB, accDB, out);
		out.close();
		return precWF + recWF + accWF;
	}

	public static void main(String[] args) {
		prepareForCrimeAnalysis();
		//CrimeAnalysisWithFilters(false);
		double t_d, t_u = 0.75, t = 0.60;
		double t_dm = 0, t_um = 0;
		double res, max = 0;

		while (t_u >= 0.55) {
			t_d = 0.45;
			res = CompareAlgorithms(t_d, t_u, t, true);
			if (res > max) {
				t_dm = t_d;
				t_um = t_u;
			}
			while (t_d > 0.15) {
				t_d -= 0.01;
				res = CompareAlgorithms(t_d, t_u, t, false);
				if (res > max) {
					t_dm = t_d;
					t_um = t_u;
				}
			}
			t -= 0.01;
			t_u -= 0.01;
		}
		news.close();
		System.out.println("MAX: " + max);
		System.out.println("td: " + t_dm + "  tu: " + t_um);
	}
}

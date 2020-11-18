package it.unimo.crime_analysis;
import java.io.File;

import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;
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
	static List<Notice> crimes = news.getCrimes();
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


	private static List<DuplicateCouple> Doc2VecConFiltri(double t1, double t2, List<Notice> list) {
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
				if (all_similarity < t1)
					continue;
				if (all_similarity > t2 || ni.borderLineCompare(nj, all_similarity, t1, t2)) {

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

	private static List<DuplicateCouple> Doc2Vec(double threshold, List<Notice> list) {
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

	private static double CompareAlgorithms(double t1, double t2, double t, boolean WF) {
		List<Notice> newsOfJen2020 = getNewsOfJen2020();
		StopWatch watch = new StopWatch();
		List<DuplicateCouple> dup = null;
		watch.start();
		if (WF)
			dup = Doc2VecConFiltri(t1, t2, newsOfJen2020);
		else
			dup = Doc2Vec(t, newsOfJen2020);
		watch.stop();
		long time = watch.getTime();
		long tp = 0, tn, fp, fn = 0;
		String filename;
		if (WF)
			filename = String.format("OutCompare%.2f_%.2f.txt", t1, t2);
		else
			filename = String.format("OutCompare%.2f.txt", t);
		OutputFileClass out = new OutputFileClass(filename);
		for (DuplicateCouple dc: dupGS) {
			if (dup.contains(dc)) {
				dup.remove(dc);
				tp++;
			} else
				fn++;
		}
		out.writeln("GS: " + dupGS.size() + " \tA: " + tp);
		fp = dup.size();
		long tot = binomial(newsOfJen2020.size(), 2);
		tn = tot - tp - fn - fp;
		double prec, rec, acc, f1;
		if (tp + fp != 0)
			prec = (double) tp / (tp + fp);
		else
			prec = 0;
		if (tp + fn != 0)
			rec = (double) tp / (tp + fn);
		else
			rec = 0;
		if (tot != 0)
			acc  = ((double) tp  + tn) / tot;
		else
			acc = 0;
		if (prec  + rec != 0)
			f1  = 2 * prec  * rec  / (prec  + rec);
		else
			f1 = 0;

		if (WF)
			printIndex("Doc2VecConFiltri " + t1 + "-" + t2, prec, rec, acc, f1, time, out);
		else
			printIndex("Doc2Vec " + t, prec, rec, acc, f1, time, out);
		out.close();
		return prec + rec + acc + f1;
	}
	
	 private static long binomial(int n, int k)
	    {
	        if (k>n-k)
	            k=n-k;

	        long b=1;
	        for (int i=1, m=n; i<=k; i++, m--)
	            b=b*m/i;
	        return b;
	    }
	 
	

	public static void main(String[] args) {
		vec = trainModel(news.getFile());
		List<DuplicateCouple> dupD2V = Doc2Vec(0.27, crimes);
		List<DuplicateCouple> dupD2VF = Doc2VecConFiltri(0.25, 0.30, crimes);
		news.close();
	}
}

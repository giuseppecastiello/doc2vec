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
	static NewsFileClass news;
	static List<Notice> crimes;
	static ParagraphVectors vec;
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
	
	
	private static void CrimeAnalysisWithFilters(boolean onlyJen2020) {
		double all_similarity, threshold_down = 0.35, threshold_up = 0.7;
		OutputFileClass out = new OutputFileClass("OutWFilters.txt");
		int c = 0;
		
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
					c++;
					out.write("DOC_" + i + ", DOC_" + j);
					out.write(ni.toString());
					out.write(nj.toString());
					out.write("all_similarity: " + all_similarity + "\n");
				}
			}
		}
		out.close();
		System.out.println(c);
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
				out.write(ni.gsToString());
				out.write(nj.gsToString());
				out.write("");
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
			out.write(dup.get(i).id1 + " " + dup.get(i).id2);
		}
		out.close();
	}
	*/
	
	private static void prepareForCrimeAnalysis() {
		news = new NewsFileClass(); 
		crimes = news.getCrimes();
		vec = trainModel(news.getFile());
	}
	
	private static void CompareAlgorithms() {
		List<DuplicateCouple> dupGS = readGoldStandard();
		prepareForCrimeAnalysis();
		CrimeAnalysisWithFilters(true);
		
	}
	
	public static void main(String[] args) throws Exception {
		prepareForCrimeAnalysis();
		//CrimeAnalysisWithFilters(false);
		CompareAlgorithms();
	}
}

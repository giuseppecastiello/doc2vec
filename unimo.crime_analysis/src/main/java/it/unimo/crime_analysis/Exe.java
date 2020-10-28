package it.unimo.crime_analysis;
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
	public static void main(String[] args) throws Exception {
		
		NewsFileClass news = new NewsFileClass(); 
		List<Notice> crimes = news.getCrimes();
		
		SentenceIterator iter = new BasicLineIterator(news.getFile());
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
				.layerSize(250)
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
		
		double all_similarity, threshold_down = 0.35, threshold_up = 0.7;
		OutputFileClass out = new OutputFileClass();
		int c = 0;
		
		for (int i = 0; i < crimes.size(); i++) {
			Notice ni, nj;
			ni = crimes.get(i);
			for (int j = i + 1; j < crimes.size(); j++) {
				nj = crimes.get(j);
				if (!ni.isInWindowWith(nj))
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
}

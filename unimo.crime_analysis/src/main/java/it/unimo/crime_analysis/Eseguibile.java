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

public class Eseguibile {
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
		for (Notizia notice : crimes) {
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
		List<Notizia> crimes = news.getCrimes();
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
				.iterations(6)
				.epochs(2)
				.layerSize(300)
				.stopWords(stopWords)
				.learningRate(0.045)
				.labelsSource(source)
				.windowSize(10)
				.iterate(iter)
				.trainWordVectors(false)
				.vocabCache(cache)
				.tokenizerFactory(t)
				.sampling(0)
				.build();

		vec.fit();
		
		double all_similarity, threshold = 0.5;
		OutputFileClass out = new OutputFileClass();
		
		for (int i = 0; i < crimes.size(); i++) {
			Notizia ni, nj;
			ni = crimes.get(i);
			for (int j = i + 1; j < crimes.size(); j++) {
				nj = crimes.get(j);
				all_similarity = vec.similarity("DOC_" + i, "DOC_" + j);
				if (all_similarity >= threshold) {
					out.write("DOC_" + i + ", DOC_" + j);
					out.write(ni.all);
					out.write(nj.all);
					out.write("all_similarity: " + all_similarity + "\n");
				}
			}
		}
		out.close();
	}
}

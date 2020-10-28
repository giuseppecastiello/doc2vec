package it.unimo.crime_analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;


import java.sql.ResultSet;

import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.AbstractCache;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.documentiterator.LabelsSource;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Eseguibile {

	private static final Logger log = LoggerFactory.getLogger(Eseguibile.class);

	public static String dataLocalPath;

	static DBManager db;

	static ArrayList<Notizia> crimes = new ArrayList<Notizia>();

	private static void dbConnection() {
		try {
			db = new DBManager(DBManager.JDBCURL);
		} catch (ClassNotFoundException e) {
			System.out.println("Missign lib...");
			throw new RuntimeException();
		} catch (SQLException e) {
			System.out.println("Error trying to connect to db");
			e.printStackTrace();
		}
	}

	private static void dbExtraction() {
		dbConnection();
		ResultSet rs = null;
		try {
			rs = db.executeQuery();
			while(rs.next()) {
				crimes.add((new Notizia(rs.getString("title"), 
						rs.getString("description"), rs.getString("text"))));
			}
			rs.close();
			db.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static File createFormattedFile(File file) {
		dbExtraction();
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		@SuppressWarnings("resource")
		BufferedWriter bw = new BufferedWriter(fw);
		for (Notizia notice : crimes) {
			try {
				bw.write(notice.toString());
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
		return file;
	}
	
	private static void readFormattedFile(File file) {
		FileReader fr = null;
		try {
			fr = new FileReader(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		BufferedReader br = new BufferedReader(fr);
		//String title, description, text;
		String all;
		try {
			while((all = br.readLine()) != null/*(title = br.readLine()) != null && (description = br.readLine()) != null && (text = br.readLine()) != null*/) {
				crimes.add(new Notizia(all));
			}
			br.close();
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void createTokenizedFile(TokenizerFactory t) {
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
	
	private static BufferedWriter createOutputFile() {
		FileWriter fw = null;
		try {
			fw = new FileWriter("output.txt");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		@SuppressWarnings("resource")
		BufferedWriter bw = new BufferedWriter(fw);
		return bw;
	}
	
	private static void writeOnOutputFile(BufferedWriter bw, String str) {
		try {
			bw.write(str + "\n");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
	
	private static void closeOutputFile(BufferedWriter bw) {
		try {
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		File file = new File("news.txt");

		if (!file.exists()) {
			file = createFormattedFile(file);
			log.info("File successfully created.");
		}
		else {
			log.info("Found a file news.txt, the algorithm will train on this file.");
			readFormattedFile(file);
		}

		SentenceIterator iter = new BasicLineIterator(file);
		AbstractCache<VocabWord> cache = new AbstractCache<>();

		LabelsSource source = new LabelsSource("DOC_");
		TokenizerFactory t = new DefaultTokenizerFactory();
		t.setTokenPreProcessor(new CommonPreprocessor());
		//createTokenizedFile(t);
		
		ArrayList<String> stopWords = new ArrayList<String>();
		stopWords.add("-");
		stopWords.add("«");
		stopWords.add("»");
		stopWords.add("+");
		stopWords.add("“");
		stopWords.add("”");
		stopWords.add("'");
		stopWords.add(" ");
		
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
		/*
		double title_similarity, desc_similarity, text_similarity;
		int index_i, index_j;
		*/
		double all_similarity, threshold = 0.5;
		BufferedWriter bw = createOutputFile();
		
		for (int i = 0; i < crimes.size(); i++) {
			Notizia ni, nj;
			ni = crimes.get(i);
			for (int j = i + 1; j < crimes.size(); j++) {
				nj = crimes.get(j);
				/*
				index_i = 4*i;
				index_j = 4*j;
				if ("".equals(ni.title) || "".equals(nj.title))
					title_similarity = threshold;
				else
					title_similarity = vec.similarity("DOC_" + index_i, "DOC_" + index_j);
				index_i++; index_j++;
				if ("".equals(ni.description) || "".equals(nj.description))
					desc_similarity = threshold;
				else
					desc_similarity = vec.similarity("DOC_" + index_i, "DOC_" + index_j);
				index_i++; index_j++;
				if ("".equals(nj.text) || "".equals(nj.text))
					text_similarity = threshold;
				else
					text_similarity = vec.similarity("DOC_" + index_i, "DOC_" + index_j);
				index_i++; index_j++;
				all_similarity = vec.similarity("DOC_" + index_i, "DOC_" + index_j);
				*/
				all_similarity = vec.similarity("DOC_" + i, "DOC_" + j);
				if (/*(title_similarity + desc_similarity + text_similarity) /3 >= threshold ||*/
						all_similarity >= threshold) {
					writeOnOutputFile(bw, "DOC_" + i + ", DOC_" + j);
					/*
					writeOnOutputFile(bw, "title_similarity: " + title_similarity);
					writeOnOutputFile(bw, ni.title);
					writeOnOutputFile(bw, nj.title);
					writeOnOutputFile(bw, "desc_similarity: " + desc_similarity);
					writeOnOutputFile(bw, ni.description);
					writeOnOutputFile(bw, nj.description);
					writeOnOutputFile(bw, "text_similarity: " + text_similarity);
					writeOnOutputFile(bw, ni.text);
					writeOnOutputFile(bw, nj.text);
					*/
					writeOnOutputFile(bw, ni.all);
					writeOnOutputFile(bw, nj.all);
					writeOnOutputFile(bw, "all_similarity: " + all_similarity + "\n");
				}
			}
		}
		closeOutputFile(bw);
	}
}

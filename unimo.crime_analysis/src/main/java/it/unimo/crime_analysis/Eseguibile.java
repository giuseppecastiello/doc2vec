package it.unimo.crime_analysis;

import java.io.BufferedWriter;
import java.io.File;
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

	private static final Logger log = LoggerFactory.getLogger(ParagraphVectorsTextExample.class);

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
				crimes.add(new Notizia(rs.getString("title"), 
						rs.getString("description"), rs.getString("text")));
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
			notice.format();
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

	public static void main(String[] args) throws Exception {
		File file = new File("news.txt");

		if (!file.exists())
			file = createFormattedFile(file);	
		else
			log.info("Found a file news.txt, the algorithm will train on this file.");

		SentenceIterator iter = new BasicLineIterator(file);
		AbstractCache<VocabWord> cache = new AbstractCache<>();

		LabelsSource source = new LabelsSource("DOC_");

		TokenizerFactory t = new DefaultTokenizerFactory();
		t.setTokenPreProcessor(new CommonPreprocessor());

		ParagraphVectors vec = new ParagraphVectors.Builder()
				.minWordFrequency(1)
				.iterations(5)
				.epochs(1)
				.layerSize(100)
				.learningRate(0.025)
				.labelsSource(source)
				.windowSize(5)
				.iterate(iter)
				.trainWordVectors(false)
				.vocabCache(cache)
				.tokenizerFactory(t)
				.sampling(0)
				.build();

		vec.fit();

		double similarity1 = (vec.similarity("DOC_0", "DOC_52") +
				vec.similarity("DOC_1", "DOC_53") +
				vec.similarity("DOC_2", "DOC_54")) / 3;
		log.info("('Gli svaligiano nella notte la prima e la seconda casa'/'Ladri a casa del consigliere Antonio Spica') similarity: " + similarity1);

		double similarity2 = (vec.similarity("DOC_0", "DOC_84") +
				vec.similarity("DOC_1", "DOC_85") +
				vec.similarity("DOC_2", "DOC_86")) / 3;
		log.info("('Gli svaligiano nella notte la prima e la seconda casa'/'Emis Killa picchiato a Modena') similarity: " + similarity2);

		/*
		double similarity3 = vec.similarity("DOC_6347", "DOC_3720");
		log.info("6348/3721 ('This is my case .'/'This is my way .') similarity: " + similarity3);


		double similarityX = vec.similarity("DOC_3720", "DOC_9852");
		log.info("3721/9853 ('This is my way .'/'We now have one .') similarity: " + similarityX +
				"(should be significantly lower)");
		 */
	}

}

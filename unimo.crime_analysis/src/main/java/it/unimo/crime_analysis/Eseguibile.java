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
						rs.getString("description"), rs.getString("text"))).format());
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
		String title, description, text;
		try {
			while((title = br.readLine()) != null && (description = br.readLine()) != null && (text = br.readLine()) != null) {
				crimes.add(new Notizia(title, description, text));
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

		String str = "Caro lettore , da tre settimane i giornalisti di ModenaToday ed i colleghi delle altre redazioni lavorano senza sosta , giorno e notte , per fornire aggiornamenti precisi ed affidabili sulla emergenza CoronaVirus . Se apprezzi il nostro lavoro , da sempre per te gratuito , e se ci leggi tutti i giorni , ti chiediamo un piccolo contributo per supportarci in questo momento straordinario . Grazie !";
		SentenceIterator iter = new BasicLineIterator(file);
		AbstractCache<VocabWord> cache = new AbstractCache<>();

		LabelsSource source = new LabelsSource("DOC_");
		TokenizerFactory t = new DefaultTokenizerFactory();
		t.setTokenPreProcessor(new CommonPreprocessor());
		createTokenizedFile(t);
		ArrayList<String> stopWords = new ArrayList<String>();
		stopWords.add("-");
		stopWords.add("«");
		stopWords.add("»");
		stopWords.add("+");
		stopWords.add("“");
		stopWords.add("”");
		stopWords.add("'");
		stopWords.add(" ");
		stopWords.add("nbsp");
		
		ParagraphVectors vec = new ParagraphVectors.Builder()
				.minWordFrequency(1)
				.iterations(5)
				.epochs(2)
				.layerSize(300)
				.stopWords(stopWords)
				.workers(4)
				.learningRate(0.05)
				.labelsSource(source)
				.windowSize(10)
				.iterate(iter)
				.trainWordVectors(false)
				.vocabCache(cache)
				.tokenizerFactory(t)
				.sampling(0)
				.resetModel(true)
				.build();

		vec.fit();

		double title_similarity, desc_similarity, text_similarity;
		char contains1, contains2;
		int index_i, index_j;
		for (int i = 0; i < crimes.size(); i++) {
			if (crimes.get(i).text.contains(str))
				contains1 = 'y';
			else
				contains1 = 'n';
			for (int j = i + 1; j < crimes.size(); j++) {
				index_i = 3*i;
				index_j = 3*j;
				title_similarity = vec.similarity("DOC_" + index_i, "DOC_" + index_j);
				index_i++; index_j++;
				desc_similarity = vec.similarity("DOC_" + index_i, "DOC_" + index_j);
				index_i++; index_j++;
				text_similarity = vec.similarity("DOC_" + index_i, "DOC_" + index_j);
				if ((title_similarity + desc_similarity + text_similarity) /3 >= 0.5) {
					if (crimes.get(j).text.contains(str))
						contains2 = 'y';
					else
						contains2 = 'n';
					log.info("DOC_" + index_i + ", DOC_" + index_j);
					log.info("title_similarity: " + title_similarity);
					log.info(crimes.get(i).title);
					log.info(crimes.get(j).title);
					log.info("desc_similarity: " + desc_similarity);
					log.info(crimes.get(i).description);
					log.info(crimes.get(j).description);
					log.info("text_similarity: " + text_similarity);
					log.info(crimes.get(i).text);
					log.info(crimes.get(j).text);
					log.info(contains1 + " " + contains2 + "\n");
				}
			}
		}
	}
}

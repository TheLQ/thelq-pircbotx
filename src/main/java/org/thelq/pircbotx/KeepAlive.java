/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thelq.pircbotx;

import com.google.common.collect.Iterators;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

/**
 *
 * @author Leon
 */
@Slf4j
public class KeepAlive {
	

	public KeepAlive(Properties properties) {
		log.info("Starting cloudbees keepalive");
		//Build infinate generator iterator
		int counter = 1;
		List<ConnectionGenerator> generators = new LinkedList();
		while (true) {
			String value;
			if ((value = properties.getProperty("keepAliveUrls." + counter + ".get")) != null) {
				log.info("New KeepAlive GET url: " + value);
				generators.add(new GetConnectionGenerator(value));
			} else if ((value = properties.getProperty("keepAliveUrls." + counter + ".post.url")) != null) {
				log.info("New KeepAlive POST url: " + value);
				String postData = properties.getProperty("keepAliveUrls." + counter + ".post.data");
				generators.add(new PostConnectionGenerator(value, postData));
			} else
				break;
		}

		//Build thread pool
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new BasicThreadFactory.Builder()
				.daemon(true)
				.namingPattern("cloudbees-keepalive-%d")
				.build());
		KeepAliveRunner keepAliveRunner = new KeepAliveRunner(Iterators.cycle(generators));
		executor.scheduleAtFixedRate(keepAliveRunner, 0, 15, TimeUnit.MINUTES);
		
		//Execute
		keepAliveRunner.run();
	}

	@Slf4j
	@RequiredArgsConstructor
	public static class KeepAliveRunner implements Runnable {
		protected final Iterator<ConnectionGenerator> generatorIterator;
		
		@Override
		public void run() {
			ConnectionGenerator curGenerator = generatorIterator.next();
			try {
				log.info("Executing keep alive using: " + curGenerator);
				URL pingUrl = new URL(curGenerator.getPingUrlRaw());
				HttpURLConnection connection = (HttpURLConnection) pingUrl.openConnection();
				connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:10.0.2) Gecko/20100101 Firefox/10.0.2");
				connection.setReadTimeout(10000);
				curGenerator.setup(connection);

				connection.connect();
				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
					throw new RuntimeException("Unknown return code " + connection.getResponseCode());
				log.info("Finished running keep alive using: " + curGenerator);
			} catch (Throwable e) {
				log.error("Error encountered when running keepalive for: " + curGenerator, e);
			}
		}
	}

	public static interface ConnectionGenerator {
		public void setup(HttpURLConnection connection) throws IOException;

		public String getPingUrlRaw();
	}

	@RequiredArgsConstructor
	@ToString
	public static class GetConnectionGenerator implements ConnectionGenerator {
		@Getter
		protected final String pingUrlRaw;

		@Override
		public void setup(HttpURLConnection connection) throws IOException {
			connection.setRequestMethod("GET");
		}
	}

	@RequiredArgsConstructor
	@ToString
	public static class PostConnectionGenerator implements ConnectionGenerator {
		@Getter
		protected final String pingUrlRaw;
		protected final String pingUrlPostData;

		@Override
		public void setup(HttpURLConnection connection) throws IOException {
			connection.setRequestMethod("POST");

			//Write post data
			connection.setDoOutput(true);
			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
			writer.write(pingUrlPostData);
			writer.flush();
		}
	}
}

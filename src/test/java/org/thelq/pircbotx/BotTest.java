/**
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thelq.pircbotx;

import com.google.common.io.Resources;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Leon
 */
@Slf4j
public class BotTest {
	@Test
	public void propertiesTest() throws IOException, URISyntaxException {
		getResources("");
		getResources(".");
		getResources("/");
		Properties properties = new Properties();
		properties.load(new FileInputStream(new File(Resources.getResource("pircbotx.properties").toURI())));
		assertTrue(properties.size() > 3, "Not enough properties: " + properties.size());
	}

	protected static void getResources(String path) {
		try {
			log.info(path);
			for (Enumeration<URL> resources = BotTest.class.getClassLoader().getResources(path); resources.hasMoreElements();) {
				URL curResource = resources.nextElement();
				log.info(" - " + curResource);
			}
		} catch (Exception e) {
			log.warn("Cannot get resources for " + path, e);
		}
	}
}

/**
 * Copyright (C) 2011 Leon Blakey <lord.quackstar at gmail.com>
 *
 * This file is part of TheLQ-PircBotX.
 *
 * TheLQ-PircBotX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TheLQ-PircBotX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with TheLQ-PircBotX. If not, see <http://www.gnu.org/licenses/>.
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

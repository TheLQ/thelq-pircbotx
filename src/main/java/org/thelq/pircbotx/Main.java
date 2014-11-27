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

import com.moandjiezana.toml.Toml;
import org.thelq.pircbotx.servlet.PingServlet;
import org.thelq.pircbotx.servlet.BotVelocityServlet;
import java.io.File;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.pircbotx.Configuration;
import org.thelq.pircbotx.commands.CountdownCommand;
import org.thelq.pircbotx.commands.HelpCommand;
import org.thelq.pircbotx.commands.IdentifiedCommand;
import org.thelq.pircbotx.commands.LevelsListCommand;
import org.thelq.pircbotx.commands.MyLevelsCommand;
import org.thelq.pircbotx.commands.NewYearsCommand;
import org.thelq.pircbotx.commands.NickUpdateListener;
import org.thelq.pircbotx.commands.StatsCommand;
import org.thelq.pircbotx.commands.UptimeCommand;
import org.thelq.pircbotx.keepalive.JenkinsKeepAlive;

/**
 * Main class
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Slf4j
public class Main {
	public static final StatsMultiBotManager MANAGER = new StatsMultiBotManager();
	public static final String PRODUCTION_SYSTEM_PROPERTY = "qprod.port";
	public static final boolean PRODUCTION = System.getProperties().containsKey(PRODUCTION_SYSTEM_PROPERTY);
	public static Server server;

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		//Initial configuration
		Configuration.Builder templateConfig = new Configuration.Builder()
				.setLogin("LQ")
				.setAutoNickChange(true);
		if (PRODUCTION)
			templateConfig.setName("TheLQ-PircBotX");
		else
			templateConfig.setName("TheLQ-BotTest");
		templateConfig.getListenerManager().addListener(new HelpCommand());
		templateConfig.getListenerManager().addListener(new IdentifiedCommand());
		templateConfig.getListenerManager().addListener(new UptimeCommand());
		templateConfig.getListenerManager().addListener(new LevelsListCommand());
		templateConfig.getListenerManager().addListener(new MyLevelsCommand());
		templateConfig.getListenerManager().addListener(new CountdownCommand());
		templateConfig.getListenerManager().addListener(new NewYearsCommand());
		templateConfig.getListenerManager().addListener(new StatsCommand());
		templateConfig.getListenerManager().addListener(new NickUpdateListener());

		//Load properties
		String filename = "thelq-pircbotx.toml";
		File file = new File(filename);
		if(!file.exists())
			file = new File("src/main/resources/" + filename);
		if(!file.exists())
			throw new RuntimeException("Cannot find file " + filename);
		Toml properties = new Toml().parse(file);

		//Join servers
		for (Toml serverArgsRaw : properties.getTables("server")) {
			ServerConfig serverArgs = serverArgsRaw.to(ServerConfig.class);
			log.debug("hostname " + serverArgs.hostname);
			log.debug("channels " + serverArgs.channels);
			log.debug("nickserv " + serverArgs.nickserv);
			MANAGER.addBot(new Configuration.Builder(templateConfig)
					.addServer(serverArgs.hostname)
					.addAutoJoinChannels(serverArgs.channels)
					.setNickservPassword(serverArgs.nickserv)
					.buildConfiguration()
			);
		}

		startWebServer(properties);

		if (PRODUCTION) {
			JenkinsKeepAlive.create();
		}

		//Connect
		MANAGER.start();
	}

	protected static void startWebServer(Toml properties) throws Exception {
		server = new Server(properties.getTable("webserver").getLong("port").intValue());
		ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		servletHandler.addServlet(new ServletHolder(new BotVelocityServlet()), "/*");
		servletHandler.addServlet(new ServletHolder(new PingServlet()), "/cloudbees-alive/*");
		server.setHandler(servletHandler);

		//Find the root path
		String rootPath;
		File classesFolder = new File("src\\main\\resources");
		if (classesFolder.exists())
			rootPath = classesFolder.getAbsolutePath();
		else
			rootPath = Main.class.getClassLoader().getResource(".").toExternalForm();
		log.info("Set resource base path to " + rootPath);
		servletHandler.setResourceBase(rootPath);

		server.start();
	}
	
	@EqualsAndHashCode
	@ToString
	@RequiredArgsConstructor
	public static class ServerConfig {
		private final String hostname;
		private final String nickserv;
		private final List<String> channels;
	}
}

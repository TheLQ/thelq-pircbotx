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

import org.thelq.pircbotx.keepalive.BotKeepAlive;
import org.thelq.pircbotx.servlet.PingServlet;
import org.thelq.pircbotx.servlet.BotVelocityServlet;
import java.io.File;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.managers.ListenerManager;
import org.pircbotx.hooks.managers.ThreadedListenerManager;
import org.thelq.pircbotx.commands.CountdownCommand;
import org.thelq.pircbotx.commands.ForumHistoryCommand;
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
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Slf4j
public class Main {
	public static final StatsMultiBotManager MANAGER = new StatsMultiBotManager();
	public static final String PRODUCTION_SYSTEM_PROPERTY = "qprod.port";
	public static final boolean PRODUCTION = System.getProperties().containsKey(PRODUCTION_SYSTEM_PROPERTY);
	public static Server server;

	public static void main(String[] args) throws Exception {
		//Initial configuration
		Configuration.Builder<PircBotX> templateConfig = new Configuration.Builder<PircBotX>()
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

		//Load nickserv data
		Properties properties = new Properties();
		properties.load(Main.class.getClassLoader().getResourceAsStream("pircbotx.properties"));

		//Servers
		MANAGER.addBot(new Configuration.Builder(templateConfig)
				.setServerHostname("irc.freenode.org")
				.addAutoJoinChannel("#pircbotx")
				.setNickservPassword(properties.getProperty("nickserv.freenode"))
				.buildConfiguration());
		MANAGER.addBot(new Configuration.Builder(templateConfig)
				.setServerHostname("irc.swiftirc.net")
				.addAutoJoinChannel("#pircbotx")
				.setNickservPassword(properties.getProperty("nickserv.swiftirc"))
				.buildConfiguration());

		//Special lyokofreak config
		ListenerManager lyokofreakListeners = new ThreadedListenerManager();
		for (Listener curListener : templateConfig.getListenerManager().getListeners())
			lyokofreakListeners.addListener(curListener);
		lyokofreakListeners.addListener(new ForumHistoryCommand(properties));
		MANAGER.addBot(new Configuration.Builder(templateConfig)
				.setServerHostname("irc.mibbit.com")
				.addAutoJoinChannel("#lyokofreak")
				.setNickservPassword(properties.getProperty("nickserv.mibbit"))
				.setListenerManager(lyokofreakListeners)
				.buildConfiguration());

		startWebServer();

		if (PRODUCTION) {
			BotKeepAlive.create(properties);
			JenkinsKeepAlive.create(properties);
		}

		//Connect
		MANAGER.start();
	}

	protected static void startWebServer() throws Exception {
		server = new Server(Integer.parseInt(System.getProperty(PRODUCTION_SYSTEM_PROPERTY, "8080")));
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
}

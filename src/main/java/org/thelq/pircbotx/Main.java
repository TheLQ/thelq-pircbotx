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
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
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

/**
 * Main class
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Slf4j
public class Main {
	public static final StatsMultiBotManager MANAGER = new StatsMultiBotManager();
	public static final boolean PRODUCTION = System.getProperties().containsKey("app.port");

	public static void main(String[] args) throws Exception {
		//Initial configuration
		Configuration.Builder templateConfig = new Configuration.Builder()
				.setLogin("LQ")
				.setAutoNickChange(true);
		if (PRODUCTION)
			templateConfig.setName("TheLQ-PircBotX");
		else
			templateConfig.setName("TheLQ-BotTest");

		//Load nickserv data
		Properties properties = new Properties();
		properties.load(new FileInputStream(new File(Resources.getResource("pircbotx.properties").toURI())));

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

		//Various Listeners and commands
		templateConfig.getListenerManager().addListener(new HelpCommand());
		templateConfig.getListenerManager().addListener(new IdentifiedCommand());
		templateConfig.getListenerManager().addListener(new UptimeCommand());
		templateConfig.getListenerManager().addListener(new LevelsListCommand());
		templateConfig.getListenerManager().addListener(new MyLevelsCommand());
		templateConfig.getListenerManager().addListener(new CountdownCommand());
		templateConfig.getListenerManager().addListener(new NewYearsCommand());
		templateConfig.getListenerManager().addListener(new StatsCommand());
		templateConfig.getListenerManager().addListener(new NickUpdateListener());

		BotServe serve = new BotServe(Integer.parseInt(System.getProperty("app.port", "8080")));
		serve.runInBackground();

		if (PRODUCTION)
			KeepAlive.create(properties);

		//Connect
		MANAGER.start();
	}
}

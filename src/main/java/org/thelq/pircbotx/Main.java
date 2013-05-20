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

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import org.pircbotx.Configuration;
import org.pircbotx.MultiBotManager;
import org.pircbotx.PircBotX;
import org.thelq.pircbotx.commands.CountdownCommand;
import org.thelq.pircbotx.commands.HelpCommand;
import org.thelq.pircbotx.commands.IdentifiedCommand;
import org.thelq.pircbotx.commands.LevelsListCommand;
import org.thelq.pircbotx.commands.MyLevelsCommand;
import org.thelq.pircbotx.commands.NYEListener;
import org.thelq.pircbotx.commands.UptimeCommand;
import org.thelq.pircbotx.server.BotServe;

/**
 * Main class
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class Main {
	public static void main(String[] args) throws Exception {
		//Initial configuration
		MultiBotManager manager = new MultiBotManager();
		Configuration.Builder templateConfig = new Configuration.Builder()
				.setName("TheLQ-testing")
				.setLogin("LQ")
				.setAutoNickChange(true);

		//Load nickserv data
		Properties passwords = new Properties();
		passwords.load(new FileInputStream(new File(Resources.getResource("nickserv.properties").toURI())));

		//Servers
		manager.addBot(new Configuration.Builder(templateConfig)
				.setServerHostname("irc.freenode.org")
				.addAutoJoinChannel("#pircbotx")
				.setNickservPassword(passwords.getProperty("freenode"))
				.buildConfiguration());
		manager.addBot(new Configuration.Builder(templateConfig)
				.setServerHostname("irc.swiftirc.net")
				.addAutoJoinChannel("#pircbotx")
				.setNickservPassword(passwords.getProperty("swiftirc"))
				.buildConfiguration());

		//Various Listeners and commands
		templateConfig.getListenerManager().addListener(new HelpCommand());
		templateConfig.getListenerManager().addListener(new IdentifiedCommand());
		templateConfig.getListenerManager().addListener(new UptimeCommand());
		templateConfig.getListenerManager().addListener(new LevelsListCommand());
		templateConfig.getListenerManager().addListener(new MyLevelsCommand());
		templateConfig.getListenerManager().addListener(new CountdownCommand());
		templateConfig.getListenerManager().addListener(new NYEListener());
		
		BotServe serve = new BotServe(8080, manager);
		serve.runInBackground();

		//Connect
		manager.start();
	}
}

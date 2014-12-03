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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thelq.pircbotx;

import com.google.common.collect.ImmutableList;
import com.moandjiezana.toml.Toml;
import java.io.File;
import java.util.List;
import org.pircbotx.ReplayServer;
import org.pircbotx.hooks.managers.ThreadedListenerManager;
import org.thelq.pircbotx.commands.api.AdminsCommand;
import org.thelq.pircbotx.commands.CountdownCommand;
import org.thelq.pircbotx.commands.HelpCommand;
import org.thelq.pircbotx.commands.IdentifiedCommand;
import org.thelq.pircbotx.commands.LevelsListCommand;
import org.thelq.pircbotx.commands.MyLevelsCommand;
import org.thelq.pircbotx.commands.NewYearsCommand;
import org.thelq.pircbotx.commands.NickUpdateListener;
import org.thelq.pircbotx.commands.UptimeCommand;
import org.thelq.pircbotx.commands.WallOfTextCommand;

/**
 *
 * @author Leon
 */
public class ReplayServerImpl extends ReplayServer {
	public static void main(String[] args) throws Exception {
		//Load properties
		String filename = "thelq-pircbotx.toml";
		File file = new File(filename);
		if (!file.exists())
			file = new File("src/main/resources/" + filename);
		if (!file.exists())
			throw new RuntimeException("Cannot find file " + filename);
		Toml properties = new Toml().parse(file);
		
		List<String> adminNicks = ImmutableList.copyOf(properties.getTable("admin").getList("nicks", String.class));

		ReplayServerImpl.replayFile(new File("test.log"), ReplayServer.generateConfig()
				.setListenerManager(new ThreadedListenerManager())
				.addListener(new HelpCommand())
				.addListener(new IdentifiedCommand())
				.addListener(new UptimeCommand())
				.addListener(new LevelsListCommand())
				.addListener(new MyLevelsCommand())
				.addListener(new CountdownCommand())
				.addListener(new NewYearsCommand())
				//.addListener(new StatsCommand())
				.addListener(new NickUpdateListener())
				.addListener(new WallOfTextCommand())
				.addListener(Main.admins = new AdminsCommand(adminNicks))
		);

	}
}

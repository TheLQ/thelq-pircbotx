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

import org.pircbotx.MultiBotManager;
import org.thelq.pircbotx.commands.HelpCommand;
import org.thelq.pircbotx.commands.IdentifiedCommand;
import org.thelq.pircbotx.commands.LevelsListCommand;
import org.thelq.pircbotx.commands.MyLevelsCommand;
import org.thelq.pircbotx.commands.UptimeCommand;

/**
 * Main class
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class Main {
	public static void main(String[] args) {
		//Might have multiple bots in the future
		MultiBotManager manager = new MultiBotManager("TheLQ-Pircbotx");
		manager.setLogin("LQ");
		manager.setVerbose(true);
		manager.setAutoNickChange(true);
		
		//Servers
		manager.createBot("irc.freenode.org").addChannel("#pircbotx");
		
		//Various Listeners and commands
		manager.getListenerManager().addListener(new HelpCommand());
		manager.getListenerManager().addListener(new IdentifiedCommand());
		manager.getListenerManager().addListener(new UptimeCommand());
		manager.getListenerManager().addListener(new LevelsListCommand());
		manager.getListenerManager().addListener(new MyLevelsCommand());
		
		//Connect
		try {
			manager.connectAll();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}

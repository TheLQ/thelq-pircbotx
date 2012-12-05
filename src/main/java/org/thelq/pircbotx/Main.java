/**
 * Copyright (C) 2011 Leon Blakey <lord.quackstar at gmail.com>
 *
 * This file is part of Quackbot.
 *
 * Quackbot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quackbot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Quackbot. If not, see <http://www.gnu.org/licenses/>.
 */
package org.thelq.pircbotx;

import org.pircbotx.MultiBotManager;

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
		
		//Connect
		try {
			manager.connectAll();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}

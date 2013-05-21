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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thelq.pircbotx.commands;

import org.pircbotx.hooks.Event;
import org.thelq.pircbotx.Main;
import org.thelq.pircbotx.Stats;

/**
 *
 * @author Leon
 */
public final class ListenerUtils {

	private ListenerUtils() {
		//Do not create
	}
	
	public static Stats getStats(Event event) {
		return Main.MANAGER.getStats(event.getBot().getBotId());
	}
	
	public static void incrimentCommands(Event event) {
		getStats(event).getReceivedCommands().incrementAndGet();
	}
	
	public static void addHistory(Event event) {
		getStats(event).addHistoryEntry(event);
	}
}

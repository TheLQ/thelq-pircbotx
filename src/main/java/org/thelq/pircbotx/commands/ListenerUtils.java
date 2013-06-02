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

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import org.thelq.pircbotx.Main;
import org.thelq.pircbotx.Stats;

/**
 *
 * @author Leon
 */
public final class ListenerUtils {
	public static final String PREFIX = Main.PRODUCTION ? "?" : "!";
	protected static final DateTimeFormatter FORMATTER_DATE = DateTimeFormat.mediumDateTime();

	private ListenerUtils() {
		//Do not create
	}

	public static Stats getStats(Event event) {
		return Main.MANAGER.getStats(event.getBot().getBotId());
	}

	public static void incrimentCommands(Event event) {
		getStats(event).getReceivedCommands().incrementAndGet();
	}

	public static void addHistory(Event event, Stats.HistoryType type, Channel channel, User user, String message) {
		getStats(event).addHistoryEntry(new Stats.HistoryEntry(type,
				FORMATTER_DATE.print(event.getTimestamp()),
				ImmutableList.of(channel),
				ImmutableList.of(user),
				message));
	}

	public static void addHistory(Event event, Stats.HistoryType type, List<Channel> channels, List<User> users, String message) {
		getStats(event).addHistoryEntry(new Stats.HistoryEntry(type,
				FORMATTER_DATE.print(event.getTimestamp()),
				channels,
				users,
				message));
	}

	public static boolean isCommand(String message, String command) {
		return StringUtils.startsWithIgnoreCase(message, PREFIX + command);
	}
}

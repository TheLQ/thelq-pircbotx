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
package org.thelq.pircbotx;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.pircbotx.Channel;
import org.pircbotx.User;

/**
 *
 * @author Leon
 */
@Data
public class Stats {
	protected final PeriodFormatter periodFormatter = PeriodFormat.getDefault();
	protected final DateTime startTime = DateTime.now();
	protected final LinkedList<HistoryEntry> history = new LinkedList<HistoryEntry>();
	protected AtomicInteger receivedMessages = new AtomicInteger(0);
	protected AtomicInteger receivedCommands = new AtomicInteger(0);

	public String getUptime() {
		return periodFormatter.print(new Duration(startTime, DateTime.now()).toPeriod());
	}

	public void addHistoryEntry(HistoryEntry historyEntry) {
		history.add(historyEntry);
		if (history.size() > 100)
			history.removeFirst();
	}

	@Data
	public static class HistoryEntry {
		protected static final Joiner SPACE_JOINER = Joiner.on(", ");
		protected static final Function<Channel, String> FUNCTION_CHANNEL_NAME = new Function<Channel, String>() {
			@Override
			public String apply(Channel input) {
				return input.getName();
			}
		};
		protected static final Function<User, String> FUNCTION_USER_NICK = new Function<User, String>() {
			@Override
			public String apply(User input) {
				return input.getNick();
			}
		};
		protected final HistoryType type;
		protected final String timestamp;
		protected final List<Channel> channels;
		protected final List<User> users;
		protected final String message;
		
		public String getChannelNames() {
			return SPACE_JOINER.join(Iterables.transform(channels, FUNCTION_CHANNEL_NAME));
		}
		
		public String getUserNames() {
			return SPACE_JOINER.join(Iterables.transform(users, FUNCTION_USER_NICK));
		}
	}

	public static enum HistoryType {
		NORMAL,
		ITALIC,
		BOLD,
		BACKGROUND
	}
}

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
package org.thelq.pircbotx.commands;

import java.lang.management.ManagementFactory;
import lombok.Getter;
import org.apache.commons.lang3.time.DateUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.thelq.pircbotx.BasicCommand;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class UptimeCommand extends ListenerAdapter implements BasicCommand {
	@Getter
	protected String help = "Show the uptime of the bot";

	@Override
	public void onMessage(MessageEvent event) throws Exception {
		if (!event.getMessage().startsWith("?uptime"))
			return;

		//Get the uptime of the JVM
		long uptime = ManagementFactory.getRuntimeMXBean().getUptime();

		//Calculate pieces of the date
		long remainingTime = uptime;
		long days = remainingTime / DateUtils.MILLIS_PER_DAY;
		remainingTime -= days * DateUtils.MILLIS_PER_DAY;
		long hours = remainingTime / DateUtils.MILLIS_PER_HOUR;
		remainingTime -= hours * DateUtils.MILLIS_PER_HOUR;
		long mins = remainingTime / DateUtils.MILLIS_PER_MINUTE;
		remainingTime -= mins * DateUtils.MILLIS_PER_MINUTE;
		long secs = remainingTime / DateUtils.MILLIS_PER_SECOND;
		
		//Done, inform the user
		event.respond("This bot has been up for " + days + " days, " + hours + " hours, " 
				+ mins + " minuites, and " + secs + " seconds");
	}
}

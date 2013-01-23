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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import org.apache.commons.lang3.mutable.MutableInt;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.Seconds;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.thelq.pircbotx.BasicCommand;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class CountdownCommand extends ListenerAdapter implements BasicCommand {
	@Getter
	protected String help = "Counts down to the specified time. Use: ?countdown <time> <sec/min>";
	protected static PeriodFormatter driftFormatter;
	protected static PeriodFormatter periodFormatterSec;
	protected static PeriodFormatter periodFormatterMinSec;

	static {
		driftFormatter = new PeriodFormatterBuilder()
				.appendMinutes().appendSuffix("m")
				.appendSeconds().appendSuffix("s")
				.appendMillis().appendSuffix("ms")
				.toFormatter();
		periodFormatterSec = new PeriodFormatterBuilder()
				.appendSeconds().appendSuffix("s")
				.toFormatter();
		periodFormatterMinSec = new PeriodFormatterBuilder()
				.appendMinutes().appendSuffix("m")
				.appendSeconds().appendSuffix("s")
				.toFormatter();
	}

	@Override
	public void onMessage(MessageEvent event) throws Exception {
		if (!event.getMessage().startsWith("?countdown"))
			return;

		//Split up the line
		String[] messageParts = event.getMessage().split(" ", 2);
		if (messageParts.length != 2 || messageParts[1].trim().length() == 0) {
			event.respond("No time passsed");
			return;
		}

		//Parse
		Period parsePeriod;
		try {
			parsePeriod = periodFormatterSec.parsePeriod(messageParts[1]);
		} catch (IllegalArgumentException e) {
			try {
				parsePeriod = periodFormatterMinSec.parsePeriod(messageParts[1]);
			} catch (IllegalArgumentException ex) {
				event.respond("Cannot parse date");
				throw ex;
			}
		}
		int remainingSeconds = parsePeriod.toStandardSeconds().getSeconds();
		DateTime startDate = new DateTime();
		DateTime endDate = startDate.plus(parsePeriod);

		//Register times we want to notify the user
		List<DateTime> notifyTimes = new ArrayList();
		CountdownUtils.registerTime(notifyTimes, startDate, endDate, 0);
		CountdownUtils.registerTime(notifyTimes, startDate, endDate, 2);
		CountdownUtils.registerTime(notifyTimes, startDate, endDate, 5);
		CountdownUtils.registerTime(notifyTimes, startDate, endDate, 10);
		CountdownUtils.registerTime(notifyTimes, startDate, endDate, 30);
		for (int i = 1; i <= (remainingSeconds % 60); i++)
			CountdownUtils.registerTime(notifyTimes, startDate, endDate, i * 60);

		//Reverse the list so they can be loaded in the correct order (easier to write in reverse above)
		Collections.reverse(notifyTimes);

		CountdownUtils.respondNow(event, "Waiting: " + periodFormatterMinSec.print(parsePeriod) + " (" + remainingSeconds + " seconds)");

		DateTime lastDateTime = startDate;
		for (DateTime curDateTime : notifyTimes) {
			int waitPeriod = Seconds.secondsBetween(lastDateTime, curDateTime).getSeconds() * 1000;
			event.getBot().log("--- Waiting: " + waitPeriod);
			Thread.sleep(waitPeriod);
			Period remainingPeriod = new Period(curDateTime, endDate);
			if (remainingPeriod.toStandardSeconds().getSeconds() == 0)
				//Done
				break;
			else
				CountdownUtils.respondNow(event, "Remaining: " + periodFormatterMinSec.print(remainingPeriod));
			lastDateTime = curDateTime;
		}

		DateTime realEndDate = new DateTime();
		Period drift = new Period(endDate, realEndDate);
		CountdownUtils.respondNow(event, "Countdown finished (Drift: " + driftFormatter.print(drift) + ")");
	}
}
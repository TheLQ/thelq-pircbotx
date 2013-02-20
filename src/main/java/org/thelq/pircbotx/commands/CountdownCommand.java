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

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.pircbotx.User;
import org.pircbotx.hooks.events.MessageEvent;
import org.thelq.pircbotx.BasicCommand;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class CountdownCommand extends AbstractAlarmListener implements BasicCommand {
	@Getter
	protected String help = "Counts down to the specified time. Use: ?countdown <time> <sec/min>";
	protected static PeriodFormatter driftFormatter;
	protected static PeriodFormatter periodFormatterSec;
	protected static PeriodFormatter periodFormatterMinSec;
	protected Map<DateTime, MessageEvent> openCountdowns = new HashMap();

	static {
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
		
		for(MessageEvent curEvent : openCountdowns.values())
			if(curEvent.getUser().equals(event.getUser())) {
				event.respond("You already have a countdown open. Please wait for it to finish");
				return;
			}

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

		//Start the process
		System.out.println("Adding to queue");
		DateTime alarmTime = new DateTime().plus(parsePeriod);
		openCountdowns.put(alarmTime, event);
		alarmTimes.add(alarmTime);
	}

	@Override
	public void onStart(DateTime alarmDate, long secondsTillNotify) {
		sendMessageNowAlarm(alarmDate, "Countdown starting...");
	}

	@Override
	public void onNotifyBefore(DateTime alarmDate, long secondsToWait) {
		System.out.println("CD: Waiting " + secondsToWait + " seconds for notify for " + openCountdowns.get(alarmDate).getUser().getNick());
	}

	@Override
	public void onNotify(DateTime alarmDate, long secondsRemain) {
		sendMessageNowAlarm(alarmDate, remainFormatter.print(new Period(1000 * secondsRemain)) + " remaining");
	}

	@Override
	public void onEnd(DateTime alarmDate) {
		sendMessageNowAlarm(alarmDate, "Done! Drift: " + calcDrift(alarmDate));
	}
	
	protected void sendMessageNowAlarm(DateTime alarmDate, String message) {
		MessageEvent event = openCountdowns.get(alarmDate);
		sendMessageNow(event.getBot(), event.getChannel(), event.getUser(), message);
	}
}
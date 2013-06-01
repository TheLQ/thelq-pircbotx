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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	protected static Map<User, Alarm> openAlarms = new ConcurrentHashMap();

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
	public void onMessage(final MessageEvent event) throws Exception {
		if (!ListenerUtils.isCommand(event.getMessage(), "countdown"))
			return;

		ListenerUtils.incrimentCommands(event);
		Alarm existingAlarm = openAlarms.get(event.getUser());
		if (existingAlarm != null) {
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
				event.respond("Cannot parse period");
				throw ex;
			}
		}

		//Start the process
		DateTime alarmTime = new DateTime(DateTimeZone.UTC).plus(parsePeriod);
		Alarm alarm = new Alarm(alarmTime) {
			protected Logger log = LoggerFactory.getLogger(getClass());

			@Override
			public List<Integer> getNotifySeconds(long totalSeconds) {
				List<Integer> notifyTimes = new ArrayList();
				for (int i = (int) (totalSeconds % 60); i >= 1; i--)
					notifyTimes.add(i * 60);
				notifyTimes.add(30);
				notifyTimes.add(10);
				notifyTimes.add(5);
				notifyTimes.add(2);
				return notifyTimes;
			}

			@Override
			public void onStart(long secondsTillNotify) {
				sendMessageNowAlarm("Countdown starting...");
			}

			@Override
			public void onNotifyBefore(long secondsToWait) {
				log.debug("Waiting " + secondsToWait + " seconds to notify user " + event.getUser().getNick());
			}

			@Override
			public void onNotify(long secondsRemain) {
				sendMessageNowAlarm(FORMATTER_REMAIN.print(new Period(1000 * secondsRemain)) + "remaining");
			}

			@Override
			public void onEnd() {
				sendMessageNowAlarm("Done! Drift: " + calcDrift());
			}

			protected void sendMessageNowAlarm(String message) {
				sendMessageNow(event.getBot(), event.getChannel(), event.getUser().getNick() + ": " + message);
			}
		};
		openAlarms.put(event.getUser(), alarm);

		//Generate notify times
		List<Integer> notifyTimes = new ArrayList();
		
			alarm.countdown();
		openAlarms.remove(event.getUser());
	}
}
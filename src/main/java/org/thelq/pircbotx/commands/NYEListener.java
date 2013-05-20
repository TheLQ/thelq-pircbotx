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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.pircbotx.hooks.events.MessageEvent;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Slf4j
public class NYEListener extends AbstractAlarmListener {
	/**
	 * Times of all NYE events relative to UTC and their timezone(s). Sorted for 
	 * ease of access
	 */
	protected static final TreeMap<DateTime, List<DateTimeZone>> nyTimes = new TreeMap();
	protected static final int newYear;
	protected static DateTimeFormatter tzOffsetFormatter = new DateTimeFormatterBuilder()
			.appendTimeZoneOffset(null, true, 2, 4)
			.toFormatter();
	protected boolean started = false;

	static {
		//Figure out what's the "new year" by rounding
		DateTime curTime = DateTime.now(DateTimeZone.UTC);
		newYear = curTime.getYear() + 1;

		//Collect timezone data
		for (String curId : DateTimeZone.getAvailableIDs()) {
			DateTimeZone tz = DateTimeZone.forID(curId);

			//Convert new years time relative to UTC
			DateTime nyTime = new DateTime(newYear, 1, 1, 0, 0, 0, 0, tz);
			DateTime nyTimeUTC = nyTime.withZone(DateTimeZone.UTC);

			//Add to map
			if (!nyTimes.containsKey(nyTimeUTC))
				nyTimes.put(nyTimeUTC, new ArrayList());
			nyTimes.get(nyTimeUTC).add(tz);
		}
	}

	public NYEListener() {
		//Add all precalculated NY times
		alarmTimes.addAll(nyTimes.keySet());
	}

	@Override
	public void onStart(DateTime alarmDate, long secondsTillNotify) {
		log(alarmDate, "Initialized NYE countdown for " + getShortNames(nyTimes.get(alarmDate)));
	}

	@Override
	public void onNotifyBefore(DateTime alarmDate, long secondsToWait) {
		log(alarmDate, "Waiting " + secondsToWait + " seconds for next notify");
	}

	@Override
	public void onNotify(DateTime alarmDate, long secondsRemain) {
		if (!started) {
			sendMessage("NEW YEARS EVE COUNTDOWN STARTING!!! " + getExtendedNames(nyTimes.get(alarmDate)));
			started = true;
		}

		//Theme the countdown
		if (secondsRemain > 9)
			sendMessage(getRemainFormatter().print(new Period(1000 * secondsRemain))
					+ " till NYE for " + getExtendedNames(nyTimes.get(alarmDate)));
		else if (secondsRemain < 9 && secondsRemain > 3)
			sendMessage(secondsRemain + " seconds");
		else if (secondsRemain == 3)
			sendMessage("3!");
		else if (secondsRemain == 3)
			sendMessage("2!!");
		else if (secondsRemain == 3)
			sendMessage("1!!!");
	}

	@Override
	public void onEnd(DateTime end) {
		sendMessage("Happy New Year!!!! Welcome to " + newYear + " " + getExtendedNames(nyTimes.get(end))
				+ "Drift: " + calcDrift(end));
	}

	@Override
	public void onMessage(MessageEvent event) throws Exception {
		String[] commandParts = event.getMessage().split(" ", 3);
		if (commandParts.length != 3 || !StringUtils.startsWithIgnoreCase(commandParts[0], "?newyears"))
			return;
		log.debug("Got new years command");
		DateTime now = DateTime.now();

		if (commandParts[1].equals("next")) {
			ListenerUtils.incrimentCommands(event);
			for (Map.Entry<DateTime, List<DateTimeZone>> curEntry : nyTimes.entrySet())
				if (curEntry.getKey().isAfter(now)) {
					event.respond("Next New Years is in " + remainFormatter.print(new Period(now, curEntry.getKey()))
							+ " for " + getUTCOffset(curEntry.getValue().get(0))
							+ " - " + getExtendedNames(curEntry.getValue()));
					return;
				}
			//No entrires...
			event.respond("No more New Years :-( | " + nyTimes.keySet().iterator().next());
		}
	}

	protected void log(DateTime alarmTime, String message) {
		System.out.println("NYE(" + getUTCOffset(nyTimes.firstEntry().getValue().get(0)) + "): " + message);
	}

	public static String getUTCOffset(DateTimeZone tz) {
		long millis = System.currentTimeMillis();
		while (tz.getOffset(millis) != tz.getStandardOffset(millis)) {
			long next = tz.nextTransition(millis);
			if (next == millis)
				break;
			millis = next;
		}
		return "UTC" + tzOffsetFormatter.withZone(tz).print(millis);
	}

	protected static String getExtendedNames(Collection<DateTimeZone> tzList) {
		if (tzList.isEmpty())
			return null;
		//Prebuild long form of timezones
		long nowTime = System.currentTimeMillis();
		Set<String> tzExtendedSet = new HashSet();
		for (DateTimeZone curTz : tzList) {
			if (StringUtils.startsWithIgnoreCase(curTz.getID(), "Etc/"))
				continue;
			tzExtendedSet.add(curTz.getName(nowTime) + "(" + curTz.getShortName(nowTime) + ")");
		}
		return StringUtils.defaultIfBlank(StringUtils.join(tzExtendedSet, ", "), null);
	}

	protected static String getShortNames(Collection<DateTimeZone> tzList) {
		//Prebuild long form of timezones
		long nowTime = System.currentTimeMillis();
		Set<String> tzShortSet = new HashSet();
		for (DateTimeZone curTz : tzList) {
			if (StringUtils.startsWithIgnoreCase(curTz.getID(), "Etc/"))
				continue;
			tzShortSet.add(curTz.getShortName(nowTime) + ", ");
		}
		return StringUtils.defaultIfBlank(StringUtils.join(tzShortSet, ", "), null);
	}
}

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

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.DisconnectEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PartEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thelq.pircbotx.BasicCommand;
import org.thelq.pircbotx.Main;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Slf4j
public class NewYearsCommand extends ListenerAdapter implements BasicCommand {
	/**
	 * Times of all NYE events relative to UTC and their timezone(s). Sorted for 
	 * ease of access
	 */
	private static final TreeMultimap<DateTime, DateTimeZone> NEW_YEAR_ZONES = TreeMultimap.create(Ordering.natural(), Ordering.usingToString());
	protected static final int NEW_YEAR;
	protected static DateTimeFormatter FORMATTER_TZOFFSET = new DateTimeFormatterBuilder()
			.appendTimeZoneOffset(null, true, 2, 4)
			.toFormatter();
	@Getter
	protected String help = "NYE countdown";
	protected DateTime waitingNewYear = null;
	protected final List<Channel> blacklistedChannels = Lists.newArrayList();

	static {
		//Guess what the current new year is
		DateTime curTime = DateTime.now(DateTimeZone.UTC);
		//if (curTime.getMonthOfYear() > 6)
		NEW_YEAR = curTime.getYear() + 1;
		//else
		//	newYear = curTime.getYear();
	}

	@Override
	public void onMessage(MessageEvent event) throws Exception {
		String[] commandParts = event.getMessage().split(" ", 2);
		if (commandParts.length != 2 || !ListenerUtils.isCommand(commandParts[0], "newyears"))
			return;

		if (commandParts[1].equals("start"))
			start();
		else if (commandParts[1].equals("year"))
			event.respond("Next year is " + NEW_YEAR);
		else if (commandParts[1].equals("next")) {
			ListenerUtils.incrimentCommands(event);
			String prefix = null;
			DateTime nextNewYear = null;
			if (waitingNewYear != null) {
				prefix = "Waiting for next New Years ";
				nextNewYear = waitingNewYear;
			} else {
				prefix = "Next New Years is ";
				nextNewYear = getNextNewYears();
				if (nextNewYear == null) {
					event.respond("No more New Years :-(");
					return;
				}
			}
			NavigableSet<DateTimeZone> timezones = getNyTimes().get(nextNewYear);
			event.respond(prefix + "in " + Alarm.FORMATTER_REMAIN.print(new Period(getNow(), nextNewYear))
					+ "for " + getUTCOffset(timezones.first())
					+ " - " + getExtendedNames(timezones));
		}
	}

	public void start() throws InterruptedException {
		DateTime nextNewYears;
		while ((nextNewYears = getNextNewYears()) != null) {
			waitingNewYear = nextNewYears;
			Alarm newYearAlarm = new Alarm(nextNewYears) {
				protected Logger log = LoggerFactory.getLogger(getClass());
				protected boolean started = false;

				@Override
				public List<Integer> getNotifySeconds(long totalSeconds) {
					List<Integer> notifySeconds = Lists.newArrayList();
					notifySeconds.add(5 * 60);
					notifySeconds.add(2 * 60);
					notifySeconds.add(60);
					notifySeconds.add(30);
					notifySeconds.add(10);
					notifySeconds.add(5);
					notifySeconds.add(3);
					notifySeconds.add(2);
					notifySeconds.add(1);
					return notifySeconds;
				}

				@Override
				public void onStart(long secondsTillNotify) {
					log.debug("Initialized NYE countdown for " + getShortNames(NEW_YEAR_ZONES.get(waitingNewYear)));
				}

				@Override
				public void onNotifyBefore(long secondsToWait) {
					log.debug("Waiting " + secondsToWait + " seconds for next notify");
				}

				@Override
				public void onNotify(long secondsRemain) {
					if (!started) {
						sendMessage("NEW YEARS EVE COUNTDOWN STARTING!!! " + getExtendedNames(NEW_YEAR_ZONES.get(waitingNewYear)));
						started = true;
					}

					//Theme the countdown
					if (secondsRemain > 9)
						sendMessage(FORMATTER_REMAIN.print(new Period(1000 * secondsRemain))
								+ "till NYE for " + getShortNames(NEW_YEAR_ZONES.get(alarmDate)));
					else if (secondsRemain <= 9 && secondsRemain > 3)
						sendMessage(secondsRemain + " seconds");
					else if (secondsRemain == 3)
						sendMessage("3!");
					else if (secondsRemain == 2)
						sendMessage("2!!");
					else if (secondsRemain == 1)
						sendMessage("1!!!");
				}

				@Override
				public void onEnd() {
					sendMessage("Happy New Year!!!! Welcome to " + NEW_YEAR + " " + getShortNames(NEW_YEAR_ZONES.get(waitingNewYear))
							+ " | Drift: " + calcDrift());
				}

				protected void sendMessage(String message) {
					//Send to all connected, non-blacklisted channels
					for (PircBotX curBot : Main.MANAGER.getBots())
						for (Channel curChannel : curBot.getUserChannelDao().getAllChannels()) {
							if (blacklistedChannels.contains(curChannel))
								continue;
							sendMessageNow(curBot, curChannel, message);
						}
				}
			};
			newYearAlarm.countdown();
		}
	}

	protected TreeMultimap<DateTime, DateTimeZone> getNyTimes() {
		synchronized (NEW_YEAR_ZONES) {
			if(NEW_YEAR_ZONES.isEmpty()) {
				NEW_YEAR_ZONES.put(getNow().plusMinutes(6), DateTimeZone.UTC);
			}
			else if (NEW_YEAR_ZONES.isEmpty())
				//Generate
				for (String curId : DateTimeZone.getAvailableIDs()) {
					DateTimeZone tz = DateTimeZone.forID(curId);

					//Convert new years time relative to UTC
					DateTime nyTime = new DateTime(NEW_YEAR, 1, 1, 0, 0, 0, 0, tz);
					DateTime nyTimeUTC = nyTime.withZone(DateTimeZone.UTC);

					//Add to map
					NEW_YEAR_ZONES.get(nyTimeUTC).add(tz);
				}
			return NEW_YEAR_ZONES;
		}
	}

	protected DateTime getNextNewYears() {
		for (DateTime curTime : getNyTimes().keySet())
			if (curTime.isAfter(getNow()))
				return curTime;
		return null;
	}

	public static String getUTCOffset(DateTimeZone tz) {
		long millis = System.currentTimeMillis();
		while (tz.getOffset(millis) != tz.getStandardOffset(millis)) {
			long next = tz.nextTransition(millis);
			if (next == millis)
				break;
			millis = next;
		}
		return "UTC" + FORMATTER_TZOFFSET.withZone(tz).print(millis);
	}

	protected static String getExtendedNames(Collection<DateTimeZone> tzList) {
		if (tzList.isEmpty())
			return null;
		//Prebuild long form of timezones
		long nowTime = System.currentTimeMillis();
		Set<String> tzExtendedSet = Sets.newHashSet();
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
		Set<String> tzShortSet = Sets.newHashSet();
		for (DateTimeZone curTz : tzList) {
			if (StringUtils.startsWithIgnoreCase(curTz.getID(), "Etc/"))
				continue;
			tzShortSet.add(curTz.getShortName(nowTime));
		}
		return StringUtils.defaultIfBlank(StringUtils.join(tzShortSet, ", "), null);
	}

	protected static DateTime getNow() {
		return DateTime.now(DateTimeZone.UTC);
	}

	@Override
	public void onPart(PartEvent event) throws Exception {
		if (event.getUser().getNick().equals(event.getBot().getNick()))
			//Its us, remove from channels
			synchronized (blacklistedChannels) {
				blacklistedChannels.remove(event.getChannel());
			}
	}

	@Override
	public void onDisconnect(DisconnectEvent event) throws Exception {
		//We lost a bot, remove its channels
		synchronized (blacklistedChannels) {
			for (Iterator<Channel> itr = blacklistedChannels.iterator(); itr.hasNext();)
				if (itr.next().getBot() == event.getBot())
					itr.remove();
		}
	}
}


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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import lombok.Synchronized;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.tz.NameProvider;
import org.pircbotx.Channel;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.DisconnectEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PartEvent;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class NYEListener extends ListenerAdapter {
	/**
	 * Times of all NYE events relative to UTC and their timezone(s). Sorted for 
	 * ease of access
	 */
	protected static final TreeMap<DateTime, List<DateTimeZone>> nyTimes = new TreeMap();
	protected List<Channel> notifyChannels = new ArrayList();
	protected static final int newYear;

	static {
		//Figure out what's the "new year" by rounding
		DateTime curTime = DateTime.now(DateTimeZone.UTC);
		newYear = (curTime.getMonthOfYear() < 6) ? curTime.getYear() : curTime.getYear() + 1;

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
		new Thread() {
			@Override
			public void run() {
				try {
					runNYELoop();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	public void runNYELoop() throws InterruptedException {
		for (final Map.Entry<DateTime, List<DateTimeZone>> curNyEntry : nyTimes.entrySet()) {
			//Make sure the event hasn't passed
			final DateTime nyLocal = curNyEntry.getKey().withZone(DateTimeZone.getDefault());
			if (nyLocal.isAfter(DateTime.now()))
				continue;

			//Execute countdown for next NYE
			runNYEHandler(nyLocal, curNyEntry.getValue());
		}
		System.out.println("NYE: No more NYE events to execute");
	}

	@Synchronized("notifyChannels")
	protected void sendMessage(String message) {
		for (Channel curChannel : notifyChannels)
			curChannel.sendMessage(message);
	}

	public void runNYEHandler(DateTime nyTimeLocal, final List<DateTimeZone> timezones) throws InterruptedException {
		//Build timezone name lists
		final List<String> tzLongNames = new ArrayList();
		final List<String> tzShortNames = new ArrayList();
		NameProvider nameProvider = DateTimeZone.getNameProvider();
		long curTimestamp = System.currentTimeMillis();
		for (DateTimeZone curTz : timezones)
			if (!StringUtils.startsWithIgnoreCase(curTz.getID(), "Etc/")) {
				tzLongNames.add(nameProvider.getName(Locale.US, curTz.getID(), curTz.getNameKey(curTimestamp)));
				tzShortNames.add(nameProvider.getShortName(Locale.US, curTz.getID(), curTz.getNameKey(curTimestamp)));
			}

		//Prebuild long form of timezones
		StringBuilder tzListBuilder = new StringBuilder();
		for (int i = 0; i < tzShortNames.size(); i++)
			tzListBuilder.append(tzShortNames.get(i)).append("(")
					.append(tzLongNames.get(i)).append(")")
					.append(", ");
		tzListBuilder.substring(0, -2);
		final String tzLongList = tzListBuilder.toString();

		CountdownUtils.countdown(nyTimeLocal, new CountdownUtils.CountdownHandler() {
			boolean started = false;

			@Override
			public void onStart(int secondsTillNotify) {
				log("Initialized NYE countdown for " + StringUtils.join(timezones, ", "));

			}

			@Override
			public void onNotifyBefore(int secondsToWait) {
				log("Waiting " + secondsToWait + " for next notify");
			}

			@Override
			public void onNotify(int secondsRemain) {
				if (!started) {
					sendMessage("NEW YEARS EVE COUNTDOWN STARTING!!! " + tzLongList);
					started = true;
				}

				//Theme the countdown
				if (secondsRemain > 9)
					sendMessage(CountdownUtils.getRemainFormatter().print(new Period(1000 * secondsRemain))
							+ " till NYE for " + StringUtils.join(tzShortNames, ", "));
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
			public void onEnd() {
				sendMessage("Happy New Year!!!! Welcome to " + newYear + " " + tzLongList);
			}

			protected void log(String message) {
				System.out.println("NYE(" + CountdownUtils.getUTCOffset(timezones.get(0)) + "): " + message);
			}
		});
	}

	@Override
	public void onMessage(MessageEvent event) throws Exception {
	}

	@Override
	public void onJoin(JoinEvent event) throws Exception {
		if (event.getUser().getNick().equals(event.getBot().getNick()))
			//Its us, add to channels
			synchronized (notifyChannels) {
				notifyChannels.add(event.getChannel());
			}
	}

	@Override
	public void onPart(PartEvent event) throws Exception {
		if (event.getUser().getNick().equals(event.getBot().getNick()))
			//Its us, remove from channels
			synchronized (notifyChannels) {
				notifyChannels.remove(event.getChannel());
			}
	}

	@Override
	@Synchronized("notifyChannels")
	public void onDisconnect(DisconnectEvent event) throws Exception {
		//We lost a bot, remove its channels
		for (Iterator<Channel> itr = notifyChannels.iterator(); itr.hasNext();)
			if (itr.next().getBot() == event.getBot())
				itr.remove();
	}
}

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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.Synchronized;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.Seconds;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.DisconnectEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.PartEvent;

/**
 *
 * @author Leon
 */
public abstract class AbstractAlarmListener extends ListenerAdapter {
	protected List<Channel> notifyChannels = new ArrayList();
	protected BlockingQueue<DateTime> alarmTimes = new LinkedBlockingQueue<DateTime>();
	protected static AtomicInteger instances = new AtomicInteger(0);
	@Getter
	protected static PeriodFormatter remainFormatter = new PeriodFormatterBuilder()
			.appendMonths().appendSuffix(" month ", " months ")
			.appendWeeks().appendSuffix(" week ", " weeks ")
			.appendDays().appendSuffix(" day ", " days ")
			.appendHours().appendSuffix(" hour ", " hours ")
			.appendMinutes().appendSuffix(" minute ", " minutes ")
			.appendSeconds().appendSuffix(" second ", " seconds ")
			.toFormatter();
	@Getter
	protected static PeriodFormatter driftFormatter = new PeriodFormatterBuilder()
			.appendMinutes().appendSuffix("m")
			.appendSeconds().appendSuffix("s")
			.appendMillis().appendSuffix("ms")
			.toFormatter();

	public AbstractAlarmListener() {
		new Thread() {
			{
				setName("alarm-" + instances.incrementAndGet() + "-" + getClass().getSimpleName());
				setDaemon(true);
			}

			@Override
			public void run() {
				try {
					runMainLoop();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	public void runMainLoop() throws InterruptedException {
		try {
			while (true) {
				//Execute countdown for next NYE
				DateTime nextAlarm = alarmTimes.take();
				if (nextAlarm.isBefore(DateTime.now()))
					continue;
				countdown(nextAlarm);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Synchronized("notifyChannels")
	protected void sendMessage(String message) {
		for (Channel curChannel : notifyChannels)
			curChannel.sendMessage(message);
	}

	public void countdown(DateTime alarmDate) throws InterruptedException {
		DateTime startDate = new DateTime();
		Period period = new Period(startDate, alarmDate);
		int periodSeconds = period.toStandardSeconds().getSeconds();
		//Register times we want to notify the user
		List<DateTime> notifyTimes = new ArrayList();
		registerNotifyTime(notifyTimes, startDate, alarmDate, 0);
		registerNotifyTime(notifyTimes, startDate, alarmDate, 2);
		registerNotifyTime(notifyTimes, startDate, alarmDate, 5);
		registerNotifyTime(notifyTimes, startDate, alarmDate, 10);
		registerNotifyTime(notifyTimes, startDate, alarmDate, 30);
		for (int i = 1; i <= (periodSeconds % 60); i++)
			registerNotifyTime(notifyTimes, startDate, alarmDate, i * 60);

		//Reverse the list so they can be loaded in the correct order (easier to write in reverse above)
		Collections.reverse(notifyTimes);

		onStart(alarmDate, periodSeconds);

		DateTime lastDateTime = startDate;
		for (DateTime curDateTime : notifyTimes) {
			long waitPeriodMilli = curDateTime.getMillis() - lastDateTime.getMillis();
			onNotifyBefore(alarmDate, (int) waitPeriodMilli / 1000);
			Thread.sleep(waitPeriodMilli);
			Period remainingPeriod = new Period(curDateTime, alarmDate);
			if (remainingPeriod.toStandardSeconds().getSeconds() == 0)
				//Done
				break;
			else
				onNotify(alarmDate, remainingPeriod.toStandardSeconds().getSeconds());
			lastDateTime = curDateTime;
		}

		onEnd(alarmDate);
	}

	protected void registerNotifyTime(List<DateTime> notifyTimes, DateTime startDate, DateTime endDate, int seconds) {
		//If the requested seconds is still in the period, add to list
		DateTime notifyTime = endDate.minusSeconds(seconds);
		if (notifyTime.isAfter(startDate) || notifyTime.isEqual(startDate))
			notifyTimes.add(notifyTime);
	}

	protected static void sendMessageNow(PircBotX bot, Channel chan, User user, String message) {
		bot.sendRawLineNow("PRIVMSG " + chan.getName() + " :" + user.getNick() + ": " + message);
	}

	protected static String calcDrift(DateTime alarmTime) {
		return driftFormatter.print(new Period(alarmTime, DateTime.now()));
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

	public void onStart(DateTime alarmDate, int secondsTillNotify) {
	}

	public void onNotifyBefore(DateTime alarmDate, int secondsToWait) {
	}

	public void onNotify(DateTime alarmDate, int secondsRemain) {
	}

	public void onEnd(DateTime alarmDate) {
	}
}

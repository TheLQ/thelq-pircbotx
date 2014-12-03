/**
 * Copyright (C) 2011 Leon Blakey <lord.quackstar at gmail.com>
 *
 * This file is part of TheLQ-PircBotX.
 *
 * TheLQ-PircBotX is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * TheLQ-PircBotX is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * TheLQ-PircBotX. If not, see <http://www.gnu.org/licenses/>.
 */
package org.thelq.pircbotx.commands;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;

/**
 *
 * @author Leon
 */
@RequiredArgsConstructor
public abstract class Alarm {
	protected static PeriodFormatter FORMATTER_REMAIN = new PeriodFormatterBuilder()
			.appendMonths().appendSuffix(" month ", " months ")
			.appendWeeks().appendSuffix(" week ", " weeks ")
			.appendDays().appendSuffix(" day ", " days ")
			.appendHours().appendSuffix(" hour ", " hours ")
			.appendMinutes().appendSuffix(" minute ", " minutes ")
			.appendSeconds().appendSuffix(" second ", " seconds ")
			.toFormatter();
	protected static PeriodFormatter FORMATTER_DRIFT = new PeriodFormatterBuilder()
			.appendMinutes().appendSuffix("m")
			.appendSeconds().appendSuffix("s")
			.appendMillis().appendSuffix("ms")
			.toFormatter();
	protected final DateTime alarmDate;

	public void countdown() throws InterruptedException {
		DateTime startDate = DateTime.now(DateTimeZone.UTC);
		Duration duration = new Duration(startDate, alarmDate);
		long durationSeconds = duration.getStandardSeconds();

		//Register times we want to notify the user
		List<Integer> notifySeconds = getNotifySeconds(durationSeconds);
		notifySeconds.add(0);
		List<DateTime> notifyTimes = Lists.newArrayList();
		for (Integer curSecond : notifySeconds) {
			//If the requested seconds is still in the period, add to list
			DateTime notifyTime = alarmDate.minusSeconds(curSecond);
			if (notifyTime.isAfter(startDate) || notifyTime.isEqual(startDate))
				notifyTimes.add(notifyTime);
		}

		onStart(durationSeconds);

		DateTime lastDateTime = startDate;
		for (DateTime curDateTime : notifyTimes) {
			long waitPeriodMilli = curDateTime.getMillis() - lastDateTime.getMillis();
			onNotifyBefore((int) waitPeriodMilli / 1000);
			Thread.sleep(waitPeriodMilli);
			long secondsRemaining = new Duration(curDateTime, alarmDate).getStandardSeconds();
			if (secondsRemaining <= 0)
				//Done
				break;
			else
				onNotify(secondsRemaining);
			lastDateTime = curDateTime;
		}

		onEnd();
	}

	protected static void sendMessageNow(PircBotX bot, Channel chan, String message) {
		bot.sendRaw().rawLineNow("PRIVMSG " + chan.getName() + " :" + message);
	}

	protected String calcDrift() {
		return FORMATTER_DRIFT.print(new Period(alarmDate, DateTime.now(DateTimeZone.UTC)));
	}
	
	public abstract List<Integer> getNotifySeconds(long totalSeconds);

	public void onStart(long secondsTillNotify) {
	}

	public void onNotifyBefore(long secondsToWait) {
	}

	public void onNotify(long secondsRemain) {
	}

	public void onEnd() {
	}
}

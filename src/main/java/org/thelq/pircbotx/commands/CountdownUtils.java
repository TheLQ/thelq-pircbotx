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
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class CountdownUtils {
	protected static PeriodFormatter driftFormatter;
	protected static PeriodFormatter periodFormatterSec;
	protected static PeriodFormatter periodFormatterMinSec;
	protected static DateTimeFormatter tzOffsetFormatter;
	@Getter
	protected static PeriodFormatter remainFormatter = new PeriodFormatterBuilder()
			.appendMonths().appendSuffix("months ")
			.appendWeeks().appendSuffix("weeks ")
			.appendDays().appendSuffix("days ")
			.appendHours().appendSuffix("hours")
			.appendMinutes().appendSuffix("minutes ")
			.appendSeconds().appendSuffix("seconds ")
			.toFormatter();

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
		tzOffsetFormatter = new DateTimeFormatterBuilder()
				.appendTimeZoneOffset(null, true, 2, 4)
				.toFormatter();
	}

	public static void countdown(DateTime endDate, CountdownHandler handler) throws InterruptedException {
		DateTime startDate = new DateTime();
		Period period = new Period(startDate, endDate);
		int periodSeconds = period.toStandardSeconds().getSeconds();
		//Register times we want to notify the user
		List<DateTime> notifyTimes = new ArrayList();
		CountdownUtils.registerTime(notifyTimes, startDate, endDate, 0);
		CountdownUtils.registerTime(notifyTimes, startDate, endDate, 2);
		CountdownUtils.registerTime(notifyTimes, startDate, endDate, 5);
		CountdownUtils.registerTime(notifyTimes, startDate, endDate, 10);
		CountdownUtils.registerTime(notifyTimes, startDate, endDate, 30);
		for (int i = 1; i <= (periodSeconds % 60); i++)
			CountdownUtils.registerTime(notifyTimes, startDate, endDate, i * 60);

		//Reverse the list so they can be loaded in the correct order (easier to write in reverse above)
		Collections.reverse(notifyTimes);

		handler.onStart(periodSeconds);

		DateTime lastDateTime = startDate;
		for (DateTime curDateTime : notifyTimes) {
			int waitPeriod = Seconds.secondsBetween(lastDateTime, curDateTime).getSeconds() * 1000;
			handler.onNotifyBefore(waitPeriod);
			Thread.sleep(waitPeriod);
			Period remainingPeriod = new Period(curDateTime, endDate);
			if (remainingPeriod.toStandardSeconds().getSeconds() == 0)
				//Done
				break;
			else
				handler.onNotify(remainingPeriod.toStandardSeconds().getSeconds());
			lastDateTime = curDateTime;
		}

		handler.onEnd();
	}

	public static void registerTime(List<DateTime> notifyTimes, DateTime startDate, DateTime endDate, int seconds) {
		//If the requested seconds is still in the period, add to list
		DateTime notifyTime = endDate.minusSeconds(seconds);
		if (notifyTime.isAfter(startDate) || notifyTime.isEqual(startDate))
			notifyTimes.add(notifyTime);
	}

	public static String getUTCOffset(DateTimeZone tz) {
		long millis = System.currentTimeMillis();
		while (tz.getOffset(millis) != tz.getStandardOffset(millis)) {
			long next = tz.nextTransition(millis);
			if (next == millis)
				break;
			millis = next;
		}
		return tzOffsetFormatter.withZone(tz).print(millis);
	}

	public interface CountdownHandler {
		public void onStart(int secondsTillNotify);

		public void onNotifyBefore(int secondsToWait);

		public void onNotify(int secondsRemain);

		public void onEnd();
	}
}

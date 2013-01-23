/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thelq.pircbotx.commands;

import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.pircbotx.hooks.events.MessageEvent;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class CountdownUtils {
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

	protected static void registerTime(List<DateTime> notifyTimes, DateTime startDate, DateTime endDate, int seconds) {
		//If the requested seconds is still in the period, add to list
		DateTime notifyTime = endDate.minusSeconds(seconds);
		if (notifyTime.isAfter(startDate) || notifyTime.isEqual(startDate))
			notifyTimes.add(notifyTime);
	}

	protected static void respondNow(MessageEvent event, String message) {
		//The send method chain sends via queue, we need to skip that
		event.getBot().sendRawLineNow("PRIVMSG " + event.getChannel().getName() + " :"
				+ event.getUser().getNick() + ": " + message);
	}
}

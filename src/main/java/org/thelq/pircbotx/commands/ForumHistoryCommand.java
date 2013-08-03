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
/**
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thelq.pircbotx.commands;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.pircbotx.Channel;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.DisconnectEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PartEvent;
import org.thelq.pircbotx.BasicCommand;

/**
 *
 * @author Leon
 */
@Slf4j
public class ForumHistoryCommand extends ListenerAdapter implements BasicCommand {
	protected static final ScheduledExecutorService backgroundMonitor = Executors.newSingleThreadScheduledExecutor();
	@Getter
	protected final String help = "Get latest posts from lyokofreak: ?forumHistory <optional:number>";
	protected final URL feedUrl;
	protected final String cookies;
	protected ScheduledFuture<?> feedMonitorFuture;

	public ForumHistoryCommand(Properties properties) throws MalformedURLException {
		this(properties.getProperty("lyokofreak.cookies"));
		Preconditions.checkNotNull(cookies, "Must specify cookies");
	}

	public ForumHistoryCommand(String cookies) throws MalformedURLException {
		feedUrl = new URL("http://lyokofreak.net/forum/feed.php");
		this.cookies = cookies;
	}

	@Override
	public void onJoin(JoinEvent event) throws Exception {
		if (event.getChannel().getName().equals("#lyokofreak"))
			feedMonitorFuture = backgroundMonitor.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
				}
			}, 5, 5, TimeUnit.MINUTES);
	}

	@Override
	public void onPart(PartEvent event) throws Exception {
		stopMonitor();
	}

	@Override
	public void onDisconnect(DisconnectEvent event) throws Exception {
		stopMonitor();
	}

	protected void stopMonitor() {
		if (feedMonitorFuture != null) {
			feedMonitorFuture.cancel(true);
			feedMonitorFuture = null;
		}
	}

	@Override
	public void onMessage(MessageEvent event) throws Exception {
		if (!StringUtils.startsWithIgnoreCase(event.getMessage(), "?forumHistory"))
			return;
		String[] messageSplit = StringUtils.split(event.getMessage(), " ");
		int number = 7;
		if (messageSplit.length == 2)
			try {
				int parsedNumber = Integer.parseInt(messageSplit[1]);
				if (parsedNumber < number)
					number = parsedNumber;
			} catch (NumberFormatException e) {
				event.respond("Unknown number " + messageSplit[1]);
			}
		ImmutableList<RssEntry> feed = getFeed();
		for (int i = 0; i < number; i++) {
			RssEntry curEntry = feed.get(i);
			event.respond(curEntry.toNiceString());
		}
	}

	protected ImmutableList<RssEntry> getFeed() throws IOException, DocumentException {
		log.info("Grabbing lyokofreak feed");
		URLConnection feedConnection = feedUrl.openConnection();
		feedConnection.setRequestProperty("Cookie", cookies);
		feedConnection.setDoOutput(true);
		feedConnection.connect();
		SAXReader reader = new SAXReader();
		Document document = reader.read(feedConnection.getInputStream());
		List<Element> elements = document.getRootElement().elements("entry");
		return ImmutableList.copyOf(Iterables.transform(elements, new Function<Element, RssEntry>() {
			@Override
			public RssEntry apply(Element input) {
				return new RssEntry(input);
			}
		}));
	}

	protected class FeedMonitor implements Runnable {
		protected final Channel notifyChannel;
		protected DateTime lastPostTime;

		public FeedMonitor(Channel notifyChannel) throws IOException, DocumentException {
			log.info("Setting up feed monitor");
			ImmutableList<RssEntry> feed = getFeed();
			lastPostTime = feed.get(feed.size() - 1).getPublished();
			this.notifyChannel = notifyChannel;
		}

		@Override
		public void run() {
			try {
				for (RssEntry curEntry : getFeed()) {
					if (!curEntry.getPublished().isAfter(lastPostTime))
						continue;
					notifyChannel.send().message("New post by " + curEntry.toNiceString());
				}
			} catch (Exception ex) {
				log.error("Could not get lyokofreak feed", ex);
			}

		}
	}

	@Data
	protected static class RssEntry {
		protected static final DateTimeFormatter DATE_PARSER = ISODateTimeFormat.dateTimeParser();
		protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.fullDate();
		protected static final PeriodFormatter PERIOD_FORMATTER = PeriodFormat.getDefault();
		protected static final PeriodType PERIOD_TO_MINUTES = PeriodType.standard().withMillisRemoved();
		protected final String title;
		protected final String author;
		protected final String link;
		protected final String category;
		protected final DateTime published;

		public RssEntry(Element entryElement) {
			category = entryElement.element("category").attributeValue("term");
			title = StringUtils.removeStart(entryElement.elementText("title"), category + " • ");
			author = entryElement.element("author").elementText("name");
			link = entryElement.element("link").attributeValue("href");
			published = DATE_PARSER.parseDateTime(entryElement.elementText("published"));
		}

		public String getPublishedString() {
			return DATE_FORMATTER.print(published);
		}

		public String getPublishedAge() {
			return PERIOD_FORMATTER.print(new Period(published, DateTime.now(), PERIOD_TO_MINUTES));
		}

		public String toNiceString() {
			return getAuthor() + " " + getPublishedAge() + " ago: "
					+ getTitle() + " in " + getCategory() + " " + getLink();
		}
	}

	public static void main(String[] args) throws MalformedURLException, IllegalArgumentException, IOException, DocumentException {
		new ForumHistoryCommand("").getFeed();
	}
}

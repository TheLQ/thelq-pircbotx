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

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ActionEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.KickEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.ModeEvent;
import org.pircbotx.hooks.events.NickChangeEvent;
import org.pircbotx.hooks.events.NoticeEvent;
import org.pircbotx.hooks.events.PartEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.events.TopicEvent;
import org.thelq.pircbotx.BasicCommand;
import org.thelq.pircbotx.Stats;

/**
 *
 * @author Leon
 */
public class StatsCommand extends ListenerAdapter implements BasicCommand {
	@Getter
	protected String help = "Get stats";

	@Override
	public void onMessage(MessageEvent event) throws Exception {
		ListenerUtils.addHistory(event, Stats.HistoryType.NORMAL, event.getChannel(), event.getUser(), event.getMessage());
		Stats botStats = ListenerUtils.getStats(event);
		botStats.getReceivedMessages().incrementAndGet();

		if (ListenerUtils.isCommand(event.getMessage(), "stats")) {
			event.respond("Processed " + botStats.getReceivedMessages() + " messages, "
					+ botStats.getReceivedCommands() + " commands, "
					+ "for " + botStats.getUptime());
			event.respond("More data available at http://thelq-pircbotx.thelq.cloudbees.net");
		}
	}

	@Override
	public void onPrivateMessage(PrivateMessageEvent event) throws Exception {
		ListenerUtils.addHistory(event, Stats.HistoryType.NORMAL, null, event.getUser(), event.getMessage());
	}

	@Override
	public void onAction(ActionEvent event) throws Exception {
		ListenerUtils.addHistory(event, Stats.HistoryType.ITALIC, event.getChannel(), event.getUser(), event.getAction());
	}

	@Override
	public void onNotice(NoticeEvent event) throws Exception {
		ListenerUtils.addHistory(event, Stats.HistoryType.BOLD, event.getChannel(), event.getUser(), event.getNotice());
	}

	@Override
	public void onJoin(JoinEvent event) throws Exception {
		ListenerUtils.addHistory(event, Stats.HistoryType.BACKGROUND, event.getChannel(), event.getUser(), getLongname(event.getUser()) + " joined");
	}

	@Override
	public void onPart(PartEvent event) throws Exception {
		ListenerUtils.addHistory(event, Stats.HistoryType.BACKGROUND, event.getChannel(), event.getUser(), getLongname(event.getUser()) + " parted");
	}

	@Override
	public void onKick(KickEvent event) throws Exception {
		ListenerUtils.addHistory(event, Stats.HistoryType.BACKGROUND,
				ImmutableList.of(event.getChannel()),
				ImmutableList.of(event.getUser(), event.getRecipient()),
				getLongname(event.getUser()) + " kicked " + getLongname(event.getRecipient()));
	}

	@Override
	public void onMode(ModeEvent event) throws Exception {
		if (event.getUser() != null)
			ListenerUtils.addHistory(event, Stats.HistoryType.BACKGROUND,
					event.getChannel(),
					event.getUser(),
					getLongname(event.getUser()) + " set mode to " + event.getMode());
	}

	@Override
	public void onNickChange(NickChangeEvent event) throws Exception {
		//Use long version so we can use old nick
		User user = event.getUser();
		ListenerUtils.addHistory(event, Stats.HistoryType.BACKGROUND,
				ImmutableList.copyOf(event.getUser().getChannels()),
				ImmutableList.of(user),
				event.getOldNick() + "!" + user.getLogin() + "@" + user.getHostmask() + " changed nick to " + event.getNewNick());
	}

	@Override
	public void onTopic(TopicEvent event) throws Exception {
		ListenerUtils.addHistory(event, Stats.HistoryType.BACKGROUND,
				event.getChannel(),
				event.getUser(),
				getLongname(event.getUser()) + " changed topic to " + event.getTopic());
	}

	protected String getLongname(User user) {
		return user.getNick() + "!" + user.getLogin() + "@" + user.getHostmask();
	}
}

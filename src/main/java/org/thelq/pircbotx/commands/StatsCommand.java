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

import lombok.Getter;
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
import org.pircbotx.hooks.types.GenericMessageEvent;
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
		ListenerUtils.addHistory(event);
		Stats botStats = ListenerUtils.getStats(event);
		botStats.getReceivedMessages().incrementAndGet();
		
		if(event.getMessage().startsWith("?stats")) {
			event.respond("Processed " + botStats.getReceivedMessages() + " messages, "
					+ botStats.getReceivedCommands() + " commands, "
					+ "for " + botStats.getUptime());
			event.respond("More data available at http://thelq-pircbotx.thelq.cloudbees.net");
		}
	}

	@Override
	public void onAction(ActionEvent event) throws Exception {
		ListenerUtils.addHistory(event);
	}

	@Override
	public void onNotice(NoticeEvent event) throws Exception {
		ListenerUtils.addHistory(event);
	}

	@Override
	public void onJoin(JoinEvent event) throws Exception {
		ListenerUtils.addHistory(event);
	}

	@Override
	public void onPart(PartEvent event) throws Exception {
		ListenerUtils.addHistory(event);
	}

	@Override
	public void onKick(KickEvent event) throws Exception {
		ListenerUtils.addHistory(event);
	}

	@Override
	public void onMode(ModeEvent event) throws Exception {
		ListenerUtils.addHistory(event);
	}

	@Override
	public void onNickChange(NickChangeEvent event) throws Exception {
		ListenerUtils.addHistory(event);
	}

	@Override
	public void onPrivateMessage(PrivateMessageEvent event) throws Exception {
		ListenerUtils.addHistory(event);
	}

	@Override
	public void onTopic(TopicEvent event) throws Exception {
		ListenerUtils.addHistory(event);
	}
	
	
	
}

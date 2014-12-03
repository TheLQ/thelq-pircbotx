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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thelq.pircbotx.commands;

import org.thelq.pircbotx.commands.api.AbstractCommand;
import com.google.common.collect.ImmutableList;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.UserLevel;
import org.pircbotx.Utils;
import org.pircbotx.hooks.WaitForQueue;
import org.pircbotx.hooks.events.OpEvent;
import org.pircbotx.hooks.events.SetModeratedEvent;
import org.pircbotx.hooks.types.GenericEvent;
import org.thelq.pircbotx.commands.api.CommandCall;

/**
 *
 * @author Leon
 */
@Slf4j
public class ChannelModeratedCommand extends AbstractCommand {
	public ChannelModeratedCommand() {
		addCommand("m", "", this::onCommandStart);
	}

	public void onCommandStart(GenericEvent event, Channel channel, User user, ImmutableList<String> args) throws Exception {
		if (StringUtils.split(channel.getMode(), ' ')[0].contains("m")) {
			user.send().notice("Channel is already at +m");
		}

		Set<UserLevel> ourLevels = event.getBot().getUserChannelDao().getLevels(channel, user);
		if (!ourLevels.contains(UserLevel.OP)) {
			//Lets hope we have chanserv rights
			user.send().notice("Acquiring op");
			WaitForQueue queue = new WaitForQueue(event.getBot());
			event.getBot().sendRaw().rawLineNow("chanserv op " + channel.getName() + " " + event.getBot().getNick());
			while (true) {
				OpEvent opEvent = queue.waitFor(OpEvent.class, 20, TimeUnit.SECONDS);
				if (opEvent == null) {
					throw new Exception(Utils.format("Timeout waiting for op in channel {} requested by user {}",
							channel.getName(), user.getNick()));
				}
				log.trace("Received op " + opEvent);
				if (opEvent.getChannel() == channel && opEvent.getRecipient() == opEvent.getBot().getUserBot())
					break;
			}
			log.debug("Sucesfully opped, waiting for mode set to succeed");

			channel.send().setMode("+m");
			while (true) {
				SetModeratedEvent modEvent = queue.waitFor(SetModeratedEvent.class, 20, TimeUnit.SECONDS);
				if (modEvent == null) {
					throw new Exception(Utils.format("Timeout waiting for +m in channel {} requested by user {}",
							channel.getName(), user.getNick()));
				}
				log.trace("Received moderated " + modEvent);
				if (modEvent.getChannel() == channel)
					break;
			}
		}

		String tempCommand = addTemporaryCommand("unset -m for " + channel.getName(), (unevent, unSourceChannel, unUser, unArgs) -> {
			channel.send().setMode("-m");
			return true;
		});
		user.send().notice(Utils.format("Moderated mode set, use '/msg {} {}' to remove", event.getBot().getNick(), CommandCall.PREFIX + tempCommand));
	}
}

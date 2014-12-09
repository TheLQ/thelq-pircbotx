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

import com.google.common.collect.ImmutableList;
import org.thelq.pircbotx.commands.api.AbstractCommand;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.UserHostmask;
import org.pircbotx.UserLevel;
import org.pircbotx.Utils;
import org.pircbotx.hooks.WaitForQueue;
import org.pircbotx.hooks.events.ModeEvent;
import org.pircbotx.hooks.events.OpEvent;
import org.pircbotx.hooks.types.GenericEvent;
import org.thelq.pircbotx.commands.api.CommandCall;

/**
 *
 * @author Leon
 */
@Slf4j
public class ModeCommands extends AbstractCommand {
	public ModeCommands() {
		addCommandChannelMode('m', "moderated (only voice+ users can talk)", true,
				(event, args) -> args.isEmpty() ? "" : args.get(0));
		addCommandChannelMode('b', "user ban, argument can be a hostmask or nick", false, (event, args) -> {
			String rawHostmask = args.get(0);
			UserHostmask parsedHostmask = new UserHostmask(event.getBot(), rawHostmask);
			if (StringUtils.isAnyBlank(parsedHostmask.getLogin(), parsedHostmask.getHostname())) {
				//This is not a hostmask, must be a user nick
				if (event.getBot().getUserChannelDao().containsUser(rawHostmask)) {
					event.respond("User " + rawHostmask + " doesn't exist");
				}
				log.debug("parsedHostmask before getUser: " + parsedHostmask);
				parsedHostmask = event.getBot().getUserChannelDao().getUser(rawHostmask);
			}
			return parsedHostmask.getHostmask();
		});
	}

	protected final void addCommandChannelMode(char modeLetter, @NonNull String helpSuffix, boolean channelMode, @NonNull BiFunction<GenericEvent, ImmutableList<String>, String> argHandler) {
		addCommand("" + modeLetter, "Set channel " + helpSuffix, (event, channel, user, args)
				-> setChannelMode(event, channel, user, modeLetter, true, channelMode, argHandler.apply(event, args)));
		addCommand("-" + modeLetter, "Set channel " + helpSuffix, (event, channel, user, args)
				-> setChannelMode(event, channel, user, modeLetter, false, channelMode, argHandler.apply(event, args)));
	}

	protected void setChannelMode(@NonNull GenericEvent event, Channel channel, @NonNull User user, char modeLetter, boolean enabled, boolean channelMode, @NonNull String arg) throws Exception {
		if (channel == null) {
			if (StringUtils.isBlank(arg)) {
				user.send().notice("Unknown channel, must run command in channel or in PM");
				return;
			}
			if (!event.getBot().getUserChannelDao().containsChannel(arg)) {
				user.send().notice("Unknown channel " + arg);
				return;
			}
			channel = event.getBot().getUserChannelDao().getChannel(arg);
			arg = "";
		}

		if (enabled && channelMode && StringUtils.split(channel.getMode(), ' ')[0].contains(String.valueOf(modeLetter))) {
			user.send().notice("Channel is already at +" + modeLetter);
		}

		WaitForQueue queue = new WaitForQueue(event.getBot());

		//Need op rights
		Set<UserLevel> ourLevels = event.getBot().getUserChannelDao().getLevels(channel, event.getBot().getUserBot());
		if (!ourLevels.contains(UserLevel.OP)) {
			user.send().notice("Acquiring op from chanserv");
			event.getBot().sendRaw().rawLineNow("CHANSERV OP " + channel.getName() + " " + event.getBot().getNick());
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
		}

		//Change mode
		String modeChange = (enabled ? "+" : "-") + modeLetter + StringUtils.defaultIfBlank(" " + arg, "");
		String modeSetRemove = enabled ? "set" : "removed";
		event.getBot().sendRaw().rawLineNow("MODE " + channel.getName() + " " + modeChange);
		while (true) {
			ModeEvent modEvent = queue.waitFor(ModeEvent.class, 20, TimeUnit.SECONDS);
			if (modEvent == null) {
				throw new Exception(Utils.format("Timeout waiting for {} in channel {} requested by user {}",
						modeChange, channel.getName(), user.getNick()));
			}
			log.trace("Received mode " + modEvent);
			if (modEvent.getChannel() == channel && modEvent.getUser() == modEvent.getBot().getUserBot())
				break;
		}
		log.debug("Mode change {} succeeded", modeChange);

		String unModeChange = CommandCall.PREFIX + (enabled ? "-" : "") + modeChange.substring(1);
		user.send().notice(Utils.format("Mode {} {}, use '/msg {} {} {}' to undo", 
				modeChange, modeSetRemove, event.getBot().getNick(), unModeChange, channel.getName()));
	}
}

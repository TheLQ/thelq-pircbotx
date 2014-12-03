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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thelq.pircbotx.commands;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.managers.ListenerManager;
import org.pircbotx.hooks.types.GenericEvent;
import org.thelq.pircbotx.Main;

/**
 *
 * @author Leon
 */
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractCommand extends ListenerAdapter {
	public static ImmutableSet<AbstractCommand> findAllCommands(ListenerManager manager) {
		return FluentIterable.from(manager.getListeners())
				.filter(AbstractCommand.class)
				.toSet();
	}

	@NonNull
	public final String name;
	@NonNull
	public final String help;
	public final boolean admin;

	public AbstractCommand(String name) {
		this(name, "", false);
	}

	public AbstractCommand(String name, String help) {
		this(name, help, false);
	}

	@Override
	public void onPrivateMessage(PrivateMessageEvent event) throws Exception {
		parseMessage(event, null, event.getUser(), event.getMessage());
	}

	@Override
	public void onMessage(MessageEvent event) throws Exception {
		parseMessage(event, event.getChannel(), event.getUser(), event.getMessage());
	}

	public void parseMessage(GenericEvent event, @Nullable Channel channel, User user, String message) throws Exception {
		CommandCall call = CommandCall.parse(event, message, name);
		if (call == null)
			return;

		//Block failed admins
		if (admin && !Main.admins.isAdmin(user)) {
			return;
		}

		onCommand(event, channel, user, call.args);
	}

	abstract void onCommand(GenericEvent event, @Nullable Channel channel, User user, ImmutableList<String> args) throws Exception;
}

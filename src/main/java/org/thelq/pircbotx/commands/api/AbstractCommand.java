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
package org.thelq.pircbotx.commands.api;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
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
	public static final Random RANDOM = new Random();

	private final Map<String, CommandEntry> commands = Maps.newHashMap();

	public String addTemporaryCommand(String help, final CommandTemporary run) {
		String commandNameRaw = UUID.randomUUID().toString().replace("-", "");
		final String commandName = commandNameRaw.substring(0, commandNameRaw.length() / 2);
		addCommand(commandName, help, (event, channel, user, args) -> {
			if (run.onCommandTemp(event, channel, user, args))
				removeCommand(commandName);
		});
		return commandName;
	}

	public void addCommand(String command, String help, CommandRun run) {
		CommandEntry entry = new CommandEntry(run, help);
		commands.put(command, entry);
	}

	public void removeCommand(String command) {
		commands.remove(command);
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
		//Parse the raw message into a command call if its in the valid format
		CommandCall call = CommandCall.parse(event, message);
		if (call == null)
			return;

		CommandEntry command = commands.get(call.command);
		if (command == null)
			return;

		//Block failed admins
		if (command.admin && !Main.admins.isAdmin(user)) {
			return;
		}

		command.run.onCommand(event, channel, user, call.args);
	}

	@RequiredArgsConstructor
	protected static class CommandEntry {
		@NonNull
		public final CommandRun run;
		@NonNull
		public final String help;
		public final boolean admin;

		public CommandEntry(CommandRun run) {
			this(run, "", false);
		}

		public CommandEntry(CommandRun run, String help) {
			this(run, help, false);
		}
	}

	public static interface CommandRun {
		public void onCommand(GenericEvent event, @Nullable Channel channel, User user, ImmutableList<String> args) throws Exception;
	}

	public static interface CommandTemporary {
		public boolean onCommandTemp(GenericEvent event, @Nullable Channel channel, User user, ImmutableList<String> args) throws Exception;
	}
}

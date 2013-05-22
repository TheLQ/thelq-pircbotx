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
import java.util.List;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.thelq.pircbotx.BasicCommand;

/**
 * Show the help of all the commands on this bot. Uses the name of the class
 * as the name of the command, minus the "Command" suffix
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class HelpCommand extends ListenerAdapter implements BasicCommand {
	@Getter
	protected String help = "Show commands and help for them";

	@Override
	public void onMessage(MessageEvent event) throws Exception {
		String message = event.getMessage().toLowerCase();
		if (!message.startsWith("?help"))
			return;

		ListenerUtils.incrimentCommands(event);
		String[] messageParts = message.split(" ");
		if (messageParts.length > 2)
			event.respond("Too many arguments");
		else if (messageParts.length == 2) {
			//User gave us a command, find it and show the help
			for (Listener curListener : event.getBot().getConfiguration().getListenerManager().getListeners()) {
				if (!(curListener instanceof BasicCommand))
					continue;

				BasicCommand command = (BasicCommand) curListener;
				if (StringUtils.startsWithIgnoreCase(getCommandName(command), messageParts[1])) {
					//Found it!
					event.respond(getCommandName(command) + " help: " + command.getHelp());
					return;
				}
			}
			//If we get here then nothing was found
			event.respond("Command " + messageParts[1] + " doesn't exist");
		} else {
			//Build a list of names of all the available commands
			List<String> commands = new ArrayList();
			for (Listener curListener : event.getBot().getConfiguration().getListenerManager().getListeners())
				if (curListener instanceof BasicCommand)
					commands.add(getCommandName((BasicCommand) curListener));

			//Compile and send
			event.respond("Available commands: " + StringUtils.join(commands, ", "));
		}
	}

	protected static String getCommandName(BasicCommand command) {
		return StringUtils.removeEnd(command.getClass().getSimpleName(), "Command");
	}
}

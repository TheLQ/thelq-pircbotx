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
import org.pircbotx.User;
import org.pircbotx.UserLevel;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.thelq.pircbotx.BasicCommand;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class MyLevelsCommand extends ListenerAdapter implements BasicCommand {
	@Getter
	protected String help = "Show the levels you have in the channel. Use ?mylevels bot to get levels of bot";

	@Override
	public void onMessage(MessageEvent event) throws Exception {
		if (!event.getMessage().startsWith("?mylevels"))
			return;

		ListenerUtils.incrimentCommands(event);
		//Which user are we getting info for
		String[] messageParts = event.getMessage().split(" ");
		boolean isBot = messageParts.length == 2 && messageParts[1].equalsIgnoreCase("bot");
		User user = isBot ? event.getBot().getUserBot() : event.getUser();
		
		event.respond((isBot ? "I am" : "You are") + ": " + user.getUserLevels(event.getChannel()).toString());
	}
}

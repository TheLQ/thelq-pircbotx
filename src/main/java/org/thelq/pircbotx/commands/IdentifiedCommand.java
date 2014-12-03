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
package org.thelq.pircbotx.commands;

import lombok.Getter;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.thelq.pircbotx.BasicCommand;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class IdentifiedCommand extends ListenerAdapter implements BasicCommand {
	@Getter
	protected String help = "Are you identified with NickServ?";

	@Override
	public void onMessage(MessageEvent event) throws Exception {
		if (ListenerUtils.isCommand(event.getMessage(), "identified")) {
			ListenerUtils.incrimentCommands(event);
			event.respond(event.getUser().isVerified() ? "You are identified" : "You are not identified");
		}
	}
}

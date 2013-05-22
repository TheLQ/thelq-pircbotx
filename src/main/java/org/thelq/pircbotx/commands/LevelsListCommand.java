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

import java.util.Collection;
import lombok.Getter;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.thelq.pircbotx.BasicCommand;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class LevelsListCommand extends ListenerAdapter implements BasicCommand {
	@Getter
	protected String help = "List who is an op, voice, owner, superop, and halfop";

	@Override
	public void onMessage(MessageEvent event) throws Exception {
		if (event.getMessage().startsWith("?levelslist")) {
			ListenerUtils.incrimentCommands(event);
			event.respond("Ops: " + getUserNames(event.getChannel().getOps())
					+ " | Voices: " + getUserNames(event.getChannel().getVoices())
					+ " | Owners: " + getUserNames(event.getChannel().getOwners())
					+ " | Super ops: " + getUserNames(event.getChannel().getSuperOps())
					+ " | Half ops: " + getUserNames(event.getChannel().getHalfOps()));
		}
	}

	protected static String getUserNames(Collection<User> users) {
		StringBuilder builder = new StringBuilder();
		for (User curUser : users)
			builder.append(curUser.getNick()).append(", ");
		if (builder.length() > 2)
			builder.setLength(builder.length() - 2);
		return builder.toString();
	}
}

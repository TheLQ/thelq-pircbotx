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

import org.thelq.pircbotx.commands.api.AbstractCommand;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.types.GenericEvent;

/**
 *
 * @author Leon
 */
public class RawCommand extends AbstractCommand {

	public RawCommand() {
		super("raw", "raw commands", true);
	}

	@Override
	public void onCommand(GenericEvent event, Channel channel, User user, ImmutableList<String> args) throws Exception {
		event.getBot().sendRaw().rawLine(StringUtils.join(args, " "));
	}

}

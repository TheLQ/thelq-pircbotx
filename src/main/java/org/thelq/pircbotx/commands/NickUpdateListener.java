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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thelq.pircbotx.commands;

import com.google.common.base.CharMatcher;
import org.apache.commons.lang3.StringUtils;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.QuitEvent;
import org.pircbotx.hooks.events.UserListEvent;

/**
 * Handle situations during restarts where the old bot instance still has our nick
 * @author Leon
 */
public class NickUpdateListener extends ListenerAdapter {
	@Override
	public void onQuit(QuitEvent event) throws Exception {
		if (!needRenaming(event) || !sharePrefix(event, event.getUser()))
			return;
		//Other bot just quit, cycle
		cycle(event);
	}

	@Override
	public void onUserList(UserListEvent event) throws Exception {
		if (!needRenaming(event))
			return;
		//See if we share a nick with any other user
		for(User curUser : event.getChannel().getUsers())
			if(sharePrefix(event, curUser))
				return;
		//Nope, cycle
		cycle(event);
	}
	
	protected void cycle(Event event) {
		//The old bot just quit, take over
		event.getBot().sendIRC().changeNick(event.getBot().getConfiguration().getName());
		event.getBot().sendIRC().identify(event.getBot().getConfiguration().getNickservPassword());
		
		//Disabled, all current servers have a properly setup chanserv that autovoices on nick change
		//for (Channel curChannel : event.getBot().getUserChannelDao().getAllChannels())
		//	curChannel.send().cycle();
	}

	protected boolean needRenaming(Event event) {
		String botNick = event.getBot().getNick();
		return CharMatcher.DIGIT.matches(botNick.charAt(botNick.length() - 1));
	}

	protected boolean sharePrefix(Event event, User user) {
		return StringUtils.getCommonPrefix(user.getNick(), event.getBot().getNick()).equals(event.getBot().getConfiguration().getName());
	}
}

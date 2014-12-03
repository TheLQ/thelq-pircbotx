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
package org.thelq.pircbotx;

import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.managers.LogManagerExceptionHandler;
import org.pircbotx.hooks.types.GenericChannelEvent;
import org.pircbotx.hooks.types.GenericUserEvent;

/**
 *
 * @author Leon
 */
public class AdminManagerExceptionHandler extends LogManagerExceptionHandler {

	@Override
	public void onException(Listener listener, Event event, Throwable exception) {
		super.onException(listener, event, exception);
		String source = (event instanceof GenericChannelEvent) ? ((GenericChannelEvent)event).getChannel().getName()
				: (event instanceof GenericUserEvent) ? ((GenericUserEvent)event).getUser().getNick() : null;
		for(String nick : Main.admins.adminsActive)
			if(event.getBot().getUserChannelDao().containsUser(nick))
				event.getBot().send().notice(nick, "ERROR In " + source + " " + exception.toString());
	}
	
}

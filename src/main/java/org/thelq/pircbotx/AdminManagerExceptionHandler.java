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

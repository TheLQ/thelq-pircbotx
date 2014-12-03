/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thelq.pircbotx.commands;

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
	void onCommand(GenericEvent event, Channel channel, User user, ImmutableList<String> args) throws Exception {
		event.getBot().sendRaw().rawLine(StringUtils.join(args, " "));
	}

}

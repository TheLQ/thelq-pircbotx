package org.thelq.pircbotx.commands;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.pircbotx.User;
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

		//Which user are we getting info for
		String[] messageParts = event.getMessage().split(" ");
		User user = event.getUser();
		if (messageParts.length == 2 && messageParts[1].equalsIgnoreCase("bot"))
			user = event.getBot().getUserBot();

		List<String> modes = new ArrayList();
		if (event.getChannel().isOp(user))
			modes.add("op");
		if (event.getChannel().hasVoice(user))
			modes.add("voice");
		if (event.getChannel().isOwner(user))
			modes.add("owner");
		if (event.getChannel().isSuperOp(user))
			modes.add("superop");
		if (event.getChannel().isHalfOp(user))
			modes.add("halfop");
		event.respond("You are: " + modes.toString());
	}
}

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
		if (event.getMessage().startsWith("?levelslist"))
			event.respond("Ops: " + getUserNames(event.getChannel().getOps())
					+ " | Voices" + getUserNames(event.getChannel().getVoices())
					+ " | Owners" + getUserNames(event.getChannel().getOwners())
					+ " | Super ops: " + getUserNames(event.getChannel().getSuperOps())
					+ " | Half ops: " + getUserNames(event.getChannel().getHalfOps()));
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

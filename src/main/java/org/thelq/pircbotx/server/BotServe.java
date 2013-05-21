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
package org.thelq.pircbotx.server;

import Acme.Serve.Serve;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.view.VelocityLayoutServlet;
import org.apache.velocity.tools.view.VelocityViewServlet;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.thelq.pircbotx.Main;

/**
 *
 * @author Leon
 */
public class BotServe extends Serve {
	protected final String rootPath;

	public BotServe(int port) {
		super(ImmutableMap.builder()
				.put(ARG_PORT, port)
				.put(ARG_NOHUP, "nohup")
				//.put(ARG_WORK_DIRECTORY, "c:\\users")
				.build(), System.out);

		File classesFolder;
		if ((classesFolder = new File("src\\main\\resources")).exists())
			rootPath = classesFolder.getAbsolutePath();
		else if ((classesFolder = new File("app")).exists())
			rootPath = classesFolder.getAbsolutePath();
		else
			rootPath = new File(".").getAbsolutePath();

		VelocityViewServlet velocityServlet = new BotVelocityServlet();
		addServlet("/", velocityServlet);
		addServlet("/myServe", new HttpServlet() {
			@Override
			protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
				response.getWriter().write(getServletContext().getClass().toString());
			}
		});
	}

	@Override
	public String getRealPath(String path) {
		//Set home page to index.vm
		if(path.equals("/"))
			path = "/index.vm";
		//Add .vm if nessesary to path
		else if(!path.endsWith(".vm"))
			path = path + ".vm";
		return rootPath + path;
	}

	@Slf4j
	@RequiredArgsConstructor
	protected static class BotVelocityServlet extends VelocityLayoutServlet {

		@Override
		protected void fillContext(Context context, HttpServletRequest request) {
			log.debug(getVelocityProperty("webapp.resource.loader.cache", "none at all"));
			context.put("manager", Main.MANAGER);
			context.put("StringUtils", StringUtils.class);
			
			//Handle bot parameter
			String botIdRaw = request.getParameter("botId");
			if(StringUtils.isNotBlank(botIdRaw))
				context.put("bot", Main.MANAGER.getBotById(Integer.parseInt(botIdRaw)));
			
			//Handle userId parameter
			String userIdRaw = request.getParameter("userId");
			if(StringUtils.isNotBlank(userIdRaw)) {
				boolean ran = false;
				Outer:
				for(PircBotX curBot : Main.MANAGER.getBots())
					for(User curUser : curBot.getUserChannelDao().getAllUsers()) {
						if(curUser.getUserId().equals(UUID.fromString(userIdRaw))) {
							context.put("user", curUser);
							ran = true;
							break Outer;
						}
					}
 				
				if(!ran)
					throw new RuntimeException("Invalid user UUID");
			}
			
			//Handle channelId parameter
			String channelIdRaw = request.getParameter("channelId");
			if(StringUtils.isNotBlank(channelIdRaw)) {
				boolean ran = false;
				Outer:
				for(PircBotX curBot : Main.MANAGER.getBots())
					for(Channel channel : curBot.getUserChannelDao().getAllChannels()) {
						if(channel.getChannelId().equals(UUID.fromString(channelIdRaw))) {
							context.put("channel", channel);
							ran = true;
							break Outer;
						}
					}
 				
				if(!ran)
					throw new RuntimeException("Invalid channel UUID");
			}
		}
	}
}

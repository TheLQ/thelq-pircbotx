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
package org.thelq.pircbotx;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.view.VelocityLayoutServlet;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;

/**
 *
 * @author Leon
 */
@Slf4j
public class BotServe {
	protected final Server server;

	public BotServe(int port) throws Exception {
		this.server = new Server(port);
		ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		servletHandler.addServlet(new ServletHolder(new BotVelocityServlet()), "/*");
		servletHandler.addServlet(new ServletHolder(new PingServlet()), "/cloudbees-alive/*");
		server.setHandler(servletHandler);

		//Find the root path
		String rootPath;
		File classesFolder;
		if ((classesFolder = new File("src\\main\\resources")).exists())
			rootPath = classesFolder.getAbsolutePath();
		else if ((classesFolder = new File("app")).exists())
			rootPath = classesFolder.getAbsolutePath();
		else
			rootPath = new File(".").getAbsolutePath();
		log.info("Set resource base path to " + rootPath);
		servletHandler.setResourceBase(rootPath);

		server.start();
	}

	@Slf4j
	public static class PingServlet extends HttpServlet {
		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			log.info("Received keepalive GET - " + request.getPathInfo());
		}

		@Override
		protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
			log.info("Received keepalive POST - " + request.getPathInfo());
		}
	}

	@Slf4j
	public static class BotVelocityServlet extends VelocityLayoutServlet {
		@Override
		public void init(ServletConfig config) throws ServletException {
			super.init(config);
			getVelocityView().setPublishToolboxes(false);
		}

		@Override
		protected void doRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
			String pathInfo = request.getPathInfo();
			Request requestJetty = (Request) request;

			if (pathInfo.equals("/"))
				//Requesting home page
				requestJetty.setPathInfo("/index.vm");
			else if (pathInfo.lastIndexOf('.') < pathInfo.lastIndexOf('/'))
				//No extension, assume vm page
				requestJetty.setPathInfo(pathInfo + ".vm");
			super.doRequest(request, response);
		}

		@Override
		protected void fillContext(Context context, HttpServletRequest request) {
			log.debug(getVelocityProperty("webapp.resource.loader.cache", "none at all"));
			context.put("manager", Main.MANAGER);
			context.put("StringUtils", StringUtils.class);

			//Handle botId parameter
			String botIdRaw = request.getParameter("botId");
			if (StringUtils.isNotBlank(botIdRaw))
				context.put("bot", Main.MANAGER.getBotById(Integer.parseInt(botIdRaw)));

			//Handle userId parameter
			String userIdRaw = request.getParameter("userId");
			if (StringUtils.isNotBlank(userIdRaw)) {
				boolean ran = false;
				Outer:
				for (PircBotX curBot : Main.MANAGER.getBots())
					for (User curUser : curBot.getUserChannelDao().getAllUsers())
						if (curUser.getUserId().equals(UUID.fromString(userIdRaw))) {
							context.put("user", curUser);
							ran = true;
							break Outer;
						}

				if (!ran)
					throw new RuntimeException("Invalid user UUID");
			}

			//Handle channelId parameter
			String channelIdRaw = request.getParameter("channelId");
			if (StringUtils.isNotBlank(channelIdRaw)) {
				boolean ran = false;
				Outer:
				for (PircBotX curBot : Main.MANAGER.getBots())
					for (Channel channel : curBot.getUserChannelDao().getAllChannels())
						if (channel.getChannelId().equals(UUID.fromString(channelIdRaw))) {
							context.put("channel", channel);
							ran = true;
							break Outer;
						}

				if (!ran)
					throw new RuntimeException("Invalid channel UUID");
			}
		}
	}
}

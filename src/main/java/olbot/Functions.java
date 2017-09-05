package olbot;

import java.sql.ResultSet;
import java.util.List;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;

public class Functions { // for command actions

	public static void setup(MessageReceivedEvent event, ImprovedString message) {
		if (event.getAuthor().getPermissionsForGuild(event.getGuild())
				.contains(Permissions.ADMINISTRATOR)) {
			List<IChannel> channelMentions = event.getMessage().getChannelMentions();
			if (channelMentions.size() == 1) {
				IChannel channel = channelMentions.get(0);
				if (message.words.size() >= 3) {
					String roleName = message.getContent(2, " ");
					if (event.getGuild().getRolesByName(roleName).size() >= 1) {
						long channelId = channel.getLongID();
						String query = String.format("INSERT INTO servers"
								+ "(server_id, channel_id, role_name) "
								+ "VALUES(%d, %d, '%s')",
								event.getGuild().getLongID(),
								channelId,
								roleName.replace("'", "\\'"));
						Handler.executeUpdate(query);
						event.getChannel().sendMessage(
								Utils.generateSuccess(
										String.format("Server has been set up with "
												+ "channel <#%d> and Open Lobby Creator role "
												+ "**%s**", channelId, roleName))
						);
					} else {
						event.getChannel().sendMessage(Utils.generateWarning(
								"Role provided does not exist!"));
					}
				} else {
					event.getChannel().sendMessage(Utils.generateWarning("Not "
							+ "enough data provided."));
				}
			} else {
				event.getChannel().sendMessage(Utils.generateWarning(
						Utils.INVALID_FORMAT));
			}
		} else {
			event.getChannel().sendMessage(Utils.generateDeny("setup",
					"only administrators are allowed to do that."));
		}
	}

	public static void resetup(MessageReceivedEvent event, ImprovedString message) {
		if (event.getAuthor().getPermissionsForGuild(event.getGuild())
				.contains(Permissions.ADMINISTRATOR)) {
			List<IChannel> channelMentions = event.getMessage().getChannelMentions();
			if (channelMentions.size() == 1) {
				IChannel channel = channelMentions.get(0);
				if (message.words.size() >= 3) {
					String roleName = message.getContent(2, " ");
					if (event.getGuild().getRolesByName(roleName).size() >= 1) {
						long channelId = channel.getLongID();
						String query = String.format("UPDATE servers "
								+ "SET server_id=%d, channel_id=%d, role_name='%s' ",
								event.getGuild().getLongID(),
								channelId,
								roleName.replace("'", "\\'"));
						Handler.executeUpdate(query);
						event.getChannel().sendMessage(
								Utils.generateSuccess(
										String.format("Server has been set up with "
												+ "channel <#%d> and Open Lobby Creator role "
												+ "**%s**", channelId, roleName))
						);
					} else {
						event.getChannel().sendMessage(Utils.generateWarning(
								"Role provided does not exist!"));
					}
				} else {
					event.getChannel().sendMessage(Utils.generateWarning("Not "
							+ "enough data provided."));
				}
			} else {
				event.getChannel().sendMessage(Utils.generateWarning(
						Utils.INVALID_FORMAT));
			}
		} else {
			event.getChannel().sendMessage(Utils.generateDeny("setup",
					"only administrators are allowed to do that."));
		}
	}

	public static void start(MessageReceivedEvent event, ImprovedString message,
			long channelId, boolean roleMissing, IRole olmrole) {
		try {
			if (!roleMissing) {
				if (event.getAuthor().getPermissionsForGuild(event.getGuild()).contains(Permissions.ADMINISTRATOR)
						|| event.getAuthor().getRolesForGuild(event.getGuild())
								.contains(olmrole)) {
					String query = String.format("SELECT id FROM lobbies WHERE server_id"
							+ "=%d", event.getGuild().getLongID());
					if (Handler.executeQuery(query).next()) {
						event.getChannel().sendMessage(Utils.generateDeny("start an open lobby",
								"there is one currently running."));
					} else {
						if (message.words.size() >= 3) {
							long authorId = event.getAuthor().getLongID();
							long serverId = event.getGuild().getLongID();
							String link = message.getWord(1);
							if (!link.startsWith("http://") && !link.startsWith("https://")) {
								link = "http://" + link;
							}
							String description = message.getContent(2, " ");
							if (link.length() > 100 || description.length() > 200) {
								event.getChannel().sendMessage(Utils.generateWarning("Link or description "
										+ "are too long."));
							} else {
								IChannel channel = MainBot.client.getChannelByID(channelId);
								if (channel != null) {

									String send = String.format(
											"__**@here New Open Lobby by <@%d>**__:\n\n"
											+ "%s\n\n"
											+ "Click the check-mark reaction or type `%sjoin` to join.\n"
											+ "**Tip:** If you don't get a Direct Message you either blocked the bot "
											+ "or disabled Direct Messages from server members.",
											event.getAuthor().getLongID(), description.replace("&sc",
											";"), Handler.PREFIX
									);
									IMessage target = channel.sendMessage(send);
									query = String.format("INSERT INTO lobbies(author_id, server_id,"
											+ "link, description, message_id) VALUES("
											+ "%d, %d, '%s', '%s', %d)", authorId, serverId,
											link.replace("'", "\\'"), description.replace("'", "\\'"), target.getLongID());
									Handler.executeUpdate(query);
									Thread.sleep(300);
									target.addReaction(Handler.REACTION);
									event.getChannel().sendMessage(Utils.generateSuccess(
											"Open lobby started."));
								} else {
									event.getChannel().sendMessage(Utils.generateWarning(
											"Open Lobby channel cannot be seen by the bot "
											+ "or was deleted."));
								}
							}
						} else {
							event.getChannel().sendMessage(Utils.generateWarning("Not enough data."));
						}
					}
				} else {
					event.getChannel().sendMessage(Utils.noOlm("start open lobbies."));
				}
			} else {
				event.getChannel().sendMessage(Utils.generateWarning("Open Lobby Maker role deleted "
						+ "or inaccessible."));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			event.getChannel().sendMessage(Utils.generateError());
		}
	}

	public static void join(ReactionAddEvent event) {
		try {
			System.out.println("Reached 6");
			String query = String.format("SELECT link, description, server_id, author_id "
					+ "FROM lobbies WHERE server_id=%d", event.getGuild().getLongID());
			ResultSet s = Handler.executeQuery(query);
			if (s.next()) {
				String link = s.getString(1);
				String description = s.getString(2);
				long serverId = s.getLong(3);
				long authorId = s.getLong(4);
				IGuild server = MainBot.client.getGuildByID(serverId);
				String serverd = "Unknown";
				if (server != null) {
					serverd = server.getName();
				}
				String send = String.format(
						"__**Open Lobby by <@%d>** in server **%s**__:\n\n"
						+ "%s\n\n"
						+ "Link: %s",
						authorId, serverd, description.replace("&sc",
								";"), link
				);
				event.getUser().getOrCreatePMChannel().sendMessage(send);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void join(MessageReceivedEvent event) {
		try {
			String query = String.format("SELECT link, description, server_id, author_id "
					+ "FROM lobbies WHERE server_id=%d", event.getGuild().getLongID());
			ResultSet s = Handler.executeQuery(query);
			if (s.next()) {
				String link = s.getString(1);
				String description = s.getString(2);
				long serverId = s.getLong(3);
				long authorId = s.getLong(4);
				IGuild server = MainBot.client.getGuildByID(serverId);
				String serverd = "Unknown";
				if (server != null) {
					serverd = server.getName();
				}
				String send = String.format(
						"__**Open Lobby by <@%d>** in server **%s**__:\n\n"
						+ "%s\n\n"
						+ "Link: %s",
						authorId, serverd, description.replace("&sc",
								";"), link
				);
				event.getAuthor().getOrCreatePMChannel().sendMessage(send);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void stop(MessageReceivedEvent event, boolean roleMissing, IRole olmrole) {
		try {
			String query = String.format("SELECT author_id FROM lobbies WHERE server_id=%d",
					event.getGuild().getLongID());
			ResultSet s = Handler.executeQuery(query);
			if (s.next()) {
				long authorId = s.getLong(1);
				if (event.getAuthor().getLongID() == authorId || event.getAuthor()
						.getPermissionsForGuild(event.getGuild()).contains(Permissions.ADMINISTRATOR)) {
					query = String.format("SELECT channel_id FROM servers WHERE server_id"
							+ "=%d", event.getGuild().getLongID());
					s = Handler.executeQuery(query);
					if (s.next()) {
						IChannel channel = MainBot.client.getChannelByID(s.getLong(1));
						if (channel != null) {
							query = String.format("SELECT message_id FROM lobbies WHERE "
									+ "server_id=%d", event.getGuild().getLongID());
							s = Handler.executeQuery(query);
							long messageId = 0;
							if (s.next()) {
								messageId = s.getLong(1);
							}
							IMessage target = MainBot.client.getMessageByID(messageId);
							if (target != null) {
								target.removeAllReactions();
							}
							query = String.format("DELETE FROM lobbies WHERE server_id"
									+ "=%d", event.getGuild().getLongID());
							Handler.executeUpdate(query);
							channel.sendMessage(Utils.LOBBY_OVER);
							event.getChannel().sendMessage(Utils.generateSuccess("Open lobby closed."));
						} else {
							event.getChannel().sendMessage(Utils.generateWarning("Open lobby channel cannot "
									+ "be found."));
						}
					} else {
						event.getChannel().sendMessage(Utils.generateWarning("Open lobby channel cannot "
								+ "be found."));
					}
				} else {
					event.getChannel().sendMessage(Utils.generateDeny("stop", "open lobby"
							+ " was not created by you, and you are not an administrator"));
				}
			} else {
				event.getChannel().sendMessage(Utils.generateDeny("stop", "there is no open "
						+ "lobby running."));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void help(MessageReceivedEvent event) {
		event.getChannel().sendMessage(Utils.HELP_MESSAGE);
	}

	public static void tos(MessageReceivedEvent event) {
		event.getChannel().sendMessage(Utils.TOS);
	}

	public static void test(MessageReceivedEvent event) {
		try {
			event.getAuthor().getOrCreatePMChannel().sendMessage(String.format("Hello there %s! Help test this bot (and other bots made by Pentox) by joining "
					+ "this server! <https://discord.gg/kxCaSfn>.", event.getAuthor().getName()));
			event.getChannel().sendMessage(Utils.generateSuccess("Invite sent in Private Message."));
		} catch (DiscordException ex) {
			event.getChannel().sendMessage(Utils.generateWarning("I can't DM you! You have "
					+ "apparently blocked me or disabled messages from server members..."));
		}
	}

	public static void blacklist(MessageReceivedEvent event, ImprovedString message) {
		try {
			if (MainBot.client.getApplicationOwner().getLongID()
					== event.getAuthor().getLongID()) {
				String operator = message.getWord(1);
				List<IUser> mentions = event.getMessage().getMentions();
				String query;
				if (operator.equals("add")) {
					if (mentions.size() >= 1) {
						for (IUser user : mentions) {
							long id = user.getLongID();
							query = String.format("SELECT user_id FROM blacklist WHERE "
									+ "user_id=%d", id);
							if (!Handler.executeQuery(query).next()) {
								query = String.format("INSERT INTO blacklist(user_id) "
										+ "VALUES(%d)", id);
								Handler.executeUpdate(query);
							}
						}
						event.getChannel().sendMessage(Utils.generateSuccess("User(s) "
								+ "has/have been **added** to the blacklist. Any users that "
								+ "were already in it were not added again."));
					} else {
						event.getChannel().sendMessage(Utils.generateDeny("add to blacklist",
								"there are no user mentioned."));
					}
				}
			  
				else if (operator.equals("show")){
						String finalMessage = "";
						query = "SELECT user_id FROM blacklist";
						ResultSet s = Handler.executeQuery(query);

						while (s.next()) {
							IUser user = MainBot.client.getUserByID(s.getLong(1));
							if (user != null) {
								finalMessage += (finalMessage.isEmpty() ? "" : "\n") + String.format(
										"- **%s#%s** (**%d**)",
										user.getName(), user.getDiscriminator(),
										user.getLongID());
							} else {
								finalMessage += (finalMessage.isEmpty() ? "" : "\n") + String.format("- [**absent user**] (**%d**)",
										s.getLong(1));
							}
						}
						finalMessage = finalMessage.trim().isEmpty() ? "*[**no users blacklisted!**]*" : finalMessage
								.trim();
						event.getChannel().sendMessage(Utils.generateSuccess("Blacklist:\n")
								+ finalMessage);
				}else if (operator.equals("remove")){
						if (mentions.size() >= 1) {
							for (IUser user : mentions) {
								long id = user.getLongID();
								query = String.format("SELECT user_id FROM blacklist WHERE "
										+ "user_id=%d", id);
								if (Handler.executeQuery(query).next()) {
									query = String.format("DELETE FROM blacklist WHERE"
											+ " user_id=%d", id);
									Handler.executeUpdate(query);
								}
							}
							event.getChannel().sendMessage(Utils.generateSuccess("User(s) "
									+ "has/have been **removed** from the blacklist."));
						} else {
							event.getChannel().sendMessage(Utils.generateDeny("add to blacklist",
									"there are no user mentioned."));
						}
				}else if (operator.equals("clear")) {
						query = "DELETE FROM blacklist";
						Handler.executeUpdate(query);
						event.getChannel().sendMessage(Utils.generateSuccess("Blacklist has been cleared."));
				} else {
					event.getChannel().sendMessage(Utils.INVALID_FORMAT);
				}
			} else {
				event.getChannel().sendMessage(Utils.generateDeny("edit/view blacklist", "only the application"
						+ " owner can do that."));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void addbot(MessageReceivedEvent event) {
		event.getChannel().sendMessage(Utils.ADDBOT);
	}
}

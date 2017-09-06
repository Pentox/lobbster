package olbot;

import java.sql.ResultSet;
import java.util.List;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionRemoveEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;

public class Functions { // for command actions

	/**
	 * Sets up a server.
	 *
	 * @param event The event detected by the EventSubscriber in Handler.java.
	 * @param message The ImprovedString object containing the message content.
	 */
	public static void setup(MessageReceivedEvent event, ImprovedString message) {
		if (event.getAuthor().getPermissionsForGuild(event.getGuild())
				.contains(Permissions.ADMINISTRATOR)) {
			List<IChannel> channelMentions = event.getMessage().getChannelMentions();
			if (channelMentions.size() == 1) {
				IChannel channel = channelMentions.get(0);
				if (message.words.size() >= 3) {
					String managerRole = message.getContent(2, " ").split(";")[0].trim();
					String participantRole = message.getContent(2, " ").split(";")[1].trim();
					String hostRole = message.getContent(2, " ").split(";")[2].trim();
					if (event.getGuild().getRolesByName(managerRole).size() >= 1
							&& event.getGuild().getRolesByName(participantRole).size() >= 1
							&& event.getGuild().getRolesByName(hostRole).size() >= 1) {
						long channelId = channel.getLongID();
						String query = String.format("INSERT INTO servers"
								+ "(server_id, channel_id, manager_role, participant_role, "
								+ "host_role, start_time, lobby_count) "
								+ "VALUES(%d, %d, '%s', '%s', '%s', %d, 0)",
								event.getGuild().getLongID(),
								channelId,
								managerRole.replace("'", "\\'"),
								participantRole.replace("'", "\\'"),
								hostRole.replace("'", "\\'"),
								System.currentTimeMillis() + 86400000);

						Handler.executeUpdate(query);
						event.getChannel().sendMessage(
								Utils.generateSuccess(
										String.format("Server has been set up with open lobby "
												+ "channel <#%d>, Open Lobby Maker role "
												+ "**%s**, Open Lobby Participant role **%s**, and Open Lobby "
												+ "Host role **%s**.",
												channelId, managerRole, participantRole, hostRole))
						);
					} else {
						event.getChannel().sendMessage(Utils.generateWarning(
								"Role(s) provided do/does not exist!"));
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

	/**
	 * Updates server configuration.
	 *
	 * @param event The event detected by the EventSubscriber in Handler.java.
	 * @param message The ImprovedString object containing the message content.
	 */
	public static void resetup(MessageReceivedEvent event, ImprovedString message) {
		if (event.getAuthor().getPermissionsForGuild(event.getGuild())
				.contains(Permissions.ADMINISTRATOR)) {
			List<IChannel> channelMentions = event.getMessage().getChannelMentions();
			if (channelMentions.size() == 1) {
				IChannel channel = channelMentions.get(0);
				if (message.words.size() >= 3) {
					String managerRole = message.getContent(2, " ").split(";")[0].trim();
					String participantRole = message.getContent(2, " ").split(";")[1].trim();
					String hostRole = message.getContent(2, " ").split(";")[2].trim();
					if (event.getGuild().getRolesByName(managerRole).size() >= 1
							&& event.getGuild().getRolesByName(participantRole).size() >= 1
							&& event.getGuild().getRolesByName(hostRole).size() >= 1) {
						long channelId = channel.getLongID();
						String query = String.format("UPDATE servers SET "
								+ "channel_id=%d, manager_role='%s', "
								+ "participant_role='%s', "
								+ "host_role='%s' WHERE server_id=%d",
								channelId,
								managerRole.replace("'", "\\'"),
								participantRole.replace("'", "\\'"),
								hostRole.replace("'", "\\'"),
								event.getGuild().getLongID());

						Handler.executeUpdate(query);
						event.getChannel().sendMessage(
								Utils.generateSuccess(
										String.format("Server settings have been update to open lobby "
												+ "channel <#%d>, Open Lobby Maker role "
												+ "**%s**, Open Lobby Participant role **%s**, and Open Lobby "
												+ "Host role **%s**.",
												channelId, managerRole, participantRole, hostRole))
						);
					} else {
						event.getChannel().sendMessage(Utils.generateWarning(
								"Role(s) provided do/does not exist!"));
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

	/**
	 * Starts an open lobby.
	 *
	 * @param event The event detected by the EventSubscriber in Handler.java.
	 * @param message The ImprovedString object containing the message content.
	 * @param channelId The channel ID of the OL channel.
	 * @param roleMissing If the OLM role is missing.
	 * @param olmrole The OLM role.
	 * @param hostRole The OL host role.
	 */
	public static void start(MessageReceivedEvent event, ImprovedString message,
			long channelId, boolean roleMissing, IRole olmrole, IRole hostRole) {
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
						query = String.format("SELECT lobby_count FROM servers WHERE "
								+ "server_id=%d", event.getGuild().getLongID());
						ResultSet s = Handler.executeQuery(query);
						boolean canCreate = true;
						if (s.next()) {
							if (s.getInt(1) >= 2) {
								canCreate = false;
							}
						}
						if (canCreate) {
							if (message.words.size() >= 3) {
								long authorId = event.getAuthor().getLongID();
								long serverId = event.getGuild().getLongID();
								String link = message.getWord(1);
								if (!link.startsWith("http://") && !link.startsWith("https://")) {
									link = "http://" + link.trim();
								}
								String description = message.getContent(2, " ");
								if (link.length() > 100 || description.length() > 200) {
									event.getChannel().sendMessage(Utils.generateWarning("Link or description "
											+ "are too long."));
								} else if (link.length() < 5 || description.length() > 2) {
									event.getChannel().sendMessage(Utils.generateWarning("Link or description "
											+ "are too short."));
								} else {
									if (link.matches("(https?://)?(www\\.)?.{3,100}\\.+.{2,100}")) {
										IChannel channel = MainBot.client.getChannelByID(channelId);
										if (channel != null) {
											event.getMessage().delete();
											String send = String.format(
													"__**@here New Open Lobby by <@%d>**__:\n\n"
													+ "%s\n\n"
													+ "Click the check-mark reaction or type `%sjoin` to join. "
													+ "Uncheck the reaction or type `%sleave` to leave.\n"
													+ "**Tip:** If you don't get a Direct Message you either blocked the bot "
													+ "or disabled Direct Messages from server members.",
													event.getAuthor().getLongID(), description.replace("&sc",
													";"), Handler.PREFIX, Handler.PREFIX
											);
											IMessage target = channel.sendMessage(send);
											query = String.format("INSERT INTO lobbies(author_id, server_id,"
													+ "link, description, message_id) VALUES("
													+ "%d, %d, '%s', '%s', %d)", authorId, serverId,
													link.replace("'", "\\'"), description.replace("'", "\\'"), target.getLongID());
											Handler.executeUpdate(query);
											query = String.format("UPDATE servers SET lobby_count=lobby_count+1 WHERE "
													+ "server_id=%d", event.getGuild().getLongID());
											Handler.executeUpdate(query);
											Thread.sleep(300);
											target.addReaction(Handler.REACTION);
											Thread.sleep(300);
											event.getAuthor().addRole(hostRole);
											event.getChannel().sendMessage(Utils.generateSuccess(
													"Open lobby started."));
										} else {
											event.getChannel().sendMessage(Utils.generateWarning(
													"Open Lobby channel cannot be seen by the bot "
													+ "or was deleted."));
										}
									} else {
										event.getChannel().sendMessage(Utils.generateWarning("Inavlid invite link."));
									}
								}
							} else {
								event.getChannel().sendMessage(Utils.generateWarning("Not enough data."));
							}
						} else {
							event.getChannel().sendMessage(Utils.generateDeny(
									"create open lobby", "the daily limit is reached."));
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

	/**
	 * Joins you to an open lobby (Detected using reaction).
	 *
	 * @param event The event detected by the EventSubscriber in Handler.java.
	 * @param participantRole The Open Lobby Participant role to use.
	 */
	public static void join(ReactionAddEvent event, IRole participantRole) {
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
				event.getUser().addRole(participantRole);
				event.getUser().getOrCreatePMChannel().sendMessage(send);
			} else {
				event.getChannel().sendMessage(Utils.generateDeny("enter", "there is no open lobby "
						+ "running on this server."));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Sends joins you to an open lobby.
	 *
	 * @param event The event detected by the EventSubscriber in Handler.java.
	 * @param participantRole The Open Lobby Participant role to use.
	 */
	public static void join(MessageReceivedEvent event, IRole participantRole) {
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
				if (participantRole != null) {
					event.getAuthor().addRole(participantRole);
				}
				Thread.sleep(200);
				event.getChannel().sendMessage(Utils.generateSuccess("Invite sent in DM."));
			} else {
				event.getChannel().sendMessage(Utils.generateDeny("enter", "there is no open lobby "
						+ "running on this server."));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Removes the Open Lobby Participant role.
	 *
	 * @param event The event detected by the EventSubscriber in Handler.java.
	 * @param participantRole The participant role.
	 * @param hostRole The OL host role.
	 */
	public static void leave(ReactionRemoveEvent event, IRole participantRole, IRole hostRole) {
		try {
			String query = String.format("SELECT id "
					+ "FROM lobbies WHERE server_id=%d LIMIT 1", event.getGuild().getLongID());
			ResultSet s = Handler.executeQuery(query);
			if (s.next()) {
				if (participantRole != null
						&& event.getUser().getRolesForGuild(event.getGuild())
								.contains(participantRole)
						&& !event.getUser().getRolesForGuild(event.getGuild())
								.contains(hostRole)) {
					event.getUser().removeRole(participantRole);
					try {
						event.getUser().getOrCreatePMChannel().sendMessage("You have left the "
								+ "open lobby!");
					} catch (DiscordException ex) {
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Removes the Open Lobby Participant role.
	 *
	 * @param event The event detected by the EventSubscriber in Handler.java.
	 * @param participantRole The participant role.
	 * @param hostRole The OL host role.
	 */
	public static void leave(MessageReceivedEvent event, IRole participantRole, IRole hostRole) {
		try {
			String query = String.format("SELECT id "
					+ "FROM lobbies WHERE server_id=%d LIMIT 1", event.getGuild().getLongID());
			ResultSet s = Handler.executeQuery(query);
			if (s.next()) {
				if (participantRole != null
						&& event.getAuthor().getRolesForGuild(event.getGuild())
								.contains(participantRole)
						&& !event.getAuthor().getRolesForGuild(event.getGuild())
								.contains(hostRole)) {
					event.getAuthor().removeRole(participantRole);
					event.getChannel().sendMessage(Utils.generateSuccess("You have left the open "
							+ "lobby."));
				} else if (!event.getAuthor().getRolesForGuild(event.getGuild())
						.contains(participantRole)) {
					event.getChannel().sendMessage(Utils.generateDeny("leave", "you are not a member "
							+ "in the open lobby."));
				} else if (event.getAuthor().getRolesForGuild(event.getGuild())
						.contains(hostRole)) {
					event.getChannel().sendMessage(Utils.generateDeny("leave", "you are the host of this "
							+ "open lobby!"));
				}
			} else {
				event.getChannel().sendMessage(Utils.generateDeny("leave", "there is no open lobby "
						+ "running on this server."));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Stops open lobby.
	 *
	 * @param event The event detected by the EventSubscriber in Handler.java.
	 * @param roleMissing If the OLM role is missing.
	 * @param olmrole The OLM role.
	 * @param prole The Open Lobby Participant role.
	 * @param hostRole the OL host role.
	 */
	public static void stop(MessageReceivedEvent event, boolean roleMissing, IRole olmrole, IRole prole,
			IRole hostRole) {
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
							Thread.sleep(300);
							event.getAuthor().removeRole(hostRole);
							for (IUser user : event.getGuild().getUsersByRole(prole)) {
								Thread.sleep(200);
								user.removeRole(prole);
							}
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

	/**
	 * Sends the help message.
	 *
	 * @param event The event detected by the EventSubscriber in Handler.java.
	 */
	public static void help(MessageReceivedEvent event) {
		try {
			event.getAuthor().getOrCreatePMChannel().sendMessage(Utils.HELP_MESSAGE);
			event.getChannel().sendMessage(Utils.generateSuccess("Help sent in DM."));
		} catch (DiscordException ex) {
			event.getChannel().sendMessage(Utils.generateWarning("I can't DM you! It looks like "
					+ "you've blocked me or disabled messages from server members..."));
		}
	}

	/**
	 * Sends the Terms of Service message.
	 *
	 * @param event The event detected by the EventSubscriber in Handler.java.
	 */
	public static void tos(MessageReceivedEvent event) {
		try {
			event.getAuthor().getOrCreatePMChannel().sendMessage(Utils.TOS);
			event.getChannel().sendMessage(Utils.generateSuccess("Details sent in DM."));
		} catch (DiscordException ex) {
			event.getChannel().sendMessage(Utils.generateWarning("I can't DM you! It looks like "
					+ "you've blocked me or disabled messages from server members..."));
		}
	}

	/**
	 * Sends the testing server invite.
	 *
	 * @param event The event detected by the EventSubscriber in Handler.java.
	 */
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

	/**
	 * Performs operations on blacklist.
	 *
	 * @param event The event detected by the EventSubscriber in Handler.java.
	 * @param message The ImprovedString object containing the message content.
	 */
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
				} else if (operator.equals("show")) {
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
				} else if (operator.equals("remove")) {
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
				} else if (operator.equals("clear")) {
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

	/**
	 * Sends help for adding bot to a server.
	 *
	 * @param event The event detected by the EventSubscriber in Handler.java.
	 */
	public static void addbot(MessageReceivedEvent event) {
		try {
			event.getAuthor().getOrCreatePMChannel().sendMessage(Utils.ADDBOT);
			event.getChannel().sendMessage(Utils.generateSuccess("Details sent in DM."));
		} catch (DiscordException ex) {
			event.getChannel().sendMessage(Utils.generateWarning("I can't DM you! It looks like "
					+ "you've blocked me or disabled messages from server members..."));
		}
	}

	/**
	 * Update the lobby limits
	 */
	public static synchronized void update() {
		String query = String.format("UPDATE servers SET lobby_count=0, start_time=%d WHERE "
				+ "start_time <= %d", System.currentTimeMillis() + 86400000, System.currentTimeMillis());
		Handler.executeUpdate(query);
	}
}

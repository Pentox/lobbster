package olbot;

import java.sql.ResultSet;
import java.sql.Statement;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

public class Handler extends MainBot {

	public static final String PREFIX = "<"; // command prefix
	public static final String REACTION = ":ballot_box_with_check:";

	@EventSubscriber
	public void onReady(ReadyEvent event) {
		System.out.println("Ready!");
		
		MainBot.client.changePlayingText(PREFIX + "help");
	}

	@EventSubscriber
	public void messageReceived(MessageReceivedEvent event) {
		try {
			String query = "SELECT 1";
			try {
				executeQuery(query);
			} catch (Exception ex) {
				try {
					MainBot.connection = null;
					System.gc();
					MainBot.connect();
				} catch (Exception ex2) {
					ex2.printStackTrace();
				}
			}
			ImprovedString message = new ImprovedString(event.getMessage().getContent());
			String command = message.getWord(0);
			query = String.format("SELECT channel_id, role_name "
					+ "FROM servers WHERE server_id=%d",
					event.getGuild().getLongID());
			ResultSet s = executeQuery(query);
			if (s.next()) {
				long channelId = s.getLong(1);
				String roleName = s.getString(2);
				boolean roleMissing = false;
				IRole role = null;
				try {
					role = event.getGuild().getRolesByName(roleName).get(0);
				} catch (IndexOutOfBoundsException ex) {
					roleMissing = true;
				}
				if (command.equals(PREFIX + "start") && !isBlacklisted(event.getAuthor())
						&& !event.getAuthor().isBot()) {
					Functions.start(event, message, channelId, roleMissing, role);
				}
				else if (command.equals(PREFIX + "join") && !isBlacklisted(event.getAuthor())
						&& !event.getAuthor().isBot()) {
					Functions.join(event);
				}
				else if (command.equals(PREFIX + "stop") && !isBlacklisted(event.getAuthor())
						&& !event.getAuthor().isBot()) {
					Functions.stop(event, roleMissing, role);
				}
				else if (command.equals(PREFIX + "setup") && !isBlacklisted(event.getAuthor())
					&& !event.getAuthor().isBot()) {
					Functions.resetup(event, message);
				}
				else if (command.equals(PREFIX + "help") && !isBlacklisted(event.getAuthor())
					&& !event.getAuthor().isBot()) {
					Functions.help(event);
				}
				else if (command.equals(PREFIX + "tos") && !isBlacklisted(event.getAuthor())
					&& !event.getAuthor().isBot()) {
					Functions.tos(event);
				}
				else if (command.equals(PREFIX + "test") && !isBlacklisted(event.getAuthor())
					&& !event.getAuthor().isBot()) {
					Functions.test(event);
				}
				else if (command.equals(PREFIX + "blacklist") && !isBlacklisted(event.getAuthor())
					&& !event.getAuthor().isBot()) {
					Functions.blacklist(event, message);
				}
				else if (command.equals(PREFIX + "addbot") && !isBlacklisted(event.getAuthor())
					&& !event.getAuthor().isBot()) {
					Functions.addbot(event);
				}

			} else {
				if (command.equals(PREFIX + "setup")
						&& !event.getAuthor().isBot()) {
					Functions.setup(event, message);
				}
				else if (command.equals(PREFIX + "help") && !isBlacklisted(event.getAuthor())
					&& !event.getAuthor().isBot()) {
					Functions.help(event);
				}
				else if (command.equals(PREFIX + "tos") && !isBlacklisted(event.getAuthor())
					&& !event.getAuthor().isBot()) {
					Functions.tos(event);
				}
				else if (command.equals(PREFIX + "test") && !isBlacklisted(event.getAuthor())
					&& !event.getAuthor().isBot()) {
					Functions.test(event);
				}
				else if (command.equals(PREFIX + "blacklist") && !isBlacklisted(event.getAuthor())
					&& !event.getAuthor().isBot()) {
					Functions.blacklist(event, message);
				}
				else if (command.equals(PREFIX + "addbot") && !isBlacklisted(event.getAuthor())
					&& !event.getAuthor().isBot()) {
					Functions.addbot(event);
				}
				else if (command.equals(PREFIX + "start")
						|| command.equals(PREFIX + "stop")
						|| command.equals(PREFIX + "join")
						) {
					event.getChannel().sendMessage(Utils.generateWarning(String.format("Those commands are only"
							+ " available for registered servers. If you are an admin, you can "
							+ "do that by executing `%ssetup` explained more in depth using "
							+ "`%shelp`.", PREFIX, PREFIX)));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.gc();
		}
	}

	@EventSubscriber
	public void reactionAdded(ReactionAddEvent event) {
		System.out.println("Reached 1");
		try {
			String query = String.format("SELECT message_id FROM lobbies WHERE server_id"
					+ "=%d", event.getGuild().getLongID());
			ResultSet s = executeQuery(query);
			if (s.next()) {
				System.out.println("Reached 2");
				long messageId = s.getLong(1);
				if (event.getMessage().getLongID() == messageId && !event.getUser().isBot()) {
					System.out.println("Reached 3");
					if (!isBlacklisted(event.getUser())) {
						System.out.println("Reached 4");
						if (event.getReaction().getUnicodeEmoji().getAliases()
								.contains(REACTION.replace(":", ""))) {
							Functions.join(event);
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// methods for easy use
	public static ResultSet executeQuery(String query) {
		try {
			Statement statement = MainBot.connection.createStatement();
			return statement.executeQuery(query);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static int executeUpdate(String query) {
		try {
			Statement statement = MainBot.connection.createStatement();
			return statement.executeUpdate(query);
		} catch (Exception ex) {
			ex.printStackTrace();
			return 0;
		}
	}

	public static boolean execute(String query) {
		try {
			Statement statement = MainBot.connection.createStatement();
			return statement.execute(query);
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public static boolean isBlacklisted(IUser user) {
		try {
			String query = String.format("SELECT id FROM blacklist WHERE user_id=%d",
					user.getLongID());
			return executeQuery(query).next();
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
}

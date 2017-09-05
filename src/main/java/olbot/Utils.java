package olbot;

public class Utils {
	
	public static final String INVALID_FORMAT = ":no_entry_sign: Invalid format! Try `"
			+ Handler.PREFIX + "help` for help.";
	public static final String LOBBY_OVER = "@here Open lobby over!";
	public static final String HELP_MESSAGE = String.format(
		":information_source: **__Opaloby__**:\nBleep bloop! I am a bot. Pentox#6935 created "
				+ "me to automate open lobbies. To add me to your server, execute `%saddbot`. "
				+ "Here are my commands!\n`%ssetup [open lobby channel mention] [open lobby "
				+ "maker role name];[open lobby participant role name]`: Sets up this server with "
				+ "the specified arguments. You "
				+ "must be an administrator to execute this command. Note that if you execute "
				+ "this command again after you've already set up the server, the bot will "
				+ "update settings for you.\n`%sstart [link] [description]`: Starts an open "
				+ "lobby. You must have the role the server administrators chose when setting "
				+ "up this bot (previous command).\n`%sjoin`: If there's an open lobby running,"
				+ " the bot will send you a private message with information on how to join the "
				+ "open lobby.\n`%sleave`: If you are participating in an open lobby, you will leave it."
				+ "\n`%sstop`: This only works if you are the person who created the"
				+ " open lobby and you have the open lobby maker role. Otherwise, you must be an"
				+ " administrator. This command stops the current open lobby, meaning that no "
				+ "more people can join.\n`%shelp`: Send help."
				+ "\n`%stos`: Shows the terms of service for this bot.\n\n"
				+ "Source code of the bot: <https://github.com/Pentox/opaloby/>.",
				Handler.PREFIX, Handler.PREFIX, Handler.PREFIX, Handler.PREFIX, Handler.PREFIX,
				Handler.PREFIX, Handler.PREFIX, Handler.PREFIX
		);
	
	public static final String TOS = ":information_source: **__Opaloby Terms of Service__**:\nBy"
			+ " using this bot, you agree to the following terms of service:\n**1.** "
			+ "You acknowledge that this bot and all of its components, excluding the official"
			+ " Discord API, are not created, endorsed, sponsored, owned, developed, "
			+ "maintained, hosted or associated by/with Discord.\n**2.** You have read and "
			+ "agreed to Discord's official Terms of Service, found on "
			+ "<https://discordapp.com/terms>.\n**3.** You acknowledge that user data will be "
			+ "stored upon the execution of some commands, but will be deleted directly after "
			+ "the end of an open lobby. If you want to get your data removed, simple close any "
			+ "open lobbies you are maintaining and all data except blacklists will be "
			+ "erased.\n**4.** You may not abuse this bot by modifying the way it behaves or "
			+ "overusing its functions repetitively and unnecessarily.\n**5.** THIS BOT COMES "
			+ "WITH NO WARRANTY OF ANY KIND; THE DEVELOPER IS NOT HELD RESPONSIBLE FOR DAMAGE "
			+ "CAUSED BY THE BOT.\nBreaking these terms will result to your account being blacklisted "
			+ "from using any commands except this one.";
	
	public static final String ADDBOT = 
			String.format(":information_source: Want to add this bot to your server? "
			+ "Here's a small guide!\n\nFirst you should create an **Open Lobby Maker** role if "
			+ "you don't have one. You can name it whatever you want BUT IT SHOULD NOT "
			+ "CONTAIN SEMI-COLONS.\nSecond, create an **Open Lobby Participant** role if "
			+ "you don't have one. Again, you can name it whatever you want BUT IT SHOULD NOT "
			+ "CONTAIN SEMI-COLONS. Place BOTH of the roles UNDER the bot role (\"Opaloby\"). "
			+ "Last, you will need to create an **open lobby channel**, if you haven't already. "
			+ "That's the channel where"
			+ " the bot will announce open lobbies and send information about them.\n"
			+ "Now, use this link: "
			+ "<https://discordapp.com/oauth2/authorize?client_id=354264151520051201&scope="
			+ "bot&permissions=268577856> to add the bot to your server.\n"
			+ "Once the bot's in your server, there is one more step.\nExecute the command "
			+ "`%ssetup [1] [2];[3]`. Mention the channel you created in place of **[1]** and "
			+ "replace **[2]** with the name of the manager role you created, and replace "
			+ "**[3]** with the participant role you created. Please note the seperating semi-colon.\n"
			+ "You're now set! Give the manager role to people who you want to be able to make open "
			+ "lobbies. Enjoy!\n\nSource code: <https://github.com/Pentox/opaloby/>."
			+ "\n**Tip:** Use `%shelp` for more commands!", Handler.PREFIX, 
			Handler.PREFIX);
	
	public static String generateWarning (String message) {
		return String.format(":warning: %s", message);
	}
	
	public static String generateDeny (String action, String reason) {
		return String.format(":no_entry_sign: You cannot %s because %s", action, reason);
	}
	
	public static String generateError (String action) {
		return String.format(":x: An error occured while %s", action);
	}
	
	public static String generateError () {
		return ":x: An error occured.";
	}
	
	public static String noOlm (String action) {
		return String.format(":warning: Only Open Lobby Makers and administrators can %s", action);
	}
	
	public static String generateSuccess (String message) {
		return String.format(":ballot_box_with_check: %s", message);
	}
}

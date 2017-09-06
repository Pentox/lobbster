# Lobbster
## About
Lobbster is a Java Discord bot to automate Open Lobbies for game parties, created by Pentox#6935. Data is kept in a MySQL database.

## How to use
This bot is already hosted by Pentox, so you don't have to compile it yourself to use it. Go to a server where the bot is available, and execute `<addbot`.
If you want to _use the source code_, this repository is for you. But unless you're using my pom.xml file, it's not as easy as **copy and pasting the code into notepad**.
First, you must get [Discord4J](https://github.com/austinv11/Discord4J) and [mysql-connector-java](https://dev.mysql.com/downloads/connector/j/5.1.html) and attach them to your Eclipse/NetBeans/IntelliJ/Maven/whatever project. I am not going to go into detail, but you must do that _first_. After you're done with that, you can copy the source code, edit it, and host your own version.

**VERY IMPORTANT NOTE**: This project uses Environment Variables and retrieves them using `System.getenv("KEY")`. If you're hosting a local version, make sure to include those in your project settings/arguments when running. Search online if you don't know how. The variables are:

`TOKEN`: Your Discord application token.

`DATABASE_ADDRESS` The MySQL database address.

`USERNAME`: The MySQL username.

`PASSWORD`: The MySQL user password.

## pom.xml
If you're confused why this project has a `pom.xml` file in it, it's the maven POM file. If you're using Maven to build your project, you can copy my own pom and you will not have to manually find and attach Discord4J and mysql-connector.

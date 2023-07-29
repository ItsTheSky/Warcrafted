package net.itsthesky.warcrafted.internal.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import net.itsthesky.warcrafted.Warcrafted;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class AdminCommands {

	public static void loadCommands(Warcrafted warcrafted) {

		final CommandAPICommand root = new CommandAPICommand("warcraftedadmin")
				.withAliases("wca", "wcadmin")
				.withPermission("warcrafted.admin");

		root.executes((sender, args) -> {
			sendHelpMessage(root, sender);
		});
		root.withSubcommand(new CommandAPICommand("help")
				.executes((sender, args) -> {
					sendHelpMessage(root, sender);
				}));

		root.withSubcommand(new CommandAPICommand("reload")
				.executes((sender, args) -> {
					// TODO: 28/07/2023 Reload the plugin
				}));

		root.withSubcommand(new CommandAPICommand("language")
				.executes((sender, args) -> {
					sendHelpMessage(getCommand(root, "language"), sender);
				})
				.withSubcommand(new CommandAPICommand("list")
						.executes((sender, args) -> {
							final List<String> availableLanguages = warcrafted.getAvailableLanguagesCode();

							if (availableLanguages.isEmpty())
								throw new IllegalStateException("No language available!");

							warcrafted.getLanguage().sendKey(sender, "commands.admin.language.list.head", availableLanguages.size());
							for (String language : availableLanguages) {
								final String selected = warcrafted.getLanguage().getCode().equals(language) ? "selected" : "unselected";
								warcrafted.getLanguage().sendKey(sender, "commands.admin.language.list.item." + selected, language);
							}
						}))
				.withSubcommand(new CommandAPICommand("set")
						.withArguments(new MultiLiteralArgument("code", warcrafted.getAvailableLanguagesCode()))
						.executes((sender, args) -> {
							final String code = (String) args.get("code");
							final boolean success = warcrafted.selectLanguage(code);

							if (!success) {
								warcrafted.getLanguage().sendKey(sender, "commands.admin.language.set.error");
								return;
							}

							warcrafted.getLanguage().sendKey(sender, "commands.admin.language.set.success", code);
						})));

		root.register();
	}

	public static void sendHelpMessage(CommandAPICommand root, CommandSender sender) {
		Warcrafted.getInstance().getLanguage().sendKey(sender, "commands.admin.help.head", root.getName());

		final boolean isRootCommand = root.getName().equals("warcraftedadmin");
		for (CommandAPICommand commandAPICommand : root.getSubcommands()) {
			final String name = commandAPICommand.getName();
			final String arguments = String.join(" ", commandAPICommand.getArguments().stream().map(argument -> "<" + argument.getNodeName() + ">").toArray(String[]::new));
			final String fullCommand = (isRootCommand ? "" : root.getName() + " ") + name + " " + arguments;

			final String description = Warcrafted.getInstance().getLanguage().rawKey("commands.admin." + (!isRootCommand ? root.getName() + "." : "") + name);

			Warcrafted.getInstance().getLanguage().sendKey(sender, "commands.admin.help.item", fullCommand, description);
		}
	}

	public static @NotNull CommandAPICommand getCommand(CommandAPICommand root, String name) {
		for (CommandAPICommand commandAPICommand : root.getSubcommands())
			if (commandAPICommand.getName().equalsIgnoreCase(name))
				return commandAPICommand;
		throw new IllegalArgumentException("Cannot find command with name " + name);
	}

}

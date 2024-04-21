package net.itsthesky.warcrafted.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.*;
import net.itsthesky.warcrafted.Warcrafted;
import net.itsthesky.warcrafted.core.game.GameSaveManager;
import net.itsthesky.warcrafted.core.game.GamesManager;
import net.itsthesky.warcrafted.core.game.core.Difficulty;
import net.itsthesky.warcrafted.core.game.core.Game;
import net.itsthesky.warcrafted.core.game.core.GamePlayer;
import net.itsthesky.warcrafted.core.game.meta.entities.Race;
import net.itsthesky.warcrafted.core.game.meta.entities.Resource;
import net.itsthesky.warcrafted.core.game.meta.entities.troops.Troop;
import net.itsthesky.warcrafted.core.game.meta.entities.buildings.Building;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
					warcrafted.getLanguage().sendKey(sender, "commands.admin.reload.reloading");
					warcrafted.reload();
					warcrafted.getLanguage().sendKey(sender, "commands.admin.reload.success");
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

		root.withSubcommand(new CommandAPICommand("game")
				.executes((sender, args) -> {
					sendHelpMessage(getCommand(root, "game"), sender);
				})
				.withSubcommand(new CommandAPICommand("set_resource")
						.withArguments(new StringArgument("game"),
								new PlayerArgument("player"),
								new MultiLiteralArgument("resource", warcrafted.getResourceManager().getEntityIds()),
								new IntegerArgument("amount"))
						.executesPlayer((player, args) -> {
							final String gameId = (String) args.get("game");
							final Resource resource = warcrafted.getResourceManager().getEntityById((String) args.get("resource"));
							final GamePlayer gamePlayer = GamesManager.getPlayer((Player) args.get("player"));
							final int value = (int) args.get("amount");

							final Game game = GamesManager.getGameById(gameId);
							if (game == null) {
								warcrafted.getLanguage().sendKey(player, "commands.admin.game_not_found", String.join(", ", GamesManager.getGameIds()));
								return;
							}

							if (!gamePlayer.getGame().equals(game)) {
								warcrafted.getLanguage().sendKey(player, "commands.admin.player_not_in_game", gamePlayer.getPlayer().getName(), game.getId());
								return;
							}

							gamePlayer.setResource(resource, Math.max(0, value));
							warcrafted.getLanguage().sendKey(player, "commands.admin.game.set_resource.success", resource.getName(), player.getName(), value, game.getId());
						}))
				.withSubcommand(new CommandAPICommand("add_building")
						.withArguments(new StringArgument("game"),
								new PlayerArgument("player"),
								new MultiLiteralArgument("building", warcrafted.getBuildingManager().getEntityIds()),
								new BooleanArgument("instant"))
						.executesPlayer((player, args) -> {
							final String gameId = (String) args.get("game");
							final Building building = warcrafted.getBuildingManager().getEntityById((String) args.get("resource"));
							final GamePlayer gamePlayer = GamesManager.getPlayer((Player) args.get("player"));
							final boolean instant = (boolean) args.get("instant");

							final Game game = GamesManager.getGameById(gameId);
							if (game == null) {
								warcrafted.getLanguage().sendKey(player, "commands.admin.game_not_found", String.join(", ", GamesManager.getGameIds()));
								return;
							}

							if (!gamePlayer.getGame().equals(game)) {
								warcrafted.getLanguage().sendKey(player, "commands.admin.player_not_in_game", gamePlayer.getPlayer().getName(), game.getId());
								return;
							}

							gamePlayer.startBuilding(building, player.getLocation(), instant);
							warcrafted.getLanguage().sendKey(player, "commands.admin.game.add_building.success", building.getName(), player.getName(), game.getId());
						})));

		root.withSubcommand(new CommandAPICommand("debug")
				.withSubcommand(new CommandAPICommand("buildings")
						.executes((sender, args) -> {
							sender.sendMessage("§9Loaded buildings (" + warcrafted.getBuildingManager().getEntities().size() + "):");
							for (Building building : warcrafted.getBuildingManager().getEntities()) {
								sender.sendMessage("§n - " + building.getId());
								sender.sendMessage("    §6- Build Time: §e" + building.getBuildTime());
								final StringBuilder formattedCost = new StringBuilder();
								for (Resource resource : building.getCost().keySet())
									formattedCost.append(resource.getId()).append(": ").append(building.getCost().get(resource)).append(" ");
								sender.sendMessage("    §6- Build Cost: §e" + formattedCost);
								sender.sendMessage("    §6- Is an upgrade?: §e" + building.isUpgrade());
								sender.sendMessage("    §6- Upgrade to: §e" + building.getUpgradeTo());
								sender.sendMessage("    §6- Requirements: §e" + building.getRequirements());
							}
						})
				)
				.withSubcommand(new CommandAPICommand("samplegame")
						.withArguments(new StringArgument("id"))
						.executesPlayer((player, args) -> {
							final String id = (String) args.get("id");

							final Game game = new Game(id, warcrafted);
							final GamePlayer gamePlayer = new GamePlayer(game, player,
									warcrafted.getRaceManager().getEntityById("undead"),
									Difficulty.EASY, null);
							game.addPlayer(gamePlayer);
						}))
				.withSubcommand(new CommandAPICommand("testholo")
						.executesPlayer((player, args) -> {

						}))
				.withSubcommand(new CommandAPICommand("savedb")
						.executesPlayer((player, args) -> {
							warcrafted.getGameSaveManager().saveGames();
						}))
				.withSubcommand(new CommandAPICommand("loaddb")
						.executesPlayer((player, args) -> {
							warcrafted.getGameSaveManager().loadGames();
						}))
				.withSubcommand(new CommandAPICommand("resetdb")
						.executesPlayer((player, args) -> {
							warcrafted.getGameSaveManager().reset();
						}))
				.withSubcommand(new CommandAPICommand("races")
						.executes(((sender, args) -> {
							sender.sendMessage("§9Loaded races (" + warcrafted.getRaceManager().getEntities().size() + "):");
							for (Race race : warcrafted.getRaceManager().getEntities()) {
								sender.sendMessage("§n - " + race.getId());
								sender.sendMessage("    §6- Starting Building: §e" + race.getStartingBuilding());
								sender.sendMessage("    §6- Starting troops:");
								for (Troop troop : race.getStartingTroops())
									sender.sendMessage("        §e- " + troop.getId() + ": " + race.getStartingTroopLevel(troop));
							}
						})))
				.executes((sender, args) -> {
					sendHelpMessage(getCommand(root, "debug"), sender);
				}));

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

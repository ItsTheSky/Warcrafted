package net.itsthesky.warcrafted.core.game.core;

import lombok.Getter;
import net.itsthesky.warcrafted.Warcrafted;
import net.itsthesky.warcrafted.core.game.GamesManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represent an active game of Warcrafted.
 */
@Getter
public final class Game {

	private final String id;
	private final Map<UUID, GamePlayer> players;
	private final Warcrafted warcrafted;

	private final BukkitTask updateTask;

	public Game(@NotNull String id, Warcrafted warcrafted) {
		this.id = id;
		this.warcrafted = warcrafted;
		GamesManager.addGame(this);

		// Players
		this.players = new HashMap<>();

		// Update task
		updateTask = Bukkit.getScheduler().runTaskTimer(warcrafted, update(), 0, 1);
	}

	// Methods

	public void addPlayer(GamePlayer player) {
		players.put(player.getPlayer().getUniqueId(), player);
	}

	public TimerTask update() {
		return new TimerTask() {
			@Override
			public void run() {

				for (GamePlayer player : players.values())
					player.update();

			}
		};
	}

	public void endGame() {
		updateTask.cancel();
		GamesManager.removeGame(this);
	}

	public String nextAvailableTeamColor() {
		final String[] colors = GameTeam.COLORS;
		int index = 0;
		for (GameTeam team : getTeams()) {
			if (team.getColor().equalsIgnoreCase(colors[index]))
				index++;
		}

		return colors[index];
	}

	// Getters

	public List<GameBuilding> getBuildings() {
		return getPlayers().stream().map(GamePlayer::getBuildings).flatMap(List::stream).toList();
	}

	public List<GamePlayer> getPlayers() {
		return List.copyOf(players.values());
	}

	public List<GameTeam> getTeams() {
		return getPlayers().stream().map(GamePlayer::getTeam).toList();
	}

	public @Nullable GamePlayer getPlayer(UUID uuid) {
		return players.get(uuid);
	}

	public @Nullable GamePlayer getPlayer(OfflinePlayer player) {
		return getPlayer(player.getUniqueId());
	}

	public List<GameTroop> getTroops() {
		return getPlayers().stream().map(GamePlayer::getTroops).flatMap(List::stream).toList();
	}

	public void clear() {
		for (GamePlayer player : getPlayers())
			player.clear();
		players.clear();
	}
}

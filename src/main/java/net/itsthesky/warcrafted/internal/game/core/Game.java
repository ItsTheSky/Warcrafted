package net.itsthesky.warcrafted.internal.game.core;

import net.itsthesky.warcrafted.api.game.GameBuilding;
import net.itsthesky.warcrafted.api.game.GamePlayer;
import org.bukkit.World;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represent an active game of Warcrafted.
 */
public final class Game {

	private final Map<UUID, GamePlayer> players;
	private final World world;

	public Game(Map<UUID, GamePlayer> players, World world) {
		this.players = players;
		this.world = world;
	}

	// Getters

	public Map<UUID, GamePlayer> getPlayers() {
		return players;
	}

	public World getWorld() {
		return world;
	}

	public List<GameBuilding> getBuildings() {
		return getPlayers().values().stream().map(GamePlayer::getBuildings).flatMap(List::stream).toList();
	}
}

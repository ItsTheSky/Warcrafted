package net.itsthesky.warcrafted.core.game;

import lombok.Getter;
import net.itsthesky.warcrafted.core.game.core.Game;
import net.itsthesky.warcrafted.core.game.core.GameBuilding;
import net.itsthesky.warcrafted.core.game.core.GamePlayer;
import net.itsthesky.warcrafted.core.game.core.GameTroop;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;

import java.util.*;

public final class GamesManager {

	@Getter
	private static final Set<Game> games = new HashSet<>();

	// ======= Performance-wise storage
	private static final Map<String, Game> gamesById = new HashMap<>();
	private static final Map<Location, GameBuilding> buildings = new HashMap<>();
	private static final Map<OfflinePlayer, GamePlayer> players = new HashMap<>();
	private static final Map<Entity, GameTroop> troops = new HashMap<>();

	// ======= Add

	public static void addGame(Game game) {
		games.add(game);
		gamesById.put(game.getId(), game);
	}

	public static void addBuilding(GameBuilding building) {
		buildings.put(building.getLocation(), building);
	}

	public static void addPlayer(GamePlayer player) {
		players.put(player.getPlayer(), player);
	}

	public static void addTroop(GameTroop troop) {
		troops.put(troop.getEntity(), troop);
	}

	// ======= Remove

	public static void removeGame(Game game) {
		games.remove(game);
		gamesById.remove(game.getId());
	}

	public static void removeBuilding(GameBuilding building) {
		buildings.remove(building.getLocation());
	}

	public static void removePlayer(GamePlayer player) {
		players.remove(player.getPlayer());
	}

	public static void removeTroop(GameTroop troop) {
		troops.remove(troop.getEntity());
	}

	// ======= Getters

	public static Game getGameByPlayer(OfflinePlayer player) {
		final GamePlayer gamePlayer = players.get(player);
		if (gamePlayer == null)
			return null;
		return gamePlayer.getGame();
	}

	public static GameBuilding getBuildingByLocation(Location location) {
		return buildings.get(location);
	}

	public static GamePlayer getPlayer(OfflinePlayer player) {
		return players.get(player);
	}

	public static GameTroop getTroop(Entity entity) {
		return troops.get(entity);
	}

	public static Game getGameById(String id) {
		return gamesById.get(id);
	}

	public static List<String> getGameIds() {
		return new ArrayList<>(gamesById.keySet());
	}

}

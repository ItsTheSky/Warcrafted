package net.itsthesky.warcrafted.core.game.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.itsthesky.warcrafted.Warcrafted;
import net.itsthesky.warcrafted.core.game.GamesManager;
import net.itsthesky.warcrafted.core.game.meta.entities.Race;
import net.itsthesky.warcrafted.core.game.meta.entities.Resource;
import net.itsthesky.warcrafted.core.game.meta.entities.troops.Troop;
import net.itsthesky.warcrafted.core.game.meta.entities.buildings.Building;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
@Setter
@RequiredArgsConstructor
public class GamePlayer {

	private final Game game;
	private final OfflinePlayer player;
	private final Race race;
	private final Difficulty difficulty;

	private final Map<Resource, Integer> resources;
	private final List<GameBuilding> buildings;
	private final List<GameTroop> troops;
	private final GameTeam team;

	private int food;
	private int maxFood;

	public GamePlayer(@NotNull Game game,
					  @NotNull OfflinePlayer player,
					  @NotNull Race race,
					  @NotNull Difficulty difficulty,
					  @Nullable GameTeam team) {
		this.game = game;
		this.player = player;
		this.race = race;
		this.difficulty = difficulty;

		// Resources
		this.resources = new HashMap<>();
		for (Resource resource : race.getInitialResources())
			this.resources.put(resource, race.getInitialResource(resource, difficulty));
		// add missing resources
		for (Resource resource : Warcrafted.getInstance().getResourceManager().getEntities())
			if (!this.resources.containsKey(resource))
				this.resources.put(resource, 0);

		// Buildings
		this.buildings = new LinkedList<>();
		final Building startingBuilding = race.getStartingBuilding();
		GameBuilding startingGameBuilding = null;
		if (startingBuilding != null)
			startingGameBuilding = startBuilding(startingBuilding, player.getPlayer().getLocation(), true);

		// Troops
		this.troops = new LinkedList<>();
		for (Troop troop : race.getStartingTroops())
			addTroop(startingGameBuilding, troop);

		// Team
		if (team == null)
			team = new GameTeam(game, game.nextAvailableTeamColor());
		this.team = team;

		// Food
		this.maxFood = race.getMaxFood();
		this.food = 0;

		GamesManager.addPlayer(this);
	}

	public int getResource(@NotNull Resource type) {
		return resources.getOrDefault(type, 0);
	}

	public void setResource(@NotNull Resource type, int amount) {
		this.resources.put(type, amount);
	}

	public void addResource(@NotNull Resource type, int amount) {
		this.resources.put(type, getResource(type) + amount);
	}

	public void removeResource(@NotNull Resource type, int amount) {
		this.resources.put(type, Math.max(0, getResource(type) - amount));
	}

	public List<GameTroop> getControllingTroops() {
		return game.getTroops().stream().filter(t -> t.isController(this)).toList();
	}

	public void send(String key, @Nullable Object... replacements) {
		Warcrafted.lang().sendKey(player.getPlayer(), key, replacements);
	}

	public GameBuilding startBuilding(Building building, Location location, boolean instant) {
		final GameBuilding gameBuilding = new GameBuilding(this, building, location, instant);
		buildings.add(gameBuilding);
		return gameBuilding;
	}

	/**
	 * Called every second
	 */
	public void update() {

		// Update stats
		int newMaxFood = 0;
		for (GameBuilding gameBuilding : getBuildings())
			newMaxFood += gameBuilding.getBuilding().getStatByName("food");
		if (newMaxFood != maxFood)
			maxFood = Math.min(newMaxFood, race.getMaxFood());

		int newFood = 0;
		for (GameTroop troop : troops)
			newFood += troop.getTroop().getFood();
		if (newFood != food)
			food = Math.min(newFood, maxFood);

		// Update buildings
		for (GameBuilding building : buildings)
			building.update();

		// Update troops
		for (GameTroop troop : troops)
			troop.update();

		// Send the actionbar message
		final List<Component> resources = new ArrayList<>();
		for (Resource resource : this.resources.keySet()) {
			final int amount = this.resources.get(resource);
			final String color = resource.getColor();

			resources.add(Warcrafted.lang().key("action-bar.resource",
					"<" + color + ">", resource.getName(), amount, resource.getSymbol()));
		}
		// add food
		resources.add(Warcrafted.lang().key("action-bar.food",
				food, maxFood));
		// add controlling entities
		final List<GameTroop> controllingTroops = getControllingTroops();
		if (controllingTroops.isEmpty())
			resources.add(Warcrafted.lang().key("action-bar.no-controlling"));
		else
			resources.add(Warcrafted.lang().key("action-bar.controlling", controllingTroops.size()));
		// send final component
		final Component separator = Component.text(" ").append(Warcrafted.lang().key("action-bar.separator")).append(Component.text(" "));
		Warcrafted.getInstance().getAdventure().player(player.getPlayer())
				.sendActionBar(Component.join(JoinConfiguration.separator(separator), resources));
	}

	public boolean hasBuilding(Building building) {
		return buildings.stream().anyMatch(b -> b.getBuilding().getId().equals(building.getId()));
	}

	public void addTroop(@Nullable GameBuilding comingFrom, Troop troop) {
		final GameBuilding from = comingFrom == null ? buildings.get(0) : comingFrom;
		final Location location = from.getLocation().clone().add(0, 1, 0);

		final GameTroop gameTroop = new GameTroop(game, this, troop, location, from);
		troops.add(gameTroop);
	}

	public void clear() {
		for (GameTroop troop : List.copyOf(troops))
			troop.clear();
		for (GameBuilding building : List.copyOf(buildings))
			building.clear();

		troops.clear();
		buildings.clear();
	}
}

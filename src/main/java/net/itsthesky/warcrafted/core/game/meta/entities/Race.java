package net.itsthesky.warcrafted.core.game.meta.entities;

import lombok.Getter;
import net.itsthesky.warcrafted.Warcrafted;
import net.itsthesky.warcrafted.core.game.core.Difficulty;
import net.itsthesky.warcrafted.core.game.meta.entities.buildings.Building;
import net.itsthesky.warcrafted.core.game.meta.entities.troops.Troop;
import net.itsthesky.warcrafted.util.ConfigManager;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Race {

	private final ConfigManager<Race> manager;

	private final String id;
	private final Map<String, Integer> initialResources; // override
	private final Map<String, Integer> startingTroops;
	private final String startingBuilding;
	private final int maxFood;

	public Race(ConfigManager<Race> manager, ConfigurationSection section) {
		this.manager = manager;

		this.id = section.getName();

		// Initial resources
		this.initialResources = new HashMap<>();
		if (section.isConfigurationSection("initial-resources")) {
			final ConfigurationSection initialResourcesSection = section.getConfigurationSection("initial-resources");
			assert initialResourcesSection != null;
			for (String key : initialResourcesSection.getKeys(false))
				this.initialResources.put(key, initialResourcesSection.getInt(key));
		}

		// Starting troops
		this.startingTroops = new HashMap<>();
		if (section.isConfigurationSection("starting-troops")) {
			final ConfigurationSection startingTroopsSection = section.getConfigurationSection("starting-troops");
			assert startingTroopsSection != null;
			for (String troopId : startingTroopsSection.getKeys(false))
				this.startingTroops.put(troopId, startingTroopsSection.getInt(troopId));
		}

		// Starting building
		startingBuilding = section.getString("starting-building", null);

		// Max food
		maxFood = section.getInt("max-food", 100);
	}

	public int getInitialResource(Resource resource, Difficulty difficulty) {
		return initialResources.getOrDefault(resource.getId(), resource.getInitialAmount(difficulty));
	}

	public int getStartingTroopLevel(Troop troop) {
		return startingTroops.getOrDefault(troop.getId(), 0);
	}

	public List<Troop> getStartingTroops() {
		final List<Troop> list = new ArrayList<>();

		for (String troopId : startingTroops.keySet())
		{
			final Troop troop = manager.getPlugin().getTroopManager().getEntityById(troopId);
			if (troop == null) {
				Warcrafted.getInstance().getLogger().warning("Troop with ID " + troopId + " not found!");
				continue;
			}
			for (int i = 0; i < startingTroops.get(troopId); i++)
				list.add(troop);
		}

		return list;
	}

	public @Nullable Building getStartingBuilding() {
		return startingBuilding == null ? null : manager.getPlugin().getBuildingManager().getEntityById(startingBuilding);
	}

	public @NotNull List<Resource> getInitialResources() {
		final List<Resource> list = new ArrayList<>();

		for (String resourceId : initialResources.keySet())
			list.add(manager.getPlugin().getResourceManager().getEntityById(resourceId));

		return list;
	}

	@Override
	public String toString() {
		return getId();
	}
}

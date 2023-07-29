package net.itsthesky.warcrafted.internal.game.meta.entities;

import lombok.Getter;
import net.itsthesky.warcrafted.api.game.Difficulty;
import net.itsthesky.warcrafted.internal.game.meta.entities.buildings.Building;
import net.itsthesky.warcrafted.internal.util.ConfigManager;
import org.bukkit.configuration.ConfigurationSection;
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
	private final Map<String, Integer> maxResources; // override
	private final Map<String, Integer> startingTroops;
	private final String startingBuilding;

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

		// Max resources
		this.maxResources = new HashMap<>();
		if (section.isConfigurationSection("max-resources")) {
			final ConfigurationSection maxResourcesSection = section.getConfigurationSection("max-resources");
			assert maxResourcesSection != null;
			for (String key : maxResourcesSection.getKeys(false))
				this.maxResources.put(key, maxResourcesSection.getInt(key));
		}

		// Starting troops
		this.startingTroops = new HashMap<>();
		if (section.isConfigurationSection("starting-troops")) {
			final ConfigurationSection startingTroopsSection = section.getConfigurationSection("starting-troops");
			assert startingTroopsSection != null;
			for (String troopId : startingTroopsSection.getKeys(false))
				this.maxResources.put(troopId, startingTroopsSection.getInt(troopId));
		}

		// Starting building
		startingBuilding = section.getString("starting-building", null);
	}

	public int getInitialResource(Resource resource, Difficulty difficulty) {
		return initialResources.getOrDefault(resource.getId(), resource.getInitialAmount(difficulty));
	}

	public int getMaxResource(Resource resource) {
		return maxResources.getOrDefault(resource.getId(), resource.getMaxAmount());
	}

	public int getStartingTroops(Troop troop) {
		return startingTroops.getOrDefault(troop.getId(), 0);
	}

	public List<Troop> getStartingTroops() {
		final List<Troop> list = new ArrayList<>();

		for (String troopId : startingTroops.keySet())
			list.add(manager.getPlugin().getTroopManager().getEntityById(troopId));

		return list;
	}

	public @Nullable Building getStartingBuilding() {
		return startingBuilding == null ? null : manager.getPlugin().getBuildingManager().getEntityById(startingBuilding);
	}
}

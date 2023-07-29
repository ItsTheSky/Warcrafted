package net.itsthesky.warcrafted.internal.game.meta.entities.buildings;

import lombok.Getter;
import net.itsthesky.warcrafted.Warcrafted;
import net.itsthesky.warcrafted.internal.game.meta.entities.Resource;
import net.itsthesky.warcrafted.internal.util.ConfigManager;
import net.itsthesky.warcrafted.internal.util.IntRange;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Building {

	private final ConfigManager<Building> manager;

	private final String id;
	private final Map<String, Integer> cost;
	private final List<String> requirements;
	private final int health;
	private final int buildTime;
	private final boolean isUpgrade;
	private final String upgradeTo;
	private final int maxAmount;

	// attacking
	private final IntRange attackDamage;
	private final double attackSpeed;
	private final int attackRange;

	public Building(ConfigManager<Building> manager, ConfigurationSection section) {
		this.manager = manager;

		this.id = section.getName();

		// Cost
		this.cost = new HashMap<>();
		if (section.isConfigurationSection("cost")) {
			final ConfigurationSection costSection = section.getConfigurationSection("cost");
			assert costSection != null;

			for (String key : costSection.getKeys(false))
				this.cost.put(key, costSection.getInt(key));
		}

		// other
		this.health = section.getInt("health");
		this.isUpgrade = section.getBoolean("upgrade", false);
		this.requirements = section.isList("requirements") ? section.getStringList("requirements") : List.of();
		this.buildTime = section.getInt("build-time");
		this.upgradeTo = section.getString("upgrade-to", null);
		this.maxAmount = section.getInt("max-amount", 0);

		// attacking
		this.attackDamage = new IntRange(section.getString("attack-damage", "0"));
		this.attackSpeed = section.getDouble("attack-speed", 0);
		this.attackRange = section.getInt("attack-range", 0);
	}

	/**
	 * Get the unique ID of this building.
	 * @return The unique ID of this building.
	 */
	public @NotNull String getId() {
		return id;
	}

	/**
	 * Get the building's cost to build.
	 * @return The building's cost to build.
	 */
	public @NotNull Map<Resource, Integer> getCost() {
		final Map<Resource, Integer> cost = new HashMap<>();
		for (Map.Entry<String, Integer> entry : this.cost.entrySet()) {
			final Resource resource = manager.getPlugin().getResourceManager().getEntityById(entry.getKey());
			if (resource == null)
				throw new IllegalArgumentException("Unknown resource " + entry.getKey() + " for building " + id);
			cost.put(resource, entry.getValue());
		}

		return Map.copyOf(cost);
	}

	/**
	 * Get the requirements to build the building.
	 * @return The requirements to build the building.
	 */
	public @NotNull List<Building> getRequirements() {
		return requirements.stream().map(manager::getEntityById).toList();
	}

	/**
	 * Get the building's name.
	 * @return The building's name.
	 */
	public @NotNull Component getName() {
		return Warcrafted.lang().key("building." + id + ".name");
	}

	/**
	 * Get the building's description (what it does).
	 * @return The building's description (what it does).
	 */
	public @NotNull Component getDescription() {
		return Warcrafted.lang().key("building." + id + ".description");
	}

	/**
	 * Get the building's upgrade. Will return null if the building is not an upgrade.
	 * @return The building's upgrade. Will return null if the building is not an upgrade.
	 */
	public @Nullable Building getUpgrade() {
		return manager.getEntityById(upgradeTo);
	}
}

package net.itsthesky.warcrafted.core.game.meta.entities.buildings.features;

import org.bukkit.configuration.ConfigurationSection;

public abstract class BuildingFeature {

	protected final String id;
	protected BuildingFeature(ConfigurationSection section) {
		this.id = section.getName();
	}

}

package net.itsthesky.warcrafted.core.game.meta.entities;

import lombok.Getter;
import net.itsthesky.warcrafted.Warcrafted;
import net.itsthesky.warcrafted.core.game.core.Difficulty;
import net.itsthesky.warcrafted.util.ConfigManager;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

@Getter
public class Resource {

	private final ConfigManager<Resource> manager;

	private final String id;
	private final String symbol;
	private final String color;
	private final int initialAmount;
	private final boolean difficultyPercentage;

	public Resource(ConfigManager<Resource> manager, ConfigurationSection section) {
		this.manager = manager;

		this.id = section.getName();
		this.symbol = section.getString("symbol");
		this.initialAmount = section.getInt("initial-amount");
		this.difficultyPercentage = section.getBoolean("difficulty-percentage", false);
		this.color = section.getString("color", "red");
	}

	public @NotNull Component getName() {
		return Warcrafted.lang().key("resources." + id + ".name");
	}

	public int getInitialAmount(Difficulty difficulty) {
		if (!difficultyPercentage)
			return initialAmount;

		return switch (difficulty) {
			case EASY -> (int) (initialAmount * 1.25);
			case NORMAL -> initialAmount;
			case HARD -> (int) (initialAmount * 0.85);
			case INSANE -> (int) (initialAmount * 0.70);
		};
	}

	public @NotNull Component getDescription() {
		return Warcrafted.lang().key("resources." + id + ".description");
	}

	@Override
	public String toString() {
		return getId();
	}
}

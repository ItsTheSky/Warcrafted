package net.itsthesky.warcrafted.internal.game.meta.entities;

import lombok.Getter;
import net.itsthesky.warcrafted.internal.util.ConfigManager;
import net.itsthesky.warcrafted.internal.util.IntRange;
import org.bukkit.configuration.ConfigurationSection;

@Getter
public class Troop {

	private final ConfigManager<Troop> manager;

	private final String id;
	private final int health;
	private final TroopType troopType;

	private final IntRange airAttack;
	private final IntRange groundAttack;

	private final IntRange airDefense;
	private final IntRange groundDefense;

	private final double speed;
	private final double attackSpeed;
	private final int range;

	public Troop(ConfigManager<Troop> manager, ConfigurationSection section) {
		this.manager = manager;

		this.id = section.getName();
		this.health = section.getInt("health");
		this.troopType = TroopType.valueOf(section.getString("type", "GROUND").toUpperCase());

		this.airAttack = new IntRange(section.getString("air-attack", "0"));
		this.groundAttack = new IntRange(section.getString("ground-attack", "0"));

		this.airDefense = new IntRange(section.getString("air-defense", "0"));
		this.groundDefense = new IntRange(section.getString("ground-defense", "0"));

		this.range = section.getInt("range", 0);
		this.speed = section.getDouble("speed");
		this.attackSpeed = section.getDouble("attack-speed", 0d);
	}

	public enum TroopType {
		AIR, GROUND
	}

	private static final double REDUCTION_CONSTANT = 0.06;
	public int calculateDamageAgainst(Troop troop) {
		// Dégâts réels = Dégâts de base * (1 - (Défense de la cible / (Défense de la cible + Constante de réduction)))
		final int base = troop.getTroopType() == TroopType.AIR ? this.airAttack.get() : this.groundAttack.get();
		final int defense = troop.getTroopType() == TroopType.AIR ? this.airDefense.get() : this.groundDefense.get();

		return (int) (base * (1 - (defense / (defense + REDUCTION_CONSTANT))));
	}
}

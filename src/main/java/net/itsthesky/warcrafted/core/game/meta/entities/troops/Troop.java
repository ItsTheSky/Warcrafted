package net.itsthesky.warcrafted.core.game.meta.entities.troops;

import lombok.Getter;
import net.itsthesky.warcrafted.Warcrafted;
import net.itsthesky.warcrafted.util.ConfigManager;
import net.itsthesky.warcrafted.util.IntRange;
import net.itsthesky.warcrafted.util.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Troop {

	private final ConfigManager<Troop> manager;

	private final String id;
	private final int health;
	private final int food;
	private final TroopType troopType;
	private final EntityType entityType;
	private final Map<EquipmentSlot, ItemStack> equipment;
	private final List<TroopTask> tasks;

	private final IntRange airAttack;
	private final IntRange groundAttack;

	private final IntRange airDefense;
	private final IntRange groundDefense;

	private final double speed;
	private final double attackSpeed;
	private final int range;
	private final boolean worker;

	private final String head;

	public Troop(ConfigManager<Troop> manager, ConfigurationSection section) {
		this.manager = manager;

		this.id = section.getName();
		this.health = section.getInt("health");
		this.food = section.getInt("food", 0);
		this.troopType = TroopType.valueOf(section.getString("type", "GROUND").toUpperCase());
		this.entityType = EntityType.valueOf(section.getString("entity-type", "ZOMBIE").toUpperCase());
		this.worker = section.getBoolean("worker", false);

		this.airAttack = new IntRange(section.getString("air-attack", "0"));
		this.groundAttack = new IntRange(section.getString("ground-attack", "0"));

		this.airDefense = new IntRange(section.getString("air-defense", "0"));
		this.groundDefense = new IntRange(section.getString("ground-defense", "0"));

		this.range = section.getInt("range", 0);
		this.speed = section.getDouble("speed");
		this.attackSpeed = section.getDouble("attack-speed", 0d);

		this.head = section.getString("head", "MHF_Zombie");

		// Equipments
		this.equipment = new HashMap<>();
		if (section.isConfigurationSection("armor")) {
			ConfigurationSection armor = section.getConfigurationSection("armor");

			assert armor != null;
			for (String slot : armor.getKeys(false)) {
				final EquipmentSlot equipmentSlot = EquipmentSlot.valueOf(slot.toUpperCase());
				final ItemStack item = Util.deserializeItem(armor.getConfigurationSection(slot));

				if (item != null)
					this.equipment.put(equipmentSlot, item);
			}
		}

		// Tasks
		this.tasks = new ArrayList<>();
		if (section.isList("tasks"))
			for (String task : section.getStringList("tasks"))
				this.tasks.add(TroopTask.getTaskById(task));
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

	public Component getName() {
		return Warcrafted.lang().key("troops." + id + ".name");
	}

	public Component getDescription() {
		return Warcrafted.lang().key("troops." + id + ".description");
	}

	@Override
	public String toString() {
		return getId();
	}
}

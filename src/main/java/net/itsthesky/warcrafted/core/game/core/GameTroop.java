package net.itsthesky.warcrafted.core.game.core;

import de.tr7zw.changeme.nbtapi.NBTEntity;
import lombok.Getter;
import lombok.Setter;
import net.itsthesky.warcrafted.core.game.GamesManager;
import net.itsthesky.warcrafted.core.game.meta.entities.troops.Troop;
import net.itsthesky.warcrafted.core.game.meta.entities.troops.TroopTask;
import net.itsthesky.warcrafted.util.ItemBuilder;
import net.itsthesky.warcrafted.util.gui.AbstractGUI;
import net.itsthesky.warcrafted.util.nms.NMSManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
public class GameTroop {

	private final UUID uuid = UUID.randomUUID();

	private final Game game;
	private final GamePlayer player;

	private final Troop troop;
	private final Entity entity;

	private int health;
	private int mana;
	private Location target;
	private GameBuilding creator;
	private TroopState state;
	private boolean currentTaskStarted = false;

	private @Nullable GamePlayer controller; // can be another one than the owner if the troop is controlled by an ally
	private @Nullable TroopTask currentTask;

	public enum TroopState {
		/**
		 * Can attack entities around him
		 */
		ATTACKING,
		/**
		 * A specific {@link TroopTask} is running on the troop
		 */
		TASKED,
		/**
		 * The troop is doing nothing
		 */
		IDLE,
		/**
		 * The troop is walking to a specific location (target)
		 */
		WALKING
	}

	public GameTroop(Game game, GamePlayer player, Troop troop,
					 Location spawn, @NotNull GameBuilding creator) {
		this.game = game;
		this.player = player;
		this.troop = troop;
		this.creator = creator;

		this.health = troop.getHealth();
		this.mana = 0;

		this.state = TroopState.IDLE;

		final EntityType type = troop.getEntityType();
		if (type == null)
			throw new NullPointerException("Entity type of troop " + troop.getName() + " is null!");

		assert type.getEntityClass() != null;
		this.entity = spawn.getWorld().spawn(spawn.toCenterLocation(), type.getEntityClass(), e -> {
			final LivingEntity livingEntity = (LivingEntity) e;
			// TODO: 01/08/2023 format the name
			livingEntity.customName(troop.getName());
			livingEntity.setCustomNameVisible(true);

			livingEntity.setInvulnerable(true);
			livingEntity.setSilent(true);
			livingEntity.setCollidable(true);
			livingEntity.setAI(true);

			final Map<EquipmentSlot, ItemStack> slots = troop.getEquipment();
			for (EquipmentSlot slot : slots.keySet())
				Objects.requireNonNull(livingEntity.getEquipment()).setItem(slot, slots.get(slot));

			final ItemStack item = new ItemBuilder(Material.PLAYER_HEAD)
					.withBase64(troop.getHead()).build();
			livingEntity.getEquipment().setHelmet(item);

			Bukkit.getMobGoals().removeAllGoals((Mob) livingEntity);
			livingEntity.setGlowing(true);

			final NBTEntity nbtEntity = new NBTEntity(livingEntity);
			nbtEntity.setBoolean("warcrafted", true);

			nbtEntity.setString("troop", troop.getId());
			nbtEntity.setString("game", game.getId());
		});

		GamesManager.addTroop(this);
	}

	public void update() {
		Location desiredLocation;

		if (this.target == null && this.state == TroopState.IDLE && !currentTaskStarted) {
			currentTaskStarted = true;
			this.state = TroopState.WALKING;

			desiredLocation = this.creator.getTroopSpawnLocation().clone();
		} else if (this.target != null && !currentTaskStarted) {
			this.state = TroopState.WALKING;

			currentTaskStarted = true;
			desiredLocation = this.target.clone();
		} else {
			desiredLocation = null;
		}

		if (desiredLocation != null) {
			Location finalDesiredLocation = desiredLocation;
			AbstractGUI.later(() -> NMSManager.getMobIA().moveTo(this.entity, 1.0d, finalDesiredLocation), 5);
			desiredLocation = null;
		}
		if (currentTask != null && currentTaskStarted && this.state == TroopState.TASKED) {
			currentTask.update(this);
		}
	}

	public void startTask(TroopTask task) {
		this.currentTask = task;
		this.state = TroopState.TASKED;
		this.currentTaskStarted = false;

		task.onStart(this);
	}

	public boolean canBeControlledBy(GamePlayer player) {
		return this.player.equals(player) || (this.controller != null && this.controller.equals(player));
	}

	public boolean isController(GamePlayer controller) {
		return this.controller != null && this.controller.equals(controller);
	}

	public void setController(GamePlayer controller) {
		if (this.controller != null) {
			try {
				NMSManager.getGlowingEntities().unsetGlowing(this.entity, player.getPlayer().getPlayer());
			} catch (ReflectiveOperationException ex) {
				throw new RuntimeException(ex);
			}
		}

		this.controller = controller;
		if (controller != null) {
			try {
				NMSManager.getGlowingEntities().setGlowing(this.entity, controller.getPlayer().getPlayer(), ChatColor.GOLD);
			} catch (ReflectiveOperationException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	public void clear() {
		this.entity.remove();
	}
}

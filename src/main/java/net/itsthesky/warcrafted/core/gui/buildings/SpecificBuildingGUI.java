package net.itsthesky.warcrafted.core.gui.buildings;

import net.itsthesky.warcrafted.core.game.core.GameBuilding;
import net.itsthesky.warcrafted.core.game.meta.entities.buildings.Building;
import net.itsthesky.warcrafted.core.game.meta.entities.buildings.BuildingTask;
import net.itsthesky.warcrafted.util.ChatWaiter;
import net.itsthesky.warcrafted.util.ItemBuilder;
import net.itsthesky.warcrafted.util.Pair;
import net.itsthesky.warcrafted.util.gui.AbstractGUI;
import net.itsthesky.warcrafted.util.gui.GUI;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class SpecificBuildingGUI extends AbstractGUI {

	private final GameBuilding building;
	public SpecificBuildingGUI(@Nullable AbstractGUI parent, GameBuilding building, Player player) {
		super(parent, lang().key("gui.building.specific", building.getBuilding().getName()), 6);
		this.building = building;

		setItems(() -> new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
				.withName(" ")
				.build(), e -> e.setCancelled(true), getBorders());
		createBackButton(49);

		final boolean hasTroops = building.getBuilding().getTasks().stream().anyMatch(task -> task.getType().equals(BuildingTask.Type.RECRUIT_TROOP));

		// Upgrade
		final Building next = building.getBuilding().getUpgradeTo();
		final int upgradeSlot = 8;
		if (next == null) {
			setItem(upgradeSlot, () -> new ItemBuilder(Material.PLAYER_HEAD)
					.withBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2VkMWFiYTczZjYzOWY0YmM0MmJkNDgxOTZjNzE1MTk3YmUyNzEyYzNiOTYyYzk3ZWJmOWU5ZWQ4ZWZhMDI1In19fQ==")
					.withName(lang().key("gui.building.specific.upgrade.no-upgrade.name"))
					.withLore(lang().key("gui.building.specific.upgrade.no-upgrade.lore"))
					.build(), e -> {
				e.setCancelled(true);
			});
		} else {
			final String rawLore = lang().rawKey("gui.building.specific.upgrade.lore");

			// Resources
			final List<Component> resources = new ArrayList<>();
			final var cost = next.getCost();
			boolean canUpgrade = true;
			for (final var entry : cost.entrySet()) {
				final boolean hasEnough = building.getPlayer().getResource(entry.getKey()) >= entry.getValue();
				if (!hasEnough)
					canUpgrade = false;

				final String key = hasEnough
						? "gui.building.specific.upgrade.has-resource-item"
						: "gui.building.specific.upgrade.has-not-resource-item";
				resources.add(lang().key(key, entry.getKey().getName(), building.getPlayer().getResource(entry.getKey()), entry.getValue()));
			}

			// Requirements
			final List<Component> requirements = new ArrayList<>();
			final List<Building> required = next.getRequirements();
			for (final var req : required) {
				final boolean hasEnough = building.getPlayer().hasBuilding(req);
				if (!hasEnough)
					canUpgrade = false;

				final String key = hasEnough
						? "gui.building.specific.upgrade.has-requirements-item"
						: "gui.building.specific.upgrade.has-not-requirements-item";
				requirements.add(lang().key(key, req.getName()));
			}
			if (requirements.isEmpty())
				requirements.add(lang().key("gui.building.specific.upgrade.no-requirements"));

			// Build final lore
			final List<Component> lore = new ArrayList<>();
			final String rawDesc = canUpgrade ? "gui.building.specific.upgrade.upgrade.info" : "gui.building.specific.upgrade.cannot-upgrade.info";
			for (var line : rawLore.split("\n")) {
				line = line.replace("%1", lang().rawKey(rawDesc));

				if (line.contains("%2")) {
					lore.addAll(resources);
					continue;
				}

				if (line.contains("%3")) {
					lore.addAll(requirements);
					continue;
				}

				lore.add(lang().parse(line));
			}

			boolean finalCanUpgrade = canUpgrade;
			setItem(upgradeSlot, () -> new ItemBuilder(Material.PLAYER_HEAD)
					.withBase64(finalCanUpgrade
							? "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWRhMDI3NDc3MTk3YzZmZDdhZDMzMDE0NTQ2ZGUzOTJiNGE1MWM2MzRlYTY4YzhiN2JjYzAxMzFjODNlM2YifX19"
							: "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2U0ZjJmOTY5OGMzZjE4NmZlNDRjYzYzZDJmM2M0ZjlhMjQxMjIzYWNmMDU4MTc3NWQ5Y2VjZDcwNzUifX19"
					)
					.withName(lang().key("gui.building.specific.upgrade.upgrade.name", next.getName()))
					.withLore(lore)
					.build(), e -> {
				e.setCancelled(true);
				if (!finalCanUpgrade) {
					player.sendMessage(lang().key("gui.building.specific.upgrade.not-enough"));
					return;
				}
				for (final var entry : cost.entrySet())
					building.getPlayer().removeResource(entry.getKey(), entry.getValue());

				player.sendMessage(lang().key("gui.building.specific.upgrade.success"));
			});
		}

		// Tasks
		setItem(4, () -> new ItemBuilder(Material.PAPER)
				.withName(lang().key("gui.building.specific.tasks.name"))
				.withLore(lang().keys("gui.building.specific.tasks.lore"))
				.build(), e -> e.setCancelled(true));
		setItems(() -> new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
				.withName(" ")
				.build(), e -> e.setCancelled(true), IntStream.range(19, 26).toArray());

		if (hasTroops) { // en bas à gauche
			setItem(53, () -> new ItemBuilder(Material.PLAYER_HEAD)
					.withBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTRkNDhhYzdlNjViOTE0Njc5MDRmMmY0ZDE1NDU1Njc3NmZmYTljZDJkNTI2YjEyYTA0OTliYjU5Y2M2NTZjNyJ9fX0=")
					.withName(lang().key("gui.building.specific.troops.spawn-point.name"))
					.withLore(lang().keys("gui.building.specific.troops.spawn-point.lore"))
					.build(), e -> {
				e.setCancelled(true);
				e.getWhoClicked().closeInventory();

				lang().sendKey(player, "game.messages.troop-spawn-point.move");
				ChatWaiter.listen(player, chatEvent -> {
					final var message = chatEvent.getMessage();
					if (message.equalsIgnoreCase("cancel")) {
						lang().sendKey(player, "game.messages.troop-spawn-point.cancelled");
						return;
					} else if (message.equalsIgnoreCase("confirm")) {
						lang().sendKey(player, "game.messages.troop-spawn-point.saved");
						building.setTroopSpawnLocation(player.getLocation());
						return;
					}
				});
			});
		}

		// first, available tasks
		final List<BuildingTask> availableTasks = building.getBuilding().getTasks();
		int slot = 10;
		for (final var task : availableTasks) {
			setItem(slot, () -> {

				final BuildingTask.ActivableState state = task.getActivableState(building);

				final ItemBuilder builder;
				final Consumer<InventoryClickEvent> action;
				if (state == BuildingTask.ActivableState.AVAILABLE) {

					builder = new ItemBuilder(Material.PLAYER_HEAD)
							.withBase64(task.getBase64Icon())
							.withName(lang().key("gui.building.specific.tasks.task.name", task.getName(), Math.round((float) task.getTime() / 1000L)))
							.withLore(task.getDescription(building));

					action = e -> {
						e.setCancelled(true);

						final var cost = task.getCost();
						for (final var entry : cost.entrySet())
							building.getPlayer().removeResource(entry.getKey(), entry.getValue());

						building.addTask(task);
						player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
						refreshInventory();
					};

				} else {

					final String icon = state == BuildingTask.ActivableState.LOCKED
							? "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2JkM2M1MjA1MGVkMzVlMWY4ZjFlZjc5ZTIxZmM2MWJhODkwNzEyOTZkMTlmYzNhMDI5NTI2YjY4ZjdkODI3ZjcifX19"
							: "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDVhZmZmZTdhOTRlMTI2NmExODhjODM1M2ExOWE5NWJlMTUyNTUyN2I0M2Y5NzEzY2YxMzQ3ZjQxNTU1ZmNkIn19fQ==";

					builder = new ItemBuilder(Material.PLAYER_HEAD)
							.withBase64(icon)
							.withName(lang().key("gui.building.specific.tasks.task.name", task.getName(), Math.round((float) task.getTime() / 1000L)))
							.withLore(task.getDescription(building));

					action = e -> e.setCancelled(true);
				}

				return new Pair<>(builder.build(), action);
			});
			slot++;
		}
	}

	@Override
	public @NotNull GUI createCopy(@NotNull Player player) {
		return new SpecificBuildingGUI(getParent(), building, player);
	}

}

package net.itsthesky.warcrafted.core.listeners;

import net.itsthesky.warcrafted.Warcrafted;
import net.itsthesky.warcrafted.core.game.GamesManager;
import net.itsthesky.warcrafted.core.game.core.GameBuilding;
import net.itsthesky.warcrafted.core.game.core.GamePlayer;
import net.itsthesky.warcrafted.core.game.core.GameTroop;
import net.itsthesky.warcrafted.core.game.meta.entities.troops.TroopTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Listener for blocks interactions
 */
public class PlayersActionListener implements Listener {

	// Open the building GUI
	@EventHandler
	public void onRightRight(PlayerInteractEvent event) {
		if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || (event.getHand() != null && event.getHand().equals(EquipmentSlot.OFF_HAND)))
			return;
		final GameBuilding building = GamesManager.getBuildingByLocation(event.getClickedBlock().getLocation());
		if (building == null)
			return;

		final GamePlayer buildingPlayer = building.getPlayer();
		if (!buildingPlayer.getPlayer().getUniqueId().equals(event.getPlayer().getUniqueId())) {
			Warcrafted.lang().sendKey(event.getPlayer(), "game.messages.building.not-yours");
			event.setCancelled(true);
			return;
		}

		building.onRightClick(event, event.getPlayer());
	}

	// Manage controlling entities
	@EventHandler
	public void onInteract(PlayerInteractAtEntityEvent event) {
		if (event.getHand().equals(EquipmentSlot.OFF_HAND))
			return;
		if (!event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.BLAZE_ROD))
			return;

		final GamePlayer player = GamesManager.getPlayer(event.getPlayer());
		if (player == null)
			return;

		final GameTroop troop = GamesManager.getTroop(event.getRightClicked());
		if (troop == null)
			return;

		if (!troop.canBeControlledBy(player)) {
			Warcrafted.lang().sendKey(event.getPlayer(), "game.messages.troop.cannot-control");
			event.setCancelled(true);
			return;
		}

		if (troop.getController() != null && troop.getController().equals(player))
			troop.setController(null);
		else
			troop.setController(player);
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent event) {
		final GamePlayer player = GamesManager.getPlayer(event.getPlayer());
		if (player == null)
			return;

		for (GameTroop controlling : player.getControllingTroops())
			controlling.setController(null);
		event.setCancelled(true);
	}

	@EventHandler
	public void onLeftClick(PlayerInteractEvent event) {
		if (!event.getHand().equals(EquipmentSlot.HAND))
			return;
		if (!event.getAction().isLeftClick())
			return;
		if (!event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.BLAZE_ROD))
			return;

		final GamePlayer player = GamesManager.getPlayer(event.getPlayer());
		if (player == null)
			return;

		final List<GameTroop> troops = player.getControllingTroops();
		if (troops.isEmpty())
			return;

		final Location target = event.getPlayer().getTargetBlock(null, 15).getLocation();
		for (GameTroop troop : troops) {
			@Nullable TroopTask task = null;
			for (TroopTask available : troop.getTroop().getTasks())
				if (available.shouldAutoEnable(event, player))
					task = available;

			if (task == null) {
				troop.setCurrentTaskStarted(false);
				troop.setTarget(target);
				continue;
			}

			troop.startTask(task);
			Bukkit.broadcast("§aTask started!", "warcrafted.debug");
		}
	}
}

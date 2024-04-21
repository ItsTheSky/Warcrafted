package net.itsthesky.warcrafted.core.game.meta.entities.troops.tasks;

import net.itsthesky.warcrafted.core.game.core.GamePlayer;
import net.itsthesky.warcrafted.core.game.core.GameTroop;
import net.itsthesky.warcrafted.core.game.meta.entities.troops.TroopTask;
import net.itsthesky.warcrafted.util.nms.NMSManager;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public class GatherBlockTask extends TroopTask {

	public GatherBlockTask() {
		super("gather-wood");
	}

	@Override
	public void onStart(GameTroop troop) {
		NMSManager.getMobIA().addCollectBlockGoal(troop,
				Material.OAK_LOG, 1.0f, 5);
		troop.getPlayer().getPlayer().getPlayer().sendMessage("§aGather wood task enabled!");
	}

	@Override
	public void update(GameTroop troop) {
		// no need to update anything
	}

	@Override
	public boolean shouldAutoEnable(@NotNull PlayerInteractEvent event, GamePlayer player) {
		return player.getPlayer().getPlayer().getTargetBlock(null, 25).getType() == Material.OAK_LOG;
	}

}

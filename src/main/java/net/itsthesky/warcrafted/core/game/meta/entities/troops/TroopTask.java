package net.itsthesky.warcrafted.core.game.meta.entities.troops;

import lombok.Getter;
import net.itsthesky.warcrafted.core.game.core.GamePlayer;
import net.itsthesky.warcrafted.core.game.core.GameTroop;
import net.itsthesky.warcrafted.core.game.meta.entities.troops.tasks.GatherBlockTask;
import net.itsthesky.warcrafted.core.game.meta.entities.troops.tasks.undead.AcolyteCollectGoldTask;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public abstract class TroopTask {

	private static final Map<String, TroopTask> TASKS = new HashMap<>();

	static {
		register(new GatherBlockTask());
		register(new AcolyteCollectGoldTask());
	}

	private static void register(TroopTask task) {
		TASKS.put(task.id, task);
	}

	public static TroopTask getTaskById(@NotNull String input) {
		return TASKS.get(input);
	}

	// ##########################################

	@Getter
	private final String id;

	protected TroopTask(String id) {
		this.id = id;
	}

	public abstract void onStart(GameTroop troop);

	public abstract void update(GameTroop troop);

	public boolean shouldAutoEnable(@NotNull PlayerInteractEvent event, GamePlayer player) {
		return false;
	};

}

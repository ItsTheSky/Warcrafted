package net.itsthesky.warcrafted.util.nms;

import net.itsthesky.warcrafted.core.game.core.GameTroop;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface MobIA {

	void clearIA(@NotNull Entity entity);

	void moveTo(@NotNull Entity entity, double speed, Location target);

	void addCollectBlockGoal(GameTroop troop, Material material, float speed, int maxYDifference);

	void removeCollectBlockGoal(Entity entity);

	@NotNull Object getGoalSelector(@NotNull Entity entity);
}

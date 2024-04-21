package net.itsthesky.warcrafted.util.nms.v1_20_R1;

import net.itsthesky.warcrafted.core.game.core.GameTroop;
import net.itsthesky.warcrafted.util.nms.MobIA;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.RemoveBlockGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class MobIA_v1_20_R1 implements MobIA {

	@Override
	public @NotNull GoalSelector getGoalSelector(@NotNull Entity entity) {
		return ((Mob) ((org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity) entity).getHandle()).goalSelector;
	}

	@Override
	public void clearIA(@NotNull Entity entity) {
		getGoalSelector(entity).removeAllGoals(g -> true);
	}

	@Override
	public void moveTo(@NotNull Entity entity, double speed, Location target) {
		final Mob mob = (Mob) ((CraftLivingEntity) entity).getHandle();
		final PathNavigation navigation = mob.getNavigation();

		navigation.moveTo(target.getX(), target.getY(), target.getZ(), speed);
	}

	@Override
	public void addCollectBlockGoal(GameTroop troop, Material material, float speed, int maxYDifference) {
		final Block block = CraftMagicNumbers.getBlock(material);
		final Entity entity = troop.getEntity();
		final PathfinderMob mob = (PathfinderMob) ((CraftLivingEntity) entity).getHandle();
		final GoalSelector goalSelector = mob.goalSelector;
		//final RemoveBlockGoal removeBlockGoal = new RemoveBlockGoal(block, mob, speed, maxYDifference);
		final BreakBlockGoal removeBlockGoal = new BreakBlockGoal(block, troop, mob, speed);

		goalSelector.addGoal(0, removeBlockGoal);
	}

	@Override
	public void removeCollectBlockGoal(Entity entity) {
		getGoalSelector(entity).removeAllGoals(g -> g instanceof RemoveBlockGoal);
	}

	public static class MoveToLocationGoal extends MoveToBlockGoal {

		private final Location target;
		public MoveToLocationGoal(PathfinderMob mob, double speed, int range, Location target) {
			super(mob, speed, range);
			this.target = target;
		}

		@Override
		protected boolean isValidTarget(@NotNull LevelReader world, @NotNull BlockPos pos) {
			final Location target = this.target;
			final BlockPos targetPos = new BlockPos(target.getBlockX(), target.getBlockY(), target.getBlockZ());

			return targetPos.equals(blockPos);
		}
	}

	// We need a custom goal for removing blokc. The default one is not really "breaking" the block, it just make the mob steps on it.
	public static class BreakBlockGoal extends MoveToBlockGoal {

		private final Block block;
		private final GameTroop troop;
		private final PathfinderMob mob;

		public BreakBlockGoal(Block block, GameTroop troop, PathfinderMob mob, float speed) {
			super(mob, speed, 60);
			this.block = block;
			this.mob = mob;
			this.troop = troop;
		}

		@Override
		public void tick() {
			super.tick();
			// Check if there's the block at 1 block around the mob
			for (int x = -1; x < 2; x++) {
				for (int y = -1; y < 2; y++) {
					for (int z = -1; z < 2; z++) {
						final BlockPos pos = blockPos.offset(x, y, z);
						if (mob.level().getBlockState(pos).getBlock() == block) {
							mob.level().destroyBlock(pos, true, mob);
							//isReachedTarget()
							setIsReachedTarget(true);
							return;
						}
					}
				}
			}
		}

		private void setIsReachedTarget(boolean value) {
			try {
				final Field field = MoveToBlockGoal.class.getDeclaredField("reachedTarget");
				field.setAccessible(true);
				field.set(this, value);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		@Override
		protected boolean isValidTarget(LevelReader world, @NotNull BlockPos pos) {
			return world.getBlockState(pos).getBlock() == block;
		}
	}
}

package net.itsthesky.warcrafted.util;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.NBTListCompound;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Util {

	public static ItemStack createHeadItem(String base64) {
		final NBTItem item = new NBTItem(new ItemStack(org.bukkit.Material.PLAYER_HEAD));
		final NBTCompound skull = item.addCompound("SkullOwner");
		skull.setString("Name", "Default Name");
		final UUID uuid = new UUID(base64.substring(base64.length() - 20).hashCode(),
				base64.substring(base64.length() - 10).hashCode());
		skull.setString("Id", uuid.toString());
		final NBTListCompound texture = skull.addCompound("Properties").getCompoundList("textures").addCompound();
		texture.setString("Value",  base64);
		return item.getItem();
	}

	public static Location toBlockLocation(Location location) {
		return new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	public static String getProgressBar(double currentValue, double maxValue, String character, String uncompletedColor, String gradient, boolean reverse) {
		final StringBuilder builder = new StringBuilder();
		final double percent = currentValue / maxValue;
		final int completed = (int) Math.round(percent * 25);
		final int uncompleted = 25 - completed;
		if (reverse) {
			for (int i = 0; i < uncompleted; i++) {
				builder.append(uncompletedColor).append(character);
			}
			for (int i = 0; i < completed; i++) {
				builder.append(gradient).append(character);
			}
		} else {
			for (int i = 0; i < completed; i++) {
				builder.append(gradient).append(character);
			}
			for (int i = 0; i < uncompleted; i++) {
				builder.append(uncompletedColor).append(character);
			}
		}
		return builder.toString();
	}

	public static ItemStack deserializeItem(ConfigurationSection section) {
		final String type = section.getString("type");
		if (type == null)
			return null;

		final ItemBuilder builder = new ItemBuilder(Material.valueOf(type));

		builder.withAmount(section.getInt("amount", 1));
		if (section.contains("color"))
			builder.withColor(Color.fromARGB(parseHex(section.getString("color"))));

		return builder.build();
	}

	public static int parseHex(String hex) {
		if (hex.startsWith("#")) hex = hex.substring(1);
		return Integer.parseInt(hex, 16);
	}
}

package net.itsthesky.warcrafted.internal.util;

import lombok.Getter;
import net.itsthesky.warcrafted.Warcrafted;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
public final class ConfigManager<T> {

	private final JavaPlugin plugin;
	private final Map<String, T> entities;

	public ConfigManager(JavaPlugin plugin, Class<T> clazz, String fileName) {
		this.plugin = plugin;
		this.entities = new HashMap<>();

		final File config = new File(plugin.getDataFolder(), fileName);
		if (!config.exists())
			plugin.saveResource(fileName, false);

		final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(config);
		for (String key : yaml.getKeys(false))
		{
			try {

				final Constructor<T> constructor = clazz.getConstructor(ConfigManager.class, ConfigurationSection.class);
				constructor.setAccessible(true);

				entities.put(key, constructor.newInstance(this, Objects.requireNonNull(yaml.getConfigurationSection(key))));

			} catch (Exception ex) {
				throw new RuntimeException("Cannot load " + clazz.getSimpleName() + " with id " + key, ex);
			}
		}

		plugin.getLogger().info("Loaded " + entities.size() + " " + clazz.getSimpleName().toLowerCase() + "!");
	}

	public T getEntityById(String id) {
		return entities.get(id);
	}

	public Warcrafted getPlugin() {
		return (Warcrafted) plugin;
	}
}

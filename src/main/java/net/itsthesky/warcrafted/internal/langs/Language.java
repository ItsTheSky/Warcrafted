package net.itsthesky.warcrafted.internal.langs;

import net.itsthesky.warcrafted.Warcrafted;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class Language {

	private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

	private final Properties properties;
	private final String code;

	public Language(@NotNull JavaPlugin plugin, @NotNull String code) throws IOException {
		this.code = code;

		final File langFile = new File(plugin.getDataFolder(), "lang/" + code + ".properties");
		final InputStream langStream = plugin.getResource("lang/" + code + ".properties");

		if (!langFile.exists() && langStream == null)
			plugin.saveResource("lang/" + code + ".properties", false);

		final InputStreamReader reader = new InputStreamReader(langStream);

		properties = new Properties();
		properties.load(reader);

		final Properties defaultConfig = new Properties();
		defaultConfig.load(reader);

		reader.close();
		langStream.close();

		final List<String> missingKeys = new ArrayList<>();
		/*for (String key : defaultConfig.getKeys(true)) {
			if (!config.contains(key)) {
				config.set(key, defaultConfig.get(key));
				missingKeys.add(key);
			}
		} */
		for (String key : defaultConfig.stringPropertyNames()) {
			if (!properties.containsKey(key)) {
				properties.setProperty(key, defaultConfig.getProperty(key));
				missingKeys.add(key);
			}
		}

		if (!missingKeys.isEmpty()) {
			plugin.getLogger().info("Added " + missingKeys.size() + " missing keys to the language file '" + code + "'!");
			properties.store(Files.newOutputStream(langFile.toPath()), "Added missing keys");
		}

		plugin.getLogger().info("Loaded the language file '" + code + "'!");
	}

	public @NotNull String rawKey(@NotNull String key, @Nullable Object... replacements) {
		String rawKey = properties.getProperty(key, key);
		if (replacements == null || replacements.length == 0)
			return rawKey;

		int i = 1;
		for (Object replacement : replacements) {
			rawKey = rawKey.replace("%" + i, replacement.toString());
			i++;
		}

		return rawKey;
	}

	public @NotNull Component key(@NotNull String key, @Nullable Object... replacements) {
		return MINI_MESSAGE.deserialize(rawKey(key, replacements));
	}

	public void sendKey(@NotNull CommandSender sender, @NotNull String key, @Nullable Object... replacements) {
		final Component component = key(key, replacements);
		final Audience audience = Warcrafted.getInstance().adventure().sender(sender);

		audience.sendMessage(component);
	}

	public String getCode() {
		return code;
	}
}

package net.itsthesky.warcrafted;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import lombok.Getter;
import net.itsthesky.warcrafted.internal.commands.AdminCommands;
import net.itsthesky.warcrafted.internal.commands.PlayerCommands;
import net.itsthesky.warcrafted.internal.game.meta.entities.Race;
import net.itsthesky.warcrafted.internal.game.meta.entities.Resource;
import net.itsthesky.warcrafted.internal.game.meta.entities.Troop;
import net.itsthesky.warcrafted.internal.game.meta.entities.buildings.Building;
import net.itsthesky.warcrafted.internal.langs.Language;
import net.itsthesky.warcrafted.internal.util.ConfigManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Getter
public final class Warcrafted extends JavaPlugin {

    @Getter
    private static Warcrafted instance;

    // ######### Meta
    private BukkitAudiences adventure;

    // ######### Languages
    private static final String[] BUILD_IN_LANGUAGES = {"en_US", "fr_FR"};
    @Getter
    private final List<Language> availableLanguages = new ArrayList<>();
    @Getter
    private Language language;

    // ######### Managers
    private ConfigManager<Building> buildingManager;
    private ConfigManager<Resource> resourceManager;
    private ConfigManager<Troop> troopManager;
    private ConfigManager<Race> raceManager;

    @Override
    public void onEnable() {
        instance = this;
        adventure = BukkitAudiences.create(this);



        // ######### Configurations
        saveDefaultConfig();
        reloadConfig();



        // ######### Languages
        final File langFolder = new File(getDataFolder(), "lang");
        if (!langFolder.exists())
            langFolder.mkdirs();

        // sample language file
        for (String lang : BUILD_IN_LANGUAGES) {
            final File langFile = new File(langFolder, lang + ".properties");
            if (!langFile.exists())
                saveResource("lang/" + lang + ".properties", false);
        }

        // custom language file
        final File[] files = langFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".properties")) {
                    final String lang = file.getName().replace(".properties", "");
                    try {
                        availableLanguages.add(new Language(this, lang));
                    } catch (Exception ex) {
                        getLogger().warning("Cannot load the custom language '" + lang + "':");
                        ex.printStackTrace();
                    }
                }
            }
        }
        final boolean success = selectLanguage(getConfig().getString("language", "en_US"));
        if (!success) {
            getLogger().warning("Cannot load the language '" + getConfig().getString("language", "en_US") + "', using the default one!");
            final boolean defSuccess = selectLanguage("en_US");
            if (!defSuccess)
                throw new IllegalStateException("Cannot load the default language 'en_US'!");
        }



        // ######### Managers
        try {

            resourceManager = new ConfigManager<>(this, Resource.class, "content/resources.yml");
            buildingManager = new ConfigManager<>(this, Building.class, "content/buildings.yml");
            troopManager = new ConfigManager<>(this, Troop.class, "content/troops.yml");
            raceManager = new ConfigManager<>(this, Race.class, "content/races.yml");

        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load managers.", ex);
        }



        // ######### Commands
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this));
        PlayerCommands.loadCommands(this);
        AdminCommands.loadCommands(this);
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    public static Language lang() {
        return getInstance().getLanguage();
    }

    // ######### Methods

    public boolean selectLanguage(String code) {
        for (Language lang : availableLanguages) {
            if (lang.getCode().equalsIgnoreCase(code)) {
                language = lang;
                return true;
            }
        }

        return false;
    }

    public BukkitAudiences adventure() {
        return adventure;
    }

    public List<String> getAvailableLanguagesCode() {
        return availableLanguages.stream().map(Language::getCode).toList();
    }
}

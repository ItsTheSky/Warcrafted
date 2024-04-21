package net.itsthesky.warcrafted.core.game;

import net.itsthesky.warcrafted.Warcrafted;
import net.itsthesky.warcrafted.core.game.core.*;
import net.itsthesky.warcrafted.core.game.meta.entities.Race;
import net.itsthesky.warcrafted.core.game.meta.entities.Resource;
import net.itsthesky.warcrafted.core.game.meta.entities.buildings.Building;
import net.itsthesky.warcrafted.core.game.meta.entities.troops.Troop;
import net.itsthesky.warcrafted.core.game.meta.entities.troops.TroopTask;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.*;

public final class GameSaveManager {

    private static final String BASE_DRIVER = "jdbc:sqlite:";

    private final Warcrafted warcrafted;

    private Connection connection;

    public GameSaveManager(Warcrafted warcrafted) {
        this.warcrafted = warcrafted;

        this.connection = null;

        warcrafted.getLogger().info("------------- Databases");
        this.connect();
    }

    public void connect() {
        warcrafted.getLogger().info("[1/2] Loading database file ...");
        final File file = new File(this.warcrafted.getDataFolder(), "games.db");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        final String url = BASE_DRIVER + file.getAbsolutePath();
        warcrafted.getLogger().info("[2/3] Connecting to database: " + url);
        try {
            this.connection = DriverManager.getConnection(url);
        } catch (Exception e) {
            e.printStackTrace();

            warcrafted.getLogger().severe("Unable to connect to the database! (See above)");
            warcrafted.getLogger().severe("Wacrafted will be UNABLE to save games via reload or restarts!");
            warcrafted.getLogger().severe("Please fix the issue and restart the server, or report the issue to the developer.");

            return;
        }

        warcrafted.getLogger().info("[3/3] Database loaded!");

        warcrafted.getLogger().info("[*] Checking tables ...");

        checkTables();

        warcrafted.getLogger().info("[*] Loading games ...");
        loadGames();

        warcrafted.getLogger().info("[*] Games loaded! We'll reset the database now!");
        reset();
    }

    public void checkTables() {

        final String[] tables = new String[] {
                "CREATE TABLE IF NOT EXISTS games (" +
                        "id TEXT NOT NULL," +

                        "PRIMARY KEY (id)" +
                        ");",

                "CREATE TABLE IF NOT EXISTS players (" +
                        "uuid TEXT NOT NULL," +

                        "game TEXT NOT NULL," +
                        "race TEXT NOT NULL," +
                        "difficulty TEXT NOT NULL," +
                        "team TEXT NOT NULL," +
                        "PRIMARY KEY (uuid)," +
                        "FOREIGN KEY (game) REFERENCES games(id)" +
                        ");",

                "CREATE TABLE IF NOT EXISTS teams (" +
                        "color TEXT NOT NULL," +

                        "game TEXT NOT NULL," +
                        "PRIMARY KEY (color)," +
                        "FOREIGN KEY (game) REFERENCES games(id)" +
                        ");",

                "CREATE TABLE IF NOT EXISTS resources (" +
                        "id TEXT NOT NULL," +

                        "player TEXT NOT NULL," +
                        "amount INTEGER NOT NULL," +

                        "PRIMARY KEY (id)," +
                        "FOREIGN KEY (player) REFERENCES players(uuid)" +
                        ");",

                "CREATE TABLE IF NOT EXISTS buildings (" +
                        "uuid TEXT NOT NULL," +

                        "player TEXT NOT NULL," +

                        "x FLOAT NOT NULL," +
                        "y FLOAT NOT NULL," +
                        "z FLOAT NOT NULL," +
                        "world TEXT NOT NULL," +

                        "building TEXT NOT NULL," +
                        "health INTEGER NOT NULL," +
                        "build_date INTEGER NOT NULL," +
                        "repair_date INTEGER NOT NULL," +

                        "spawn_x FLOAT NOT NULL," +
                        "spawn_y FLOAT NOT NULL," +
                        "spawn_z FLOAT NOT NULL," +

                        "PRIMARY KEY (uuid)," +
                        "FOREIGN KEY (player) REFERENCES players(uuid)" +
                        ");",

                "CREATE TABLE IF NOT EXISTS troops (" +
                        "id TEXT NOT NULL," +

                        "player TEXT NOT NULL," +

                        "x FLOAT NOT NULL," +
                        "y FLOAT NOT NULL," +
                        "z FLOAT NOT NULL," +
                        "world TEXT NOT NULL," +

                        "troop TEXT NOT NULL," +
                        "health INTEGER NOT NULL," +
                        "mana INTEGER NOT NULL," +
                        "state TEXT NOT NULL," +
                        "building TEXT NOT NULL," +

                        "PRIMARY KEY (id)," +
                        "FOREIGN KEY (player) REFERENCES players(uuid)," +
                        "FOREIGN KEY (building) REFERENCES buildings(uuid)" +
                        ");",

                "CREATE TABLE IF NOT EXISTS troops_tasks (" +
                        "id TEXT NOT NULL," +

                        "troop TEXT NOT NULL," +
                        "task TEXT NOT NULL," +

                        "PRIMARY KEY (id)," +
                        "FOREIGN KEY (troop) REFERENCES troops(id)" +
                        ");"
        };

        for (String table : tables) {
            try {
                this.connection.createStatement().execute(table);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void saveGames() {

        // Save games
        for (final Game game : GamesManager.getGames()) {

            for (final GamePlayer player : game.getPlayers()) {

                // player
                update("INSERT INTO players " +
                        "(uuid, game, race, difficulty, team) VALUES " +
                        "('" + player.getPlayer().getUniqueId() + "', '" +
                        game.getId() + "', '" +
                        player.getRace().getId() + "', '" +
                        player.getDifficulty().name() + "', '" +
                        player.getTeam().getColor() + "');");

                // resources
                for (final Resource resource : player.getResources().keySet()) {
                    update("INSERT INTO resources " +
                            "(id, player, amount) VALUES " +
                            "('" + resource.getId() + "', '" +
                            player.getPlayer().getUniqueId() + "', " +
                            player.getResource(resource) + ");");
                }

                // buildings
                for (final GameBuilding building : player.getBuildings()) {
                    update("INSERT INTO buildings " +
                            "(uuid, player, x, y, z, building, health, build_date, repair_date, spawn_x, spawn_y, spawn_z, world) VALUES " +
                            "('" + building.getUuid() + "', '" +
                            player.getPlayer().getUniqueId() + "', " +
                            building.getLocation().getX() + ", " +
                            building.getLocation().getY() + ", " +
                            building.getLocation().getZ() + ", '" +
                            building.getBuilding().getId() + "', " +
                            building.getHealth() + ", " +
                            building.getBuildDate() + ", " +
                            building.getRepairDate() + ", " +
                            building.getTroopSpawnLocation().getX() + ", " +
                            building.getTroopSpawnLocation().getY() + ", " +
                            building.getTroopSpawnLocation().getZ() + ", '" +
                            building.getLocation().getWorld().getName() + "');");
                }

                // team
                update("INSERT INTO teams " +
                        "(color, game) VALUES " +
                        "('" + player.getTeam().getColor() + "', '" +
                        game.getId() + "');");

                // troops
                for (final GameTroop troop : player.getTroops()) {
                    update("INSERT INTO troops " +
                            "(id, player, x, y, z, world, troop, health, mana, state, building) VALUES " +
                            "('" + troop.getUuid() + "', '" +
                            player.getPlayer().getUniqueId() + "', " +
                            troop.getEntity().getLocation().getX() + ", " +
                            troop.getEntity().getLocation().getY() + ", " +
                            troop.getEntity().getLocation().getZ() + ", '" +
                            troop.getEntity().getLocation().getWorld().getName() + "', '" +
                            troop.getTroop().getId() + "', " +
                            troop.getHealth() + ", " +
                            troop.getMana() + ", '" +
                            troop.getState().name() + "', '" +
                            troop.getCreator().getUuid() + "');");

                    final TroopTask task = troop.getCurrentTask();
                    if (task != null) {
                        update("INSERT INTO troops_tasks " +
                                "(id, troop, task) VALUES " +
                                "('" + UUID.randomUUID() + "', '" +
                                troop.getUuid() + "', '" +
                                task.getId() + "');");
                    }
                }
            }

            // game
            update("INSERT INTO games " +
                    "(id) VALUES " +
                    "('" + game.getId() + "');");
        }

    }

    public void loadGames() {

        try (final ResultSet games = query("SELECT * FROM games;")) {
            if (games == null) {
                warcrafted.getLogger().severe("Unable to load games from the database! (games == null)");
                return;
            }

            while (games.next()) {

                final String gameId = games.getString("id");
                final Game game = new Game(gameId, warcrafted);

                // Load teams
                final Map<String, GameTeam> availableTeams = new HashMap<>();
                try (final ResultSet teams = query("SELECT * FROM teams WHERE game = '" + gameId + "';")) {
                    if (teams == null) {
                        warcrafted.getLogger().severe("Unable to load teams from the database! (teams == null)");
                        return;
                    }

                    while (teams.next()) {
                        final String color = teams.getString("color");
                        availableTeams.put(color, new GameTeam(game, color));
                    }
                }

                // Load players
                final Map<UUID, GamePlayer> availablePlayers = new HashMap<>();
                try (final ResultSet players = query("SELECT * FROM players WHERE game = '" + gameId + "';")) {
                    if (players == null) {
                        warcrafted.getLogger().severe("Unable to load players from the database! (players == null)");
                        return;
                    }

                    while (players.next()) {
                        final UUID uuid = UUID.fromString(players.getString("uuid"));
                        final Race race = warcrafted.getRaceManager().getEntityById(players.getString("race"));
                        final Difficulty difficulty = Difficulty.valueOf(players.getString("difficulty"));
                        final GameTeam team = availableTeams.get(players.getString("team"));
                        final OfflinePlayer player = warcrafted.getServer().getOfflinePlayer(uuid);

                        if (race == null || team == null) {
                            warcrafted.getLogger().severe("Unable to load player's data from the database! (race == null or team == null)");
                            return;
                        }

                        // Resources
                        final Map<Resource, Integer> resources = new HashMap<>();
                        try (final ResultSet resourcesSet = query("SELECT * FROM resources WHERE player = '" + uuid + "';")) {
                            if (resourcesSet == null) {
                                warcrafted.getLogger().severe("Unable to load player's resources from the database! (resourcesSet == null)");
                                return;
                            }

                            while (resourcesSet.next()) {
                                final Resource resource = warcrafted.getResourceManager().getEntityById(resourcesSet.getString("id"));
                                if (resource == null) {
                                    warcrafted.getLogger().severe("Unable to load player's resources from the database! (resource == null)");
                                    return;
                                }

                                resources.put(resource, resourcesSet.getInt("amount"));
                            }
                        }

                        final GamePlayer gamePlayer =
                                new GamePlayer(game, player, race, difficulty, resources, new ArrayList<>(), new ArrayList<>(), team);
                        GamesManager.addPlayer(gamePlayer);

                        // Buildings
                        final List<GameBuilding> buildings = new ArrayList<>();
                        try (final ResultSet buildingsSet = query("SELECT * FROM buildings WHERE player = '" + uuid + "';")) {
                            if (buildingsSet == null) {
                                warcrafted.getLogger().severe("Unable to load player's buildings from the database! (buildingsSet == null)");
                                return;
                            }

                            while (buildingsSet.next()) {
                                final Building building = warcrafted.getBuildingManager().getEntityById(buildingsSet.getString("building"));
                                final World world = warcrafted.getServer().getWorld(buildingsSet.getString("world"));
                                final UUID buildingUuid = UUID.fromString(buildingsSet.getString("uuid"));

                                final Location location = new Location(world, buildingsSet.getDouble("x"), buildingsSet.getDouble("y"), buildingsSet.getDouble("z"));
                                final Location troopSpawnLocation = new Location(world, buildingsSet.getDouble("spawn_x"), buildingsSet.getDouble("spawn_y"), buildingsSet.getDouble("spawn_z"));

                                if (building == null) {
                                    warcrafted.getLogger().severe("Unable to load player's buildings from the database! (building == null)");
                                    return;
                                }

                                final GameBuilding gameBuilding = new GameBuilding(gamePlayer, building, location, true);

                                gameBuilding.setTroopSpawnLocation(troopSpawnLocation);
                                gameBuilding.setHealth(buildingsSet.getInt("health"));
                                gameBuilding.setBuildDate(buildingsSet.getLong("build_date"));
                                gameBuilding.setRepairDate(buildingsSet.getLong("repair_date"));
                                gameBuilding.setUuid(buildingUuid);

                                buildings.add(gameBuilding);
                                GamesManager.addBuilding(gameBuilding);
                            }

                            gamePlayer.getBuildings().addAll(buildings);
                        }

                        // Troops
                        final List<GameTroop> troops = new ArrayList<>();
                        try (final ResultSet troopsSet = query("SELECT * FROM troops WHERE player = '" + uuid + "';")) {
                            if (troopsSet == null) {
                                warcrafted.getLogger().severe("Unable to load player's troops from the database! (troopsSet == null)");
                                return;
                            }

                            while (troopsSet.next()) {
                                final Troop troop = warcrafted.getTroopManager().getEntityById(troopsSet.getString("troop"));
                                final World world = warcrafted.getServer().getWorld(troopsSet.getString("world"));
                                final UUID buildingUuid = UUID.fromString(troopsSet.getString("building"));

                                final Location location = new Location(world, troopsSet.getDouble("x"), troopsSet.getDouble("y"), troopsSet.getDouble("z"));

                                if (troop == null) {
                                    warcrafted.getLogger().severe("Unable to load player's troops from the database! (troop == null)");
                                    return;
                                }

                                final GameBuilding building = buildings.stream().filter(b -> b.getUuid().equals(buildingUuid)).findFirst().orElse(null);
                                if (building == null) {
                                    warcrafted.getLogger().severe("Unable to load player's troops from the database! (building == null)");
                                    return;
                                }
                                final GameTroop gameTroop = new GameTroop(game, gamePlayer, troop, location, building);

                                gameTroop.setHealth(troopsSet.getInt("health"));
                                gameTroop.setMana(troopsSet.getInt("mana"));
                                gameTroop.setState(GameTroop.TroopState.valueOf(troopsSet.getString("state")));

                                // Task
                                final ResultSet taskSet = query("SELECT * FROM troops_tasks WHERE troop = '" + gameTroop.getUuid() + "';");
                                if (taskSet != null) {
                                    final String taskId = taskSet.getString("task");
                                    final TroopTask task = TroopTask.getTaskById(taskId);
                                    if (task != null) {
                                        gameTroop.setCurrentTask(task);
                                    }
                                }

                                troops.add(gameTroop);
                                GamesManager.addTroop(gameTroop);
                            }

                            gamePlayer.getTroops().addAll(troops);
                        }

                        availablePlayers.put(uuid, gamePlayer);
                    }

                    for (final GamePlayer player : availablePlayers.values())
                        game.addPlayer(player);

                    GamesManager.addGame(game);
                    warcrafted.getLogger().info("Game '" + gameId + "' loaded!");
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void reset() {
        try {
            this.connection.createStatement().execute("DROP TABLE IF EXISTS games;");
            this.connection.createStatement().execute("DROP TABLE IF EXISTS players;");
            this.connection.createStatement().execute("DROP TABLE IF EXISTS teams;");
            this.connection.createStatement().execute("DROP TABLE IF EXISTS resources;");
            this.connection.createStatement().execute("DROP TABLE IF EXISTS buildings;");
            this.connection.createStatement().execute("DROP TABLE IF EXISTS troops;");

            checkTables();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void update(String query) {
        try {
            this.connection.createStatement().execute(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ResultSet query(String query) {
        try {
            return this.connection.createStatement().executeQuery(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

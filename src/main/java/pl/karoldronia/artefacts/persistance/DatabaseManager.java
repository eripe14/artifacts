package pl.karoldronia.artefacts.persistance;

import com.zaxxer.hikari.HikariConfig;
import eu.okaeri.configs.json.simple.JsonSimpleConfigurer;
import eu.okaeri.configs.serdes.commons.SerdesCommons;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import eu.okaeri.persistence.PersistencePath;
import eu.okaeri.persistence.document.DocumentPersistence;
import eu.okaeri.persistence.jdbc.MariaDbPersistence;
import org.bukkit.plugin.Plugin;
import pl.karoldronia.artefacts.config.impl.PluginConfig;

public class DatabaseManager {

    private final Plugin plugin;
    private final PluginConfig pluginConfig;

    public DatabaseManager(Plugin plugin, PluginConfig pluginConfig) {
        this.plugin = plugin;
        this.pluginConfig = pluginConfig;
    }

    public DocumentPersistence connect() {
        HikariConfig mysqlHikari = new HikariConfig();
        mysqlHikari.setJdbcUrl(this.pluginConfig.storage.url);

        PersistencePath basePath = PersistencePath.of(this.pluginConfig.storage.prefix);

        return new DocumentPersistence(
                new MariaDbPersistence(
                        basePath,
                        mysqlHikari
                ),
                JsonSimpleConfigurer::new,
                new SerdesCommons(),
                new SerdesBukkit()
        );
    }


}
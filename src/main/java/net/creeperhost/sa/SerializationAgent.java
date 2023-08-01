package net.creeperhost.sa;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.instrument.Instrumentation;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by covers1624 on 31/7/23.
 */
public class SerializationAgent {

    private static final Gson GSON = new Gson();

    public static boolean DEBUG = false;
    public static boolean OFFLINE = false;
    @Nullable
    public static Config CONFIG;

    public static final Set<String> toTransform = new HashSet<>();
    public static final Set<String> allowedClasses = new HashSet<>();
    public static final List<String> allowedPackages = new ArrayList<>();

    public static void premain(@Nullable String agentArgs, Instrumentation inst) {
        if (agentArgs != null) {
            for (String arg : agentArgs.split(",")) {
                if (arg.equals("debug")) {
                    DEBUG = true;
                } else if (arg.equals("offline")) {
                    OFFLINE = true;
                }
            }
        }
        CONFIG = loadConfig();
        if (CONFIG == null) {
            Logger.info("Did not load a config. No patches will be applied.");
            return;
        }
        allowedClasses.addAll(CONFIG.classAllowlist);
        allowedPackages.addAll(CONFIG.packageAllowlist);
        for (Config.PatchModule patchModule : CONFIG.patchModules) {
            toTransform.addAll(patchModule.classesToPatch);
            allowedClasses.addAll(patchModule.classAllowlist);
            allowedPackages.addAll(patchModule.packageAllowlist);
        }

        Logger.debug("Identified " + toTransform.size() + " classes to try and transform.");
        Logger.debug("Identified " + allowedClasses.size() + " allowed classes.");
        Logger.debug("Identified " + allowedPackages.size() + " allowed package filters.");

        inst.addTransformer(new SerializationAgentTransformer());
    }

    @Nullable
    private static Config loadConfig() {
        Config config = null;
        try {
            config = loadLocalConfig();
        } catch (IOException ex) {
            Logger.error("Failed to load local config.", ex);
        }
        if (config == null && !OFFLINE) {
            try {
                config = loadRemoteConfig();
            } catch (IOException ex) {
                Logger.error("Failed to load remote config.", ex);
            }
        }
        if (config == null) {
            try {
                config = loadBuiltinConfig();
            } catch (IOException ex) {
                Logger.error("Failed to load local config.", ex);
            }
        }
        return config;
    }

    @Nullable
    private static Config loadLocalConfig() throws IOException {
        Path local = Paths.get("serializationisbad.json");
        Logger.debug("Trying to load local config from " + local.toAbsolutePath());

        if (Files.notExists(local)) {
            Logger.debug(" Does not exist.");
            return null;
        }

        try (InputStream is = Files.newInputStream(local)) {
            return loadConfig(is);
        }
    }

    private static Config loadBuiltinConfig() throws IOException {
        Logger.debug("Trying to load baked-in config.");
        try (InputStream is = SerializationAgent.class.getResourceAsStream("serializationisbad.json")) {
            return loadConfig(is);
        }
    }

    private static Config loadRemoteConfig() throws IOException {
        Logger.debug("Trying to retrieve remote config...");
        URL url = new URL("https://raw.githubusercontent.com/dogboy21/serializationisbad/master/serializationisbad.json");

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            // 5 seconds seems reasonable, considering this will hang on startup if this request fails.
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            try (InputStream is = connection.getInputStream()) {
                return loadConfig(is);
            } catch (IOException ex) {
                Logger.error("Failed to request remote config.", ex);
                return null;
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static Config loadConfig(InputStream is) throws IOException {
        try {
            return GSON.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), Config.class);
        } catch (JsonParseException ex) {
            throw new IOException("Failed to parse json.", ex);
        }
    }
}

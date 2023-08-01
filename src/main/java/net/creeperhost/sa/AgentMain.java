package net.creeperhost.sa;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.jar.JarFile;

/**
 * Created by covers1624 on 24/1/23.
 */
public class AgentMain {

    public static void premain(@Nullable String args, Instrumentation inst) {
        // This is some black magic we need to do in order to juggle onto the correct class loader.
        // Vanilla creates a new classloader using the platform classloader as the parent ignoring the system classloader (which we are installed in).
        // In order for minecraft to be able to call our injections, and for us to have the same static state,
        // we need to append ourselves to the boostrap classpath and class load SerializationAgent on that class loader.
        File ourFile = getOurFile();
        if (ourFile == null) return;
        addToFabricSysLibraries(ourFile);

        try (JarFile file = new JarFile(ourFile)) {
//            System.out.println("[SerializationAgent AgentMain] Appending to boostrap classloader..");
            inst.appendToBootstrapClassLoaderSearch(file);
        } catch (IOException ex) {
            System.err.println("[SerializationAgent AgentMain] Failed to add jar to boostrap classloader. SerializationAgent disabled..");
            ex.printStackTrace(System.err);
            return;
        }

        try {
            ClassLoader platformClassLoader = ClassLoader.getSystemClassLoader().getParent();
//            System.out.println("[SerializationAgent AgentMain] Calling SerializationAgent premain..");
            Class<?> clazz = Class.forName("net.creeperhost.sa.SerializationAgent", true, platformClassLoader);
            Method method = clazz.getMethod("premain", String.class, Instrumentation.class);
            method.invoke(null, args, inst);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            System.err.println("[SerializationAgent AgentMain] Failed to reflect SerializationAgent.");
            ex.printStackTrace(System.err);
        }
    }

    @Nullable
    private static File getOurFile() {
        ProtectionDomain domain = SerializationAgent.class.getProtectionDomain();
        CodeSource source = domain.getCodeSource();
        if (source == null) {
            System.err.println("[SerializationAgent AgentMain] Failed to find our own code source. SerializationAgent disabled..");
            return null;
        }

        URL location = source.getLocation();
        if (location == null) {
            System.err.println("[SerializationAgent AgentMain] Failed to find our own location. SerializationAgent disabled..");
            return null;
        }

        URI uri;
        try {
            uri = location.toURI();
        } catch (URISyntaxException ex) {
            System.err.println("[SerializationAgent AgentMain] Failed to convert URL to URI. SerializationAgent disabled..");
            ex.printStackTrace(System.err);
            return null;
        }
        if (!"file".equals(uri.getScheme())) {
            System.err.println("[SerializationAgent AgentMain] SerializationAgent is not loaded from a file? SerializationAgent disabled..");
            return null;
        }
        return Paths.get(uri).toFile();
    }

    private static void addToFabricSysLibraries(File file) {
        String prop = System.getProperty("fabric.systemLibraries");
        prop = appendIfNotEmpty(prop, File.pathSeparator) + file.getAbsolutePath();
        System.setProperty("fabric.systemLibraries", prop);
    }

    private static String appendIfNotEmpty(@Nullable String str, String suffix) {
        if (str == null || str.isEmpty()) return "";
        return str + suffix;
    }
}

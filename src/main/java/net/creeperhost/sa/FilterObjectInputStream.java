package net.creeperhost.sa;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * Created by covers1624 on 31/7/23.
 */
public class FilterObjectInputStream extends ObjectInputStream {

    public FilterObjectInputStream(InputStream in) throws IOException {
        super(in);
    }

    public static boolean isTypeAllowed(String type) {
        if (type.charAt(0) == '[') {
            if (type.length() == 2) {
                // Will be primitive arrays, they use internal names. [I for example.
                return true;
            }
            return isTypeAllowed(type.substring(1));
        }
        if (type.charAt(0) == 'L' && type.charAt(type.length() - 1) == ';') {
            return isTypeAllowed(type.substring(1, type.length() - 1));
        }
        if (SerializationAgent.allowedClasses.contains(type)) {
            return true;
        }
        for (String allowedPackage : SerializationAgent.allowedPackages) {
            if (type.startsWith(allowedPackage + ".")) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        Logger.debug("Trying to load: " + desc.getName());
        if (!isTypeAllowed(desc.getName())) {
            throw new ClassNotFoundException("Class " + desc.getName() + " is not allowed to be deserialized.");
        }

        return super.resolveClass(desc);
    }
}

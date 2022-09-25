package discord.data.credential;

import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

public class CredentialManager {

    private static final Properties PROPERTIES = new Properties();

    static {
        final File file = new File("credentials.properties");

        if (!file.exists()) {
            LoggerFactory.getLogger(CredentialManager.class).error(file.getName() + " does not exist and cannot be loaded.");
        }

        try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            PROPERTIES.load(reader);
        } catch (IOException e) {
            LoggerFactory.getLogger(CredentialManager.class).error(file.getName() + " failed to load properly.");
        }
    }

    public static String getCredential(Credential key) {
        return PROPERTIES.getProperty(key.toString(), "");
    }

}

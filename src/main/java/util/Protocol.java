package util;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.nio.charset.StandardCharsets;

/**
 * Protocol - composant principal de l'application.
 */
public final class Protocol {
    private Protocol() {
        // Classe utilitaire statique: pas d'instance.
    }

    public static String encode(String value) {
        // Encode une chaine en Base64 pour eviter les problemes de separateur.
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    public static String decode(String value) {
        // Decode la valeur Base64 vers texte UTF-8.
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }

    public static String join(String... tokens) {
        // Construit une ligne protocole delimitee par '|'.
        return String.join("|", tokens);
    }

    public static List<String> split(String line) {
        // Decoupe une ligne protocole en tokens.
        String[] raw = line.split("\\|");
        List<String> out = new ArrayList<>(raw.length);
        for (String token : raw) {
            // Copie explicite dans une liste mutable.
            out.add(token);
        }
        return out;
    }
}


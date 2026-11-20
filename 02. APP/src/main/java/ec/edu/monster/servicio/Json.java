package ec.edu.monster.servicio;

import com.google.gson.JsonObject;

/** Utilidad para leer campos de texto opcionales sin NullPointerException. */
final class Json {

    private Json() {
    }

    static String texto(JsonObject o, String campo) {
        return o != null && o.has(campo) && !o.get(campo).isJsonNull() ? o.get(campo).getAsString() : "";
    }
}

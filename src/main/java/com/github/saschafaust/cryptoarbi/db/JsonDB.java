package com.github.saschafaust.cryptoarbi.db;

import io.jsondb.JsonDBTemplate;

/**
 * Filesystem Database Implementation
 */
public class JsonDB {

    private static final class InstanceHolder {
        private static final String dbFilesLocation = "src/main/ressources/dbFiles";
        private static final String baseScanPackage = "com.github.saschafaust.cryptoarbi.db.vo";
        static final JsonDBTemplate INSTANCE = new JsonDBTemplate(dbFilesLocation, baseScanPackage);
    }

    /**
     * Gets the Singleton
     * 
     * @return JsonDBTemplate
     */
    public static JsonDBTemplate instance() {
        return InstanceHolder.INSTANCE;
    }

    private JsonDB() {
        // prevent instatiation
    }

}

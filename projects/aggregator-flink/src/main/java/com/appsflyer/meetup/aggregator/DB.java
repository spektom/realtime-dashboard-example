package com.appsflyer.meetup.aggregator;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.gen.exc.ReqlOpFailedError;
import com.rethinkdb.model.MapObject;
import com.rethinkdb.net.Connection;

import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DB {

    /**
     * Create database with all needed tables
     */
    public static void initialize() throws TimeoutException {
        RethinkDB r = RethinkDB.r;
        Connection conn = r.connection().connect();
        try {
            //r.dbDrop("af").run(conn)
            r.dbCreate("af").run(conn);
            r.db("af").tableCreate("geo_stats").optArg("primary_key", "appId").run(conn);
            r.db("af").tableCreate("total_stats").optArg("primary_key", "appId").run(conn);
        } catch (ReqlOpFailedError e) {
            e.printStackTrace();
            ;
        } finally {
            conn.close(false);
        }
    }

    /**
     * Accepts statistics in format: appId -> {measurement1 -> count, measurement2 -> count, ...}
     *
     * @param table Target table
     * @param stats Statistics per application
     */
    public static void insertStats(String table, Map<String, Map<String, Integer>> stats) throws TimeoutException {
        RethinkDB r = RethinkDB.r;
        Connection conn = r.connection().connect();
        try {
            r.db("af").table(table).insert(
                    stats.entrySet().stream().map(new Function<Map.Entry<String, Map<String, Integer>>, MapObject>() {
                        @Override
                        public MapObject apply(Map.Entry<String, Map<String, Integer>> e) {
                            return r.hashMap("appId", e.getKey())
                                    .with("stats", e.getValue());
                        }
                    }).collect(Collectors.toList())
            ).optArg("conflict", "replace").run(conn);
        } finally {
            conn.close(false);
        }
    }

}

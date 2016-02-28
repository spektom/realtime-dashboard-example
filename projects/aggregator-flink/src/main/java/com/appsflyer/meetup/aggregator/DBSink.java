package com.appsflyer.meetup.aggregator;

import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Aggregates values into a stats structure, and flushes it to a DB
 */
public class DBSink implements SinkFunction<Tuple3<String, String, Integer>> {

    private static final Logger LOGGER = Logger.getLogger(DBSink.class);

    private long lastInvoke = Long.MAX_VALUE;
    private Map<String, Map<String, Integer>> stats = new HashMap<>();
    private String tableName;

    public DBSink(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public void invoke(Tuple3<String, String, Integer> value) throws Exception {
        long now = System.currentTimeMillis();

        // Check if we are out of the aggregation window:
        if (now - lastInvoke > 1000) {
            LOGGER.debug("Flushing " + tableName + " to DB");
            LOGGER.trace(stats);

            DB.insertStats(tableName, stats);
            stats.clear();
        }

        Map<String, Integer> statsByApp = stats.get(value.f0);
        if (statsByApp == null) {
            statsByApp = new HashMap<>();
            stats.put(value.f0, statsByApp);
        }
        statsByApp.put(value.f1, value.f2);
        lastInvoke = now;
    }
}

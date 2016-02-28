package com.appsflyer.meetup.aggregator;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer082;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Aggregator {

    private static DataStream<String> createKafkaStream(StreamExecutionEnvironment env) throws UnknownHostException {
        Properties kafkaProperties = new Properties();

        kafkaProperties.put("zookeeper.connect", "kafka.appsflyer.com:2181");
        kafkaProperties.put("bootstrap.servers", "kafka.appsflyer.com:9092");
        kafkaProperties.put("group.id", "installs-" + UUID.randomUUID());
        kafkaProperties.put("auto.offset.reset", "latest");

        return env
                .addSource(new FlinkKafkaConsumer082<>("installs", new SimpleStringSchema(), kafkaProperties));
    }

    private static void findTopCountries(DataStream<Event> stream) {
        stream
                .map(new MapFunction<Event, Tuple3<String, String, Integer>>() {
                    @Override
                    public Tuple3<String, String, Integer> map(Event event) throws Exception {
                        return Tuple3.of(event.appId, event.country, 1);
                    }
                })
                // Redistribute by app_id + country:
                .keyBy(0, 1)
                .timeWindow(Time.of(2, TimeUnit.SECONDS))
                // Sum third column:
                .sum(2)
                // Redistribute by app_id:
                .keyBy(0)
                .addSink(new DBSink("geo_stats"));
    }

    private static void calcTotalInstalls(DataStream<Event> stream) {
        stream
                .map(new MapFunction<Event, Tuple3<String, String, Integer>>() {
                    @Override
                    public Tuple3<String, String, Integer> map(Event event) throws Exception {
                        return Tuple3.of(event.appId, "installs", 1);
                    }
                })
                .keyBy(0, 1)
                .timeWindow(Time.of(2, TimeUnit.SECONDS))
                // Sum third column:
                .sum(2)
                .addSink(new DBSink("total_stats"));
    }

    public static void main(String[] args) throws Exception {
        DB.initialize();

        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.IngestionTime);
        env.enableCheckpointing(100000, CheckpointingMode.EXACTLY_ONCE);
        env.setParallelism(2);

        DataStream<String> kafkaStream = createKafkaStream(env);
        DataStream<Event> eventsStream = kafkaStream.map(new Event.Parser()).filter(event -> event != null);

        findTopCountries(eventsStream);
        calcTotalInstalls(eventsStream);

        env.execute("Aggregator");
    }
}

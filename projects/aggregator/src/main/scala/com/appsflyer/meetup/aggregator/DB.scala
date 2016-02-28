package com.appsflyer.meetup.aggregator

import com.rethinkdb.RethinkDB
import com.rethinkdb.gen.exc.ReqlOpFailedError
import scala.collection.JavaConverters._

object DB {

  /**
    * Create database with all needed tables
    */
  def initialize(): Unit = {
    val r = RethinkDB.r
    val conn = r.connection().connect()
    try {
      r.dbDrop("af").run(conn)
      r.dbCreate("af").run(conn)
      r.db("af").tableCreate("geo_stats").optArg("primary_key", "appId").run(conn)
      r.db("af").tableCreate("total_stats").optArg("primary_key", "appId").run(conn)
      r.db("af").tableCreate("device_stats").optArg("primary_key", "appId").run(conn)
    } catch {
      case x: ReqlOpFailedError => x.printStackTrace()
    } finally {
      conn.close(false)
    }
  }

  /**
    * Accepts statistics in format: appId -> {measurement1 -> count, measurement2 -> count, ...}
    * @param table Target table
    * @param stats Statistics per application
    */
  def insertStats(table: String, stats: Map[String, Map[String, Int]]): Unit = {
    val r = RethinkDB.r
    val conn = r.connection().connect()
    try {
      r.db("af").table(table).insert(
        r.array(stats.map { s =>
          r.hashMap("appId", s._1)
            .`with`("stats", s._2.asJava)
        }.toArray: _*)
      ).optArg("conflict", "replace").run(conn)
    } finally {
      conn.close(false)
    }
  }
}

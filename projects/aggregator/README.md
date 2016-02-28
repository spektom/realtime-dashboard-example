af-conveyor-scala
==================

The goal of this service is to store pre-aggregated (counted) events coming from Kafka into a DB, while preserving exactly once semantics.
There are several aggregation frameworks provided, which use different methodologies. Some of them are provided just for the reference.

To learn more about them click on the links below.


Method                                                       | Description
-------------------------------------------------------------|----------------------------------------------------------------------------------
[Snapshotting Aggregation Framework](docs/snapshotting.md)   | Aggregation using two processes: one creating snapshots and another updating DB
[Transactional Aggregation Framework](docs/transactional.md) | Aggregation using SQL transaction mechanism


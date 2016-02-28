coding-with-af-spark-streaming
===============================

Spark Streaming makes it easy to build scalable fault-tolerant streaming applications. 

At AppsFlyer, we use Spark for many of our offline processing services. Spark Streaming joined our technology stack a few months ago for real-time work flows, reading directly from Kafka to provide value to our clients in near-real-time. 

In this session we will code a real-time dashboard application based on Spark Streaming technology. You will learn how to collect events from Kafka, aggregate them by time window and present aggregated insights in a dashboard. 

## Screenshot

![Screenshot](https://github.com/spektom/coding-with-af-spark-streaming/raw/master/screenshot.gif)

## Architecture

The following scheme presents what we are going to create in this session:


                    Read events
     +------------+              +------------------------+
     |            |              |                        |
     |  Kafka     | -----------> |   Spark Streaming Job  |
     |            |              |                        |
     +------------+              +------------------------+
                                             |
                                             | Update
                                             V                   +---------------+
                                    +--------+------+            |               |
                                    |               |   Push!    |   Real-time   |
                                    |   RethinkDB   | ---------> |   Dashboard   |
                                    |               |            |       _..     |
                                    +---------------+            |   (../   \)   |
                                                                 +---------------+



1. We'll read real-time events from Kafka queue.
2. We'll aggregate events in Spark streaming job by some sliding window.
3. We'll write aggregated events to RethinkDB.
4. RethinkDB will push updates to the real-time dashboard.
5. Real-time dashboard will present current status.


## Prerequisites

1. Laptop with at least 4GB of RAM.
2. Make sure Virtualization is enabled in BIOS.
3. [VirtualBox v5.x](https://www.virtualbox.org/wiki/Downloads) or greater installed.
4. [Vagrant v1.8.1](https://www.vagrantup.com/downloads.html) or greater installed.


## Preparation & Setup

Please do these steps **prior** to coming to the Meetup:

1. Clone or download this repository.
2. Run `vagrant up && vagrant halt` inside the `vagrant/` directory.

Please be patient, this process may take some time...


## Running the Application

1. Go inside `vagrant/` directory.

2. Boot the Vagrant virtual machine:

     `vagrant up`

3. Start the Spark Streaming process:

     `vagrant ssh -c /projects/aggregator/run.sh`

4. Start the Dashboard application in another terminal window:

     `vagrant ssh -c /projects/dashboard/run.sh`


If everything was successful, you should be able to access your Dashboard application here:

[http://192.168.10.16:3000](http://192.168.10.16:3000)


## Cleaning Up

After the meetup, you may want to close running virtual machine. To do so, run inside `vagrant/` directory:

     vagrant suspend

To destroy the virtual machine completely, run:

     vagrant destroy



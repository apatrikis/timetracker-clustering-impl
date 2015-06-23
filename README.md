# Time Tracker
Time Tracker is a self educational application for timesheet tracking. The application mainly has three parts:
- a JEE7 REST server, connecting to a database for storing users, projects and timesheets, as well as an WebSocket interface to notify registered clients about status changes.
- a common library with interfaces, entities and exceptions.
- a AngularJS web client that is consuming the servers's REST API and registers for status changes.

# Time Tracker - Clustering IMPL
## Main desicions
During the creation and modification of `TimeRecord`s a notification message is created. A notification is to be send to a receiver.
The messaging between the server and the client is handeled in `Timetracker Websocket`.

In case a cluster is used, the messaging must span over all server instances.

Much like `Timetracker Websocket`, `Event`s are use for decoupling this `Clustering API` from other functionalities.
The `Timetracker Clustering` receives events, and uses a `JMS Topic` for broadcasting messages into the cluster.

## Used software and versions
- IDE : Oracle NetBeans 8.0.2 (https://netbeans.org/downloads/, choose the `Java EE` edition)

The IDE is allready installed for the development of `Timetracker Server`.

For the following descriptions the base installation directory `\time-tracker` is assumed.

## Development
The directory structure described below is like this:
```
\time-tracker
   \timetracker-clustering-impl
```

### Requirements
1. Download the `timetracker-clustering-impl` project from https://github.com/apatrikis/timetracker-clustering-impl

In case NetBeans IDE is not already installed refer to `timetracker-server`.


### Initial configuration
Two `JMS` appilcation server resources are used:
- JMS connection factory
  The `Glassfish` application server comes with a `java:comp/DefaultJMSConnectionFactory`.
- JMS Topic
  For sending and receiving messages, the `Topic` `jms/ttTopicMessageForUser` is used.

For development purpose, the `jms/ttTopicMessageForUser` resource is ceated with the `glassfish-resources.xml` file which comes with the `timetracker-server` project.
A manual creation with the `asadmin` tool is described below.

## Test
Currently there are no `Unit test` for this project.
The classes are involved in test for the `timetracker-server`.

```
asadmin --user admin --host localhost --port 14848 create-admin-object --restype javax.jms.Topic --raname jmsra --description "TimeTracker topic for distributing a message in a Glassfish cluster." --property Name=ttTopicMessageForUser jms/ttTopicMessageForUser
```

Note that the port of the `Arquillian` `Glassfish Application Server` is `--port 14848`.

## Build (CI)
For `CI` the `Maven POM` is to be used within `Jenkins`.
For more information see the `timetracker-server` project.

## Run
As described for testing, the `JMS Topic` needs to be created, this time for the cluster.

```
asadmin --user admin --host localhost --port 4848 create-admin-object --target tt-cluster-1 --restype javax.jms.Topic --raname jmsra --description "TimeTracker topic for distributing a message in a Glassfish cluster." --property Name=ttTopicMessageForUser jms/ttTopicMessageForUser
```

Instead of `create-admin-object` one could also use `create-jms-resource`.

The packaging type of this project is `JAR`.
In case the messaging for the `timetracker-server` should be available in a clusteres envronment the artifact needs to be included in the `timetracker-server` artifact.

To achieve this the project has to be added as a `runtime` dependendy in the `timetracker-server` `pom`.

```
<dependency>
    <groupId>com.prodyna.pac</groupId>
    <artifactId>timetracker-clustering-impl</artifactId>
    <version>1.0-SNAPSHOT</version>
    <scope>runtime</scope>
</dependency>
```

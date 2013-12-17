# CMF Global Topology Extension
-----------------------------

This is used in conjunction with the Event Bus to enable dynamically routed, resilient clients through the manipulation of AMQP Brokers and Topology information.

## Quick Start

Coming Soon.


## Global Topology Service (GTS) Documentation

The following documentation describes how to build, configure and implement the Global Topology Service.  The Global Topology Service is a very light web service (Dropwizard) CMF-AMQP clients can contact (via REST) to retrieve topology configuration specific to them.  The GTS has some management facilities for making the configuration of clients dynamic.

> This tutorial is design for users who may not have a Java background, so if you are a Java developer, please know that it is not our intention to insult your intelligence/experience.

### Building GTS

Once the dependencies are installed, building the Global Topology Service is pretty easy:

(run this command from the AMP/services/global-topology-service directory)
```bash
mvn package
```

The Maven Shade plugin will (very verbosely) squawk at you about having "Duplicate classes" in the "fat jar" it's building.  It's best to simply `mvn clean` the project before packaging.

If you want to run the project right after building it, there's a simple BASH script located in the root of the project directory that will `mvn clean`, `mvn package` and then start the server with a default configuration file.  

You can execute it with:

```bash
sh start-server.sh
```

### Running the GTS

Assuming you have performed `mvn package` and have a "Fat JAR" (located in the target directory of your Maven project), you can start the server using the following command:

```bash
java -jar target/gts-{{version}}.jar server {{yaml config file}}
```
For instance...
```bash
java -jar target/gts-1.0-SNAPSHOT.jar server configuration/gts.yaml
```

- //TODO: Get an awesome admin like 'Chuck' to make this a REHL service (via install script). If only it was as easy as `mvn chuck`.

## Configuration

The greatest amount of complexity with the GTS is understanding how to configure it (since there is a bit of configuration).

Please refer to the [configuration](https://github.com/Berico-Technologies/CMF-Global-Topology/tree/master/configuration) directory, which contains sample Dropwizard `yaml` configurations and Spring Context files.

There are two primary components to configuring the Global Topology Service:
* Dropwizard `yaml` file
* Spring context files

### Dropwizard `yaml` Configuration
-----------------------------

The GTS implementation of Dropwizard does not heavily utilize the `yaml` file, mostly because it lacks the sort of flexibility we needed to support a myriad of deployment environments.  The `yaml` configuration, does however, provide most of the Dropwizard specific configuration, which includes:

* Logging
* SSL
* HTTP Settings
* Location of static web server assets

For documentation on these settings, please refer to the Dropwizard project:  http://dropwizard.codahale.com/.

We will discuss some common Dropwizard-specific tasks:

* Changing the Admin Port password
* Enabling SSL

We have extended the configuration to also include:

* Location of Spring Context file
* Ability to turn on Spring Security as an Auth/Auth provider

> If you are familiar with Dropwizard, it's important to note that we don't use pure Dropwizard, but rather a custom service extension based on Spring.  You can find that library here: https://github.com/Berico-Technologies/Dropwizard-Utils

#### Changing the Admin Port password

The Admin Port is a separate web application launched on port 8081.  It can be protected by Basic Auth, which is recommended, since it contains sensitive metrics and the ability to manipulate certain aspects of the server.

To enable Basic Auth on the Admin Port, specific the `adminUsername` and `adminPassword` under the `http` settings:
```yaml
http:
    adminUsername: admin
    adminPassword: admin123
```

To remove Basic Auth on the Admin Port, remove the `adminUsername` and `adminPassword` settings.

#### Enabling SSL

To enable SSL on Dropwizard, you first need to generate Certificates.  Hopefully your organization already has a Certificate Authority and the ability to generate these certs for you.  If you need some certs for development/test, or simply need to roll your own CA, please refer to the excellent documentation on the Jetty website (parts 1-3b) which detail how to do this: http://docs.codehaus.org/display/JETTY/How+to+configure+SSL.

Assuming you have generated the certificates, you can enable SSL by adding the following settings under `http` in the `yaml` configuration file:

```yaml
http:
    connectorType: nonblocking+ssl
    ssl:
        keyStore: ./ssl/keystore.jks
        keyStorePassword: password
        trustStore: ./ssl/keystore.jks
        trustStorePassword: password
        needClientAuth: true
```

The following is an explanation of each of the properties:

*  **connectorType** - The type of Jetty connector to use.  The default is `blocking`, used for short duration, non-SSL requests.  Dropwizard also supports the following non-SSL implementations: `non-blocking` and `legacy`.  For SSL connections, you must use either `nonblocking+ssl` or `legacy+ssl`.
*  **keyStore** - The certificate key store for the server.
*  **keyStorePassword** - Password to unlock the keystore.
*  **trustStore** - The store for trusted certificates.
*  **trustStorePassword** - Password to unlock the trust store.
*  **needClientAuth** - This forces the client certificate to be authenticated against the trust store.

A much more comprehensive list of SSL settings can be found here: https://github.com/codahale/dropwizard/blob/master/docs/source/manual/core.rst

#### Location of the Spring context file

In order to run the GTS, you need to provide a Spring context file with the associated resources preconfigured.  We provide a number of example context files in the [configuration](https://github.com/Berico-Technologies/CMF-Global-Topology/tree/master/configuration) directory.

*We do not provide a default implementation of the GTS.  You will need to modify or clone one of these implementations to suit your needs*

To change the location of the Spring context file, edit the `yaml` file and provide the location relative to the working directory you executed the service from:

```yaml
applicationContext: configuration/mongoContext.xml
```

This context file represents the "root" Spring context.  From this file you should load any auxiliary configuration files you may need.  We've separated a number of the service functions into separate files (please see the section on Spring Context for more details).

#### Turn On/Off Spring Security

Spring Security is far more flexible and capable that Dropwizard's authentication and authorization facilities.  Berico Technologies has created a Dropwizard extension that injects the Spring Security Filter Chain into the underlying Jetty container to enable these more advanced features.  You can find more details on this plugin at the following project:  https://github.com/Berico-Technologies/Dropwizard-Utils.

To enable or disable Spring Security, edit the `yaml` file specifying whether Spring Security should be used:

```yaml
useSpringSecurity: true
```

> By enabling Spring Security, this assumes that you have configured Spring Security in your Spring Context file.

### Spring Context Configuration
-----------------------------

The majority of GTS-related configuration settings are found in Spring context files.  If you are unfamiliar with the Spring Framework, there is a really easy tutorial (2 minutes) here:  http://www.mkyong.com/spring3/spring-3-hello-world-example/

The GTS uses Berico's custom `SpringService` implementation of the Dropwizard `Service`.  Using `SpringService`,  we define our Dropwizard components in XML (and not programmatically, which is how it is typically done in Dropwizard).  This means our REST resources, Tasks, Health-Checks, etc. are defined in Spring.

There are a few essential components defined in Spring that you need to be aware of:

* **topologyService** (cmf.topology.core.GlobalTopologyService) - implementation of the ITopologyService (but at the server) providing the content to the REST endpoint clients query for topology information.
* **topologyRepository** (an implementation of cmf.topology.core.ITopologyRepository) - a bean must be specified in the configuration providing an implementation of that `ITopologyRepository` interface.  The GTS comes with both an `InMemoryTopologyRepository` and a `MongoTopologyRepository`.
* **topologyServiceResource** (cmf.topology.resources.TopologyServiceResource) - a JAX-RS (REST) provider for the `GlobalTopologyService`; this is the endpoint clients will connect to.

We also provide a set of CRUD interfaces for the Topology Repository:

* **exchangeResource** (cmf.topology.resources.ExchangeResource) - CRUD for ExtendedExchange domain object, which adds a couple of additional properties to the cmf.rabbit.topology.Exchange.
* **routeInfoResource** (cmf.topology.resources.RouteInfoResource) - CRUD for ExtendedRouteInfo domain object, which adds a couple of additional properties to the cmf.rabbit.topology.RouteInfo.

And we also offer the same CRUD interfaces for Ember Data (for our forthcoming GTS UI):

* **emExchangeResource** (cmf.topology.resources.EmExchangeResource) - identical to "exchangeResource", but compatible with Ember Data which requires the payload to be output in a special way.
* **emRouteInfoResource** (cmf.topology.resources.EmRouteInfoResource) - identical to "routeInfoResource", but compatible with Ember Data which requires the payload to be output in a special way.

Dropwizard also has the concept of "Health Checks" which you lookup (via web-RPC) to see if the service is functioning OK.  GTS provides the following Health Checks:

* **repositoryHealthCheck** (cmf.topology.health.RepositoryHealthCheck) - checks to see if the Repository is alive.







## Global Topology Client Documentation

To utilize the Global Topology Service as a centralized provider for Topology Information, CMF Clients can utilize ITopologyService implementations found in this repository.  These clients will call the GTS when the Transport provider encounters an Event not found in it's `RouteCache`.




# KotlinInventoryService
This was an experimental project for me to play with Dropwizard and Kotlin.

Toy microservice that models inventory information in a supply chain context.

I abandoned my work with Javalin and switched to Dropwizard https://bitbucket.org/honstain/javalin-inventory-service-v2/src/master/

## Dependencies
### Dropwizard
Version 2.0.23
Java framework for high-performance RESTful web services.
* https://www.dropwizard.io/en/latest/
* https://github.com/dropwizard/dropwizard

### Dropwizard Metrics
I use the Dropwizard Metrics library to report to Graphite
* https://www.dropwizard.io/projects/metrics/en/latest/manual/core.html
* https://www.dropwizard.io/projects/metrics/en/latest/manual/jersey.html
* https://metrics.dropwizard.io/4.2.0/manual/graphite.html#manual-graphite

There is a grafana dashboard record in grafanaDashboard.json that you can use to recreate the dashboard. 
This was the guide I used to run Grafana locally (Grafana and Graphite via docker-compose) https://www.linode.com/docs/guides/install-graphite-and-grafana/

### Kafka
This uses the Dropwizard version of the Kafka client that utilizes the Apache Kafka client.
* https://github.com/dropwizard/dropwizard-kafka

**WARNING** The integration test assumes there is a Kafka cluster to interact with, if you want to change that you can modify product-service-test-config.yml 

### Docker Compose
I have included in the folder `docker-compose_stuff` the configuration for the Grafan+Graphite stack and the Kafka+Kowl stack.

How to start the KotlinProductService application
---

1. Run `mvn clean install` to build your application
1. Start application with `java -jar target/kotlin-inventory-service-1.0-SNAPSHOT.jar server config.yml`
1. To check that your application is running enter url `http://localhost:7080`

Health Check
---

To see your applications health enter url `http://localhost:7081/healthcheck`

Project Links
---

* Kowl http://localhost:9080/topics
* Dropwizard Metrics http://localhost:7081/metrics?pretty=true
* Grafana http://localhost:3000/
* Graphite http://localhost:8080/
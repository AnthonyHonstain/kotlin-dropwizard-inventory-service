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

How to start the KotlinProductService application
---

1. Run `mvn clean install` to build your application
1. Start application with `java -jar target/kotlin-inventory-service-1.0-SNAPSHOT.jar server config.yml`
1. To check that your application is running enter url `http://localhost:7080`

Health Check
---

To see your applications health enter url `http://localhost:7081/healthcheck`

# rmqtest

Simple Spring Boot application that sends every 5s a message to RMQ and receives it. To build the project please run

    gradle clean build

or if no gradle installation exists

    ./gradlew clean build

The [Spring Boot AMQP configuration](https://github.com/spring-projects/spring-boot/tree/v1.3.3.RELEASE/spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/amqp) is used. The application expects RabbitMQ to be running `localhost:5672` (Property `spring.rabbitmq.host` and `spring.rabbitmq.port`) or if deployed to PCF a RabbitMQ service binding is expected.

*NOTE:* The application is based on the [Messaging with RabbitMQ](https://github.com/spring-guides/gs-messaging-rabbitmq/blob/master/complete/src/main/java/hello/Application.java) example.§

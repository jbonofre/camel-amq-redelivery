package net.nanthrax.test.camelamqredelivery;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;

public class Main {

    public static void main(String[] args) throws Exception {
        DefaultCamelContext camelContext = new DefaultCamelContext();
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616?jms.redeliveryPolicy.maximumRedeliveries=5&jms.redeliveryPolicy.redeliveryDelay=84000");
        SimpleRegistry registry = new SimpleRegistry();
        registry.put("connectionFactory", connectionFactory);
        camelContext.setRegistry(registry);
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                errorHandler(noErrorHandler());
                from("jms:queue:test?connectionFactory=#connectionFactory&cacheLevelName=CACHE_CONSUMER&acknowledgementModeName=CLIENT_ACKNOWLEDGE")
                        .log("Message received")
                        .choice()
                        .when(simple("${body} contains 'good'")).log("Good message: ${body}")
                        .otherwise().throwException(new IllegalArgumentException("Bad message"));
            }
        });
        camelContext.start();

        while (true) {
            Thread.sleep(10000);
        }
    }

}

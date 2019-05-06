package net.nanthrax.test.camelamqredelivery;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;

public class Main {

    public static void main(String[] args) throws Exception {
        DefaultCamelContext camelContext = new DefaultCamelContext();
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        SimpleRegistry registry = new SimpleRegistry();
        registry.put("connectionFactory", connectionFactory);
        camelContext.setRegistry(registry);
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("jms:queue:test?connectionFactory=#connectionFactory&acknowledgementModeName=CLIENT_ACKNOWLEDGE")
                        .log("Message received")
                        .choice()
                        .when(simple("${body} contains 'good'")).log("Good message: ${body}")
                        .otherwise().process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        throw new IllegalArgumentException("Bad message");
                    }
                });
            }
        });
        camelContext.start();

        while (true) {
            Thread.sleep(10000);
        }
    }

}

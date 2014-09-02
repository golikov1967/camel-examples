package io.github.jimmidyson.examples.camel.amqmqtt;

import org.apache.activemq.broker.BrokerService;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringDelegatingTestContextLoader;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = CamelSpringDelegatingTestContextLoader.class, locations = {
        "classpath:/META-INF/spring/camel-context.xml", "classpath:/activemq-mqtt.xml" })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MyCamelTest {

    @EndpointInject(uri = "mock:result")
    private MockEndpoint mockResult;

    @EndpointInject(uri = "mock:sslResult")
    private MockEndpoint mockSslResult;

    @Produce
    private ProducerTemplate producer;

    @Test
    public void testPlainMqtt() throws Exception {
        producer.sendBody("mqtt:bar?publishTopicName=test/mqtt/topic", "Hello, world!");
        mockResult.expectedMessageCount(1);
        mockResult.assertIsSatisfied();
    }

    @Test
    public void testSslMqtt() throws Exception {
        producer.sendBody("mqtt:bar?host=ssl://localhost:1885&publishTopicName=test/mqtt/topic", "Hello, world!");
        mockSslResult.expectedMessageCount(1);
        mockSslResult.assertIsSatisfied();
    }
}
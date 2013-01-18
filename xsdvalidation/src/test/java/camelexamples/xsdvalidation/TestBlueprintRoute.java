package camelexamples.xsdvalidation;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;
import org.junit.Test;

import javax.jms.*;
import java.util.Properties;
import java.util.Scanner;

public class TestBlueprintRoute extends CamelBlueprintTestSupport {

  @Produce(uri = "activemq:testqueue")
  private ProducerTemplate template;

  @Override
  protected String getBlueprintDescriptor() {
    return "OSGI-INF/blueprint/camelContext.xml";
  }

  private void consumeMessage(String destinationQueue, String expectedBody) throws Exception {
    ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
    Connection connection = connectionFactory.createConnection();
    connection.start();
    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer consumer = session.createConsumer(session.createQueue(destinationQueue));
    Message message = consumer.receive(1000);
    assertNotNull("Should have received a message from destination: " + destinationQueue, message);

    TextMessage textMessage = assertIsInstanceOf(TextMessage.class, message);
    assertEquals("Message body", expectedBody, textMessage.getText());
  }

  @Test
  public void testRouteWithInvalidMessage() throws Exception {
    // set mock expectations
    getMockEndpoint("mock:invalid").expectedMessageCount(1);

    // send a message
    String messageToSend = new Scanner(getClass().getResourceAsStream("/invalid-shiporder.xml")).useDelimiter("\\Z").next();
    template.sendBody(messageToSend);

    // assert mocks
    assertMockEndpointsSatisfied();
  }

  @Test
  public void testRouteWithValidMessage() throws Exception {
    // set mock expectations
    getMockEndpoint("mock:invalid").expectedMessageCount(0);

    // send a message
    String messageToSend = new Scanner(getClass().getResourceAsStream("/valid-shiporder.xml")).useDelimiter("\\Z").next();
    template.sendBodyAndHeader("activemq:testqueue", messageToSend, "DESTINATION", "valid");

    // assert mocks
    assertMockEndpointsSatisfied();

    consumeMessage("valid", messageToSend);
  }

}

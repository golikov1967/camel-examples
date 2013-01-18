package camelexamples.xsdvalidation;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jms.*;
import java.util.Scanner;

public class TestBlueprintRoute extends CamelBlueprintTestSupport {

  private static final String SOURCE_ENDPOINT = "activemq:testqueue";
  private static final String VALID_QUEUE = "valid";
  private static final String INVALID_ENDPOINT = "mock:invalid";
  private static final String DESTINATION_HEADER = "destination";

  private ActiveMQConnectionFactory connectionFactory;
  private Connection connection;
  private Session session;
  private MessageConsumer consumer;

  @Override
  protected String getBlueprintDescriptor() {
    return "OSGI-INF/blueprint/camelContext.xml";
  }

  @Before
  public void setUpConnection() throws Exception {
    connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
    connection = connectionFactory.createConnection();
    connection.start();
    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
  }

  @After
  public void tearDownConnection() throws Exception {
    session.close();
    connection.close();
    connection.close();
  }

  private Message readMessageFromQueue(String destination) throws Exception {
    MessageConsumer consumer = session.createConsumer(session.createQueue(VALID_QUEUE));
    try {
      Message message = consumer.receive(1000);
      return message;
    } finally {
      consumer.close();
    }
  }

  @Test
  public void testRouteWithInvalidMessage() throws Exception {
    // set mock expectations
    getMockEndpoint(INVALID_ENDPOINT).expectedMessageCount(1);

    // send a message
    String messageToSend = new Scanner(getClass().getResourceAsStream("/invalid-shiporder.xml")).useDelimiter("\\Z").next();
    template.sendBodyAndHeader(SOURCE_ENDPOINT, messageToSend, DESTINATION_HEADER, VALID_QUEUE);

    // assert mocks
    assertMockEndpointsSatisfied();

    Message message = readMessageFromQueue(VALID_QUEUE);

    assertNull("Should not have received a message from destination: " + VALID_QUEUE, message);
  }

  @Test
  public void testRouteWithValidMessage() throws Exception {
    // set mock expectations
    getMockEndpoint(INVALID_ENDPOINT).expectedMessageCount(0);

    // send a message
    String messageToSend = new Scanner(getClass().getResourceAsStream("/valid-shiporder.xml")).useDelimiter("\\Z").next();
    template.sendBodyAndHeader(SOURCE_ENDPOINT, messageToSend, DESTINATION_HEADER, VALID_QUEUE);

    // assert mocks
    assertMockEndpointsSatisfied();

    Message message = readMessageFromQueue(VALID_QUEUE);

    assertNotNull("Should have received a message from destination: " + VALID_QUEUE, message);

    TextMessage textMessage = assertIsInstanceOf(TextMessage.class, message);
    assertEquals("Message body", messageToSend, textMessage.getText());
  }

}

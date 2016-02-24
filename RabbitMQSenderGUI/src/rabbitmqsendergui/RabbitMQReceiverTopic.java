package rabbitmqsendergui;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQReceiverTopic {
        public RabbitMQReceiverTopic() throws IOException, TimeoutException{
            String EXCHANGE_NAME = "huis";
            //Connectie settings
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(RabbitMQSenderGUI.host);
            factory.setUsername(RabbitMQSenderGUI.user);
            factory.setPassword(RabbitMQSenderGUI.passwd);

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, "topic");
            String queueName = channel.queueDeclare().getQueue();
            String bindingKey = RabbitMQSenderGUI.HuisID;
            channel.queueBind(queueName, EXCHANGE_NAME, bindingKey);

            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
            Consumer consumer = new DefaultConsumer(channel) {
              @Override
              public void handleDelivery(String consumerTag, Envelope envelope,AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                String msg = message.toString();
                String[] bericht = msg.split("/");
                
                if (RabbitMQSenderGUI.HuisID.equals(bericht[0])){;
                    switch (bericht[1]) {
                        case "Vaatwasmachine":
                            switch (bericht[2]) {
                                case "Uit":
                                    RabbitMQSenderGUI.vaatwas = false;
                                    break;
                                case "Aan":
                                    RabbitMQSenderGUI.vaatwas = true;
                                    break;
                            }       break;
                        case "Lamp":
                            switch (bericht[2]) {
                                case "Uit":
                                    RabbitMQSenderGUI.lamp = false;
                                    break;
                                case "Aan":
                                    RabbitMQSenderGUI.lamp = true;
                            break;
                    }       break;
                    }
                }
                System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + message + "'");
              }
            };
            channel.basicConsume(queueName, true, consumer);
        }
}

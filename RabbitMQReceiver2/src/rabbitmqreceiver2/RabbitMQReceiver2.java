package rabbitmqreceiver2;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

class RabbitMQReceiver2 {
    private static final String EXCHANGE_NAME = "logs";

    public static void main(String[] argv) throws Exception {
        
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("137.135.132.202");
        factory.setUsername("groep3");
        factory.setPassword("abc123!");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, "");
                
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        
        Consumer consumer = new DefaultConsumer(channel) {
        @Override
            public void handleDelivery(String consumerTag, Envelope envelope,AMQP.BasicProperties properties, byte[] body) throws IOException {              
                String message = new String(body, "UTF-8");
                String msg = message;
                System.out.println(" [x] Received '" + message + "'");                
                String[] bericht = msg.split("/");
                
                String URL = "jdbc:sqlserver://sqlgroep3.database.windows.net:1433;databaseName=SQLGroep3";
                String username = "sql";
                String password = "Projectgroep3";
                java.sql.Connection SQLconnection = null;
                Statement st = null;                
                PreparedStatement prepsInsert = null;                
                java.util.Date datum = new java.util.Date();
                DateFormat Huidigedatum = new SimpleDateFormat("yyyy-MM-dd");
                DateFormat Huidigetijd = new SimpleDateFormat("HH:mm:ss");
                DateFormat TimeSTamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String Date = Huidigedatum.format(datum);
                String Tijd = Huidigetijd.format(datum);
                String timestamp = TimeSTamp.format(datum);
                
                try {                                        
                    SQLconnection = DriverManager.getConnection(URL,username,password);                    
                    st = SQLconnection.createStatement();
                    String query = "INSERT INTO huizen (huisnr,apparaat,soort,value,datum,tijd,timestamp) VALUES"+
                            "("+bericht[0]+",'"+bericht[1]+"','"+bericht[2]+"',"+bericht[3]+",'"+Date+"','"+Tijd+"','"+timestamp+"')";
                    prepsInsert = SQLconnection.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
                    prepsInsert.execute();
                    SQLconnection.close();
                    
                } catch (Exception e) {                    
                    System.out.println("error");            
                    e.printStackTrace();
                }
            }
          };
    channel.basicConsume(queueName, true, consumer);
  }

    
}

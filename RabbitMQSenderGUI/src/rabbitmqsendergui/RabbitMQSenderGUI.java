package rabbitmqsendergui;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
/**
 *
 * @author Dimitri
 */
public class RabbitMQSenderGUI extends JFrame{
    //Connectiegegevens instellen
    public static final String EXCHANGE_NAME = "logs";
    public static String host = "137.135.132.202";
    public static String user = "groep3";
    public static String passwd = "abc123!";
    
    public static boolean startstop = true;
    public static boolean lamp = true;
    public static boolean vaatwas = true;
    //Veranderen per huis
    public static final String HuisID = "1";

    public static void main(String[] args) throws IOException, TimeoutException {
        JFrame venster = new RabbitMQSenderGUI();
        venster.setSize(400, 300);
        venster.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        venster.setTitle("Sender simulator");
        JPanel paneeltje = new Paneel() ;
        venster.setContentPane(paneeltje);
        venster.setVisible(true);
    }
}
    class Paneel extends JPanel{
        private final JButton btnStart;
        private final JLabel lblTitel,lblStatusSimulator, lblLamp,lblVaatwas,lblLampVerbruik,lblVaatwasVerbruik,lblPV,lblPVVerbruik,lblTotaal,lblTotaalVerbruik,lblLijn;
        private final ConnectionFactory factory = new ConnectionFactory();
        private Connection connection;
        private Channel channel;
        private update t = new update();
        private RabbitMQReceiverTopic receiver;
        
        public Paneel() throws IOException, TimeoutException {
            setLayout(null);
            receiver = new RabbitMQReceiverTopic();
            //Labels
            btnStart = new JButton();
            lblTitel = new JLabel();
            lblStatusSimulator = new JLabel();
            lblLamp = new JLabel();
            lblVaatwas = new JLabel();
            lblLampVerbruik = new JLabel();
            lblVaatwasVerbruik = new JLabel();
            lblPV = new JLabel();
            lblPVVerbruik = new JLabel();
            lblLijn  = new JLabel();
            lblTotaal = new JLabel();
            lblTotaalVerbruik = new JLabel();
            //Tekst instellen
            btnStart.setText("Start");
            lblTitel.setText("Simulator");
            lblStatusSimulator.setText("Status simulator: ");
            lblLamp.setText("Lamp: ");
            lblVaatwas.setText("Vaatwas: ");
            lblPV.setText("Zonnepaneel: ");
            lblLijn.setText("---------------");
            lblTotaal.setText("Totaal: ");
            //Extra settings
            lblTitel.setFont(new java.awt.Font("Tahoma", 0, 18));
            lblTitel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            lblTitel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            //Bounds instellen
            lblTitel.setBounds(5, 5, 375, 30);
            btnStart.setBounds(5, 40, 80, 30);
            lblStatusSimulator.setBounds(100, 40, 300, 30);
            lblLamp.setBounds(5,80,80,30);
            lblLampVerbruik.setBounds(100,80,300,30);
            lblVaatwas.setBounds(5,120,80,30);
            lblVaatwasVerbruik.setBounds(100,120,300,30);
            lblPV.setBounds(5,160,80,30);
            lblPVVerbruik.setBounds(100,160,300,30);
            lblLijn.setBounds(100,180,300,30);
            lblTotaal.setBounds(5, 210, 80, 30);
            lblTotaalVerbruik.setBounds(100,210,300,30);
            //Alles op Paneel zetten
            add(lblTitel);
            add(btnStart);
            add(lblStatusSimulator);
            add(lblLamp);
            add(lblVaatwas);
            add(lblLampVerbruik);
            add(lblVaatwasVerbruik);
            add(lblPV);
            add(lblPVVerbruik);
            add(lblLijn);
            add(lblTotaal);
            add(lblTotaalVerbruik);
            //Handler
            StartStopHandler SSHandler = new StartStopHandler();
            btnStart.addActionListener(SSHandler);
        }
        //ButtonHandler
        class StartStopHandler implements ActionListener{
            @Override
            public void actionPerformed(ActionEvent e) {                                         
                if (RabbitMQSenderGUI.startstop == true){
                    try {
                        start();
                    } catch (IOException | TimeoutException | InterruptedException ex) {
                        Logger.getLogger(Paneel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                else if (RabbitMQSenderGUI.startstop == false){
                    try {
                        stop();
                    } catch (IOException | TimeoutException ex) {
                        Logger.getLogger(Paneel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        //Start methode
        public void start() throws IOException, TimeoutException, InterruptedException{
            lblStatusSimulator.setText("Status simulator: Running");
            btnStart.setText("Stop");
            RabbitMQSenderGUI.startstop = false;
            t = new update();
            t.start();
        }
        //Stop methode
        public void stop() throws IOException, TimeoutException{
            lblStatusSimulator.setText("Status simulator: Not running");
            btnStart.setText("Start");
            RabbitMQSenderGUI.startstop = true;
            lblLamp.setForeground(Color.BLACK);
            lblLampVerbruik.setForeground(Color.BLACK);
            lblVaatwas.setForeground(Color.BLACK);
            lblVaatwasVerbruik.setForeground(Color.BLACK);
            lblPVVerbruik.setForeground(Color.BLACK);
            lblPV.setForeground(Color.BLACK);
            t.stop();
        }
        //Thread
        class update extends Thread{
            @Override
            public void run(){
                try {
                    String message;
                    Random r =  new Random();
                    int random;
                    int Totaal = 0;
                    factory.setHost(RabbitMQSenderGUI.host);
                    factory.setUsername(RabbitMQSenderGUI.user);
                    factory.setPassword(RabbitMQSenderGUI.passwd);
                    connection = factory.newConnection();
                    channel = connection.createChannel();
                    channel.exchangeDeclare(RabbitMQSenderGUI.EXCHANGE_NAME, "fanout");
                    
                    while (true){
                        if (RabbitMQSenderGUI.lamp == true){
                            //Max = 200, Min = 100 --> (Max-min)+min
                            random = r.nextInt(100)+100;
                            lblLamp.setText("Lamp: Aan");
                            lblLamp.setForeground(Color.GREEN);
                            lblLampVerbruik.setText(random + "W");
                            lblLampVerbruik.setForeground(Color.GREEN);
                            message = RabbitMQSenderGUI.HuisID +"/Lamp/Power/"+ random;
                            channel.basicPublish(RabbitMQSenderGUI.EXCHANGE_NAME, "", null, message.getBytes());
                            Totaal += random;
                        } else{
                            lblLamp.setText("Lamp: Uit");
                            lblLamp.setForeground(Color.RED);
                            lblLampVerbruik.setText("");
                            lblLampVerbruik.setForeground(Color.RED);
                            message = RabbitMQSenderGUI.HuisID +"/Lamp/Power/0";
                            channel.basicPublish(RabbitMQSenderGUI.EXCHANGE_NAME, "", null, message.getBytes());
                        }
                        if (RabbitMQSenderGUI.vaatwas == true){
                            //Max = 1200, Min = 2000 --> (Max-min)+min
                            random = r.nextInt(800)+1200;
                            lblVaatwas.setText("Vaatwas: Aan");
                            lblVaatwas.setForeground(Color.GREEN);
                            lblVaatwasVerbruik.setText(random + "W");
                            lblVaatwasVerbruik.setForeground(Color.GREEN);
                            message = RabbitMQSenderGUI.HuisID +"/Vaatwasmachine/Power/"+ random;
                            channel.basicPublish(RabbitMQSenderGUI.EXCHANGE_NAME, "", null, message.getBytes());
                            Totaal += random;
                        } else {
                            lblVaatwas.setText("Vaatwas: Uit");
                            lblVaatwas.setForeground(Color.RED);
                            lblVaatwasVerbruik.setText("");
                            lblVaatwasVerbruik.setForeground(Color.RED);
                            message = RabbitMQSenderGUI.HuisID +"/Vaatwasmachine/Power/0";
                            channel.basicPublish(RabbitMQSenderGUI.EXCHANGE_NAME, "", null, message.getBytes());
                        }
                        //Max = 2500, Min = 1100 --> (Max-min)+min
                        random = r.nextInt(1400)+1100;
                        lblPVVerbruik.setText(random+"W");
                        lblPVVerbruik.setForeground(Color.GREEN);
                        lblPV.setForeground(Color.GREEN);
                        message = RabbitMQSenderGUI.HuisID +"/PV/Power/"+ random;
                        channel.basicPublish(RabbitMQSenderGUI.EXCHANGE_NAME, "", null, message.getBytes());
                        Totaal -= random;
                        
                        random = r.nextInt(200)+100;
                        message = RabbitMQSenderGUI.HuisID +"/Baseload/Power/"+ random;
                        channel.basicPublish(RabbitMQSenderGUI.EXCHANGE_NAME, "", null, message.getBytes());
                        Totaal += random;
                        
                        message = RabbitMQSenderGUI.HuisID +"/Totaal/Power/"+ Totaal;
                        lblTotaalVerbruik.setText(Totaal+"W");
                        channel.basicPublish(RabbitMQSenderGUI.EXCHANGE_NAME, "", null, message.getBytes());
                        System.out.println(" [x] Sent '" + message + "'");
                        Totaal = 0;
                        Thread.sleep(3000);
                    }   } catch (IOException | TimeoutException | InterruptedException ex) {
                    Logger.getLogger(Paneel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }     
}
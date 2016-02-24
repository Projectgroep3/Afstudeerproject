package com.mycompany.app;

import com.microsoft.azure.iothub.DeviceClient;
import com.microsoft.azure.iothub.IotHubClientProtocol;
import com.microsoft.azure.iothub.Message;
import com.microsoft.azure.iothub.IotHubStatusCode;
import com.microsoft.azure.iothub.IotHubEventCallback;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import com.google.gson.Gson;


public class App 
{
	private static String connString = "HostName=IotHubGroep3.azure-devices.net;DeviceId=huis1;SharedAccessKey=9sxdW7PBGfVakXHq3cJiZS8vMovXmRp09ury5B0Weh0=";
	private static IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
	
	public static void main( String[] args ) throws IOException, URISyntaxException {

		  MessageSender ms0 = new MessageSender();
		  Thread t0 = new Thread(ms0);
		  t0.start(); 

		  System.out.println("Press ENTER to exit.");
		  System.in.read();
		  ms0.stopThread = true;
	}
	
	private static class TelemetryDataPoint {
		  public String deviceId, apparaat;
		  public int verbruik;
		  public String tijdVerstuurd;

		  public String serialize() {
		    Gson gson = new Gson();
		    return gson.toJson(this);
		  }
	}
	
	private static class EventCallback implements IotHubEventCallback
	{
	  public void execute(IotHubStatusCode status, Object context) {
	    System.out.println("IoT Hub responded to message with status " + status.name());

	    if (context != null) {
	      synchronized (context) {
	        context.notify();
	      }
	    }
	  }
	}
	
	public static Message apparaat(String huis,String apparaat,int verbruik){	
		
        TelemetryDataPoint telemetryDataPoint = new TelemetryDataPoint();
        telemetryDataPoint.deviceId = huis;
        telemetryDataPoint.apparaat = apparaat;
        telemetryDataPoint.verbruik = verbruik;
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyy/MM/dd hh:mm:ss");
        telemetryDataPoint.tijdVerstuurd = sdf.format(date);		        
        String msgStr = telemetryDataPoint.serialize();
        Message msg = new Message(msgStr);
        System.out.println(msgStr);
		
		return msg;
	}
	
	private static class MessageSender implements Runnable {
		  public volatile boolean stopThread = false;

		  public void run()  {
		    try {
			Random rand = new Random();
		      DeviceClient client;
		      client = new DeviceClient(connString, protocol);
		      client.open();
		      
		      
		      while (!stopThread) {
		        Object lockobj = new Object();
		        EventCallback callback = new EventCallback();
		        client.sendEventAsync(apparaat("huis1", "lamp", rand.nextInt(60)), callback, lockobj);
		        client.sendEventAsync(apparaat("huis1", "vaatwas", rand.nextInt(2000)), callback, lockobj);
		        client.sendEventAsync(apparaat("huis1", "koffiemachine", rand.nextInt(600)), callback, lockobj);

		        synchronized (lockobj) {
		          lockobj.wait();
		        }
		        Thread.sleep(1000);
		      }
		      client.close();
		    } catch (Exception e) {
		      e.printStackTrace();
		    }
		  }
		}
}

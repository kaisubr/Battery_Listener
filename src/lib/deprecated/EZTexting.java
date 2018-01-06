package lib.deprecated;

import com.eztexting.api.client.EzTextingClient;
import com.eztexting.api.client.api.messaging.model.DeliveryMethod;
import com.eztexting.api.client.api.messaging.model.SmsMessage;
import listener.texting.EZ_P;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Battery_Listener, file created in listener.texting by Kailash Sub.
 */
@Deprecated
public class EZTexting {
    EzTextingClient client;

    public EZTexting() {
        client = new EzTextingClient(EZ_P.username, EZ_P.password);
    }

    public String sendSMSMessageAtDate(String subject, String message, DeliveryMethod method, List<String> outBoxNumbers, Date date) {
        SmsMessage msg = new SmsMessage();
        msg.setSubject(subject);
        msg.setMessage(message);
        msg.setDeliveryMethod(method);
        msg.setPhoneNumbers(outBoxNumbers);
        msg.setStampToSend(date);
        SmsMessage response = client.messagingApi().send(msg);
        return response.toString();
    }

    public String sendSMSMessageNow(String subject, String message, DeliveryMethod method, List<String> outBoxNumbers) {
        SmsMessage msg = new SmsMessage();
        msg.setSubject(subject);
        msg.setMessage(message);
        msg.setDeliveryMethod(method);
        msg.setPhoneNumbers(outBoxNumbers);
        SmsMessage response = client.messagingApi().send(msg);
        return response.toString();
    }

    public String sendSMSMessageNow(String subject, String message, DeliveryMethod method) {
        SmsMessage msg = new SmsMessage();
        msg.setSubject(subject);
        msg.setMessage(message);
        msg.setDeliveryMethod(method);
        msg.setPhoneNumbers(Arrays.asList(EZ_P.I_PHONE_4));
        SmsMessage response = client.messagingApi().send(msg);
        System.out.println("SENT! " + response);
        return response.toString();
    }

    public void sendSMSMessageManually(String subject, String message, DeliveryMethod method) throws IOException {

        String data = "User=" + EZ_P.username + "&Password=" + EZ_P.password +
                "&PhoneNumbers[]=" + EZ_P.I_PHONE_4 +
                "&Subject=" + subject + "&Message=" + message + "&MessageTypeID=" + method;

        URL url = new URL("https://app.eztexting.com/sending/messages?format=xml");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");

        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(data);
        wr.flush();
        conn.connect();

        int responseCode = conn.getResponseCode();
        System.out.println("Response code: " + responseCode);
        System.out.println(conn.getResponseMessage());

    }
}

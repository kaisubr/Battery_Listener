package listener.texting;

import net.sargue.mailgun.Configuration;
import net.sargue.mailgun.Mail;
import org.glassfish.jersey.client.ClientResponse;
import sample.Main;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import java.io.File;

/**
 * Battery_Listener, file created in listener.texting by Kailash Sub.
 */
public class MailGTextingClient {
    public static Configuration configuration;

    public MailGTextingClient() {
        createConfiguration();
    }

    private static void createConfiguration() {
        configuration = new Configuration()
                .domain(SG_P.domain)
                .apiKey(SG_P.api_key)
                .from("Mailgun Sandbox", "postmaster@sandbox34a535cc84ca49e1bf03a97e104c8b5d.mailgun.org");
    }

    public static void test() {
        System.out.println("testing");
        if (configuration == null) createConfiguration();
        Mail.using(configuration)
                .to(SG_P.to)
                .subject("This is a test.")
                .text("Hello world!")
                .build()
                .send();
        System.out.println("testing done.");
    }

    public void sendMesesage(String subject, String content) {
        System.out.println("~ sending ~");

        Mail.using(configuration)
                .to(SG_P.to)
                .subject(subject)
                .text(content)
                .multipart()
                    .attachment(new File(Main.LOG_PATH))
                .build()
                .send();

        System.out.println("~ sending done ~");
    }
}

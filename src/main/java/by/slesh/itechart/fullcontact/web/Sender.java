package by.slesh.itechart.fullcontact.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import by.slesh.itechart.fullcontact.util.PathUtil;

/**
 * @author Eugene Putsykovich(slesh) Mar 5, 2015
 *
 */
public class Sender {
    private final static Logger LOGGER = Logger.getLogger(Sender.class);

    public static final Properties SSL = new Properties();
    static {
	SSL.put("mail.smtp.host", "smtp.gmail.com");
	SSL.put("mail.smtp.socketFactory.port", "465");
	SSL.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
	SSL.put("mail.smtp.auth", "true");
	SSL.put("mail.smtp.port", "465");
    }

    public static final Properties TLS = new Properties();
    static {
	TLS.put("mail.smtp.auth", "true");
	TLS.put("mail.smtp.starttls.enable", "true");
	TLS.put("mail.smtp.host", "smtp.gmail.com");
	TLS.put("mail.smtp.port", "587");
    }

    private String from;
    private String username;
    private String password;

    private void fetchCredentials() {
	Properties credentials = new Properties();
	try (InputStream input = PathUtil.getResourceFile("web.properties")) {
	    credentials.load(input);
	    from = credentials.getProperty("from");
	    username = credentials.getProperty("username");
	    password = credentials.getProperty("password");
	    LOGGER.info("mail credentials loaded successful:");
	    LOGGER.info(credentials.toString());
	} catch (IOException ex) {
	    LOGGER.info("cannot open and load mail server credentials file");
	}
    }

    private Properties properties;

    private Sender(Properties properties) {
	this.properties = properties;
	fetchCredentials();
    }

    public static Sender createSender(Properties properties) {
	return new Sender(properties);
    }
    
    public Email createEmail() {
	LOGGER.info("BEGIN");
	Message message = null;
	try {
	    Authenticator authenticator = new GMailAuthenticator(username, password);
	    Session session = Session.getDefaultInstance(properties, authenticator);
	    message = new MimeMessage(session);
	    message.setFrom(new InternetAddress(from));
	} catch (MessagingException e) {
	    e.printStackTrace();
	}
	LOGGER.info("END email created successful!");
	return new Email(message);
    }
    
    public void send(Email email) {
	try {
	    Message message = email.getMessage();
	    Transport.send(message);
	} catch (MessagingException e) {
	    throw new RuntimeException("Some problem occured during send. Maybe internet connection bad.");
	}
    }
}

package test.java.appModules;

import java.io.IOException;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;

public class ReadMail {

	private static final String email_id = "number.2018@yahoo.com";
	private static final String password = "Evernym123";

	public static String getSmsLink() {
		// set properties
		Properties properties = new Properties();
		// You can use imap or imaps , *s -Secured
		properties.put("mail.store.protocol", "imaps");
		// Host Address of Your Mail
		properties.put("mail.imaps.host", "imap.mail.yahoo.com");
		// Port number of your Mail Host
		properties.put("mail.imaps.port", "993");
		properties.put("mail.imap.ssl.enable", "true");
		properties.put("mail.imap.mail.auth", "true");
		String link = "";

		// properties.put("mail.imaps.timeout", "10000");

		try {

			// create a session
			Session session = Session.getDefaultInstance(properties, null);
			// SET the store for IMAPS
			Store store = session.getStore("imaps");

			System.out.println("Connection initiated......");
			// Trying to connect IMAP server
			store.connect(email_id, password);
			System.out.println("Connection is ready :)");

			// Get inbox folder
			Folder inbox = store.getFolder("inbox");
			// SET readonly format (*You can set read and write)
			inbox.open(Folder.READ_ONLY);

			// Display email Details

			// Inbox email count
			int messageCount = inbox.getMessageCount();
			System.out.println("Total Messages in INBOX:- " + messageCount);

			Message[] messages = inbox.getMessages();
			Message message = messages[messages.length - 1];
			String MessageBody = getTextFromMessage(message);
			System.out.println("Text: " + MessageBody);
			// Extract the received sms invitation link from the message
			link = MessageBody.substring(MessageBody.indexOf("https://link.comect.me"), MessageBody.indexOf("https://link.comect.me") + 34);
			System.out.println("SMS Link  = " + link);
			// close the store and folder objects
			inbox.close(false);
			store.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return link;
	}

	private static String getTextFromMessage(Message message) throws MessagingException, IOException {
		String result = "";
		if (message.isMimeType("text/plain")) {
			result = message.getContent().toString();
		} else if (message.isMimeType("multipart/*")) {
			MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
			result = getTextFromMimeMultipart(mimeMultipart);
		}
		return result;
	}

	private static String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
		String result = "";
		int count = mimeMultipart.getCount();
		for (int i = 0; i < count; i++) {
			BodyPart bodyPart = mimeMultipart.getBodyPart(i);
			if (bodyPart.isMimeType("text/plain")) {
				result = result + "\n" + bodyPart.getContent();
				break; // without break same text appears twice in my tests
			} else if (bodyPart.isMimeType("text/html")) {
				String html = (String) bodyPart.getContent();
			} else if (bodyPart.getContent() instanceof MimeMultipart) {
				result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
			}
		}
		return result;
	}
}
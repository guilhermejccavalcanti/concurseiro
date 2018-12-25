import java.util.Properties;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class JavaEmailSender {
	public static void enviar(String subject, String msg) {
		Properties props = new Properties();
		/** Par�metros de conex�o com servidor Gmail */
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");

		Session session = Session.getDefaultInstance(props,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication("guiganister@gmail.com",	"gjcc1990");
					}
				});
		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("guiganister@gmail.com")); // Remetente
			Address[] toUser = InternetAddress.parse("guiganister@gmail.com");
			message.setRecipients(Message.RecipientType.TO, toUser);
			message.setSubject(subject);// Assunto
			message.setText(msg);
			Transport.send(message);
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}
}
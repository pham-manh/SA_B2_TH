package org.example.form;

import org.apache.log4j.BasicConfigurator;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

public class ReceiverForm extends JFrame {

    private JPanel pnl_main;
    private JButton showButton;
    private JTextField textField1;

    public ReceiverForm(String title) {
        super(title);
        this.setTitle(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(pnl_main);
        this.pack();
        showButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    receiver(textField1);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    public static void main(String[] args) {
        JFrame jFrame = new ReceiverForm("ReceiverForm");
        jFrame.setVisible(true);
    }

    private void receiver(JTextField textField1) throws Exception {
        BasicConfigurator.configure();

        Properties settings = new Properties();
        settings.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        settings.setProperty(Context.PROVIDER_URL,
                "tcp://localhost:61616");

        Context context = new InitialContext(settings);

        Object obj = context.lookup("ConnectionFactory");

        ConnectionFactory factory = (ConnectionFactory) obj;

        Destination destination = (Destination) context.
                lookup("dynamicQueues/phamdinhmanh");

        Connection connection = factory.
                createConnection("admin", "admin");

        connection.start();

        Session session = connection.createSession(false,
                Session.CLIENT_ACKNOWLEDGE);

        MessageConsumer messageConsumer = session.createConsumer(destination);

        messageConsumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                try {
                    if (message instanceof TextMessage) {
                        TextMessage textMessage = (TextMessage) message;
                        String str = textMessage.getText();
                        textField1.setText(str);
                        System.out.println("Receive" + str);
                        textMessage.acknowledge();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

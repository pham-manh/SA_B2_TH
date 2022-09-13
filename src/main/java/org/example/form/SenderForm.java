package org.example.form;

import org.apache.log4j.BasicConfigurator;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

public class SenderForm extends JFrame {
    private JPanel pnl_main;
    private JTextField txt_Text;
    private JButton sendButton;

    public SenderForm(String title) {
        super(title);
        this.setTitle(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(pnl_main);
        this.setLocationRelativeTo(null);
        this.pack();

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    sender(txt_Text.getText());
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    public static void main(String[] args) {
        JFrame jFrame = new SenderForm("SenderForm");
        jFrame.setVisible(true);
    }

    private void sender(String str) throws Exception {
        BasicConfigurator.configure();
        Properties setting = new Properties();
        setting.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        setting.setProperty(Context.PROVIDER_URL,"tcp://localhost:61616");
        Context context = new InitialContext(setting);
        ConnectionFactory factory = (ConnectionFactory) context.lookup("ConnectionFactory");
        Destination destination = (Destination)
                context.lookup("dynamicQueues/phamdinhmanh");
        Connection connection = factory.createConnection("admin","admin");
        connection.start();

        Session session = connection.createSession(
                false,Session.AUTO_ACKNOWLEDGE
        );

        MessageProducer producer = session.createProducer(destination);

        Message msg = session.createTextMessage(txt_Text.getText());

        producer.send(msg);

        session.close();
        connection.close();
        System.out.println("Done...");

    }
}

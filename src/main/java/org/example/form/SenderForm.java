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
//config environment for JNDI
        Properties settings = new Properties();
        settings.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        settings.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
//create context
        Context ctx = new InitialContext(settings);
//lookup JMS connection factory
        ConnectionFactory factory =
                (ConnectionFactory) ctx.lookup("ConnectionFactory");
//lookup destination. (If not exist-->ActiveMQ create once)
        Destination destination =
                (Destination) ctx.lookup("dynamicQueues/thanthidet");
//get connection using credential
        Connection con = factory.createConnection("admin", "admin");
//connect to MOM
        con.start();
//create session
        Session session = con.createSession(
                /*transaction*/false,
                /*ACK*/Session.AUTO_ACKNOWLEDGE
        );
//create producer
        MessageProducer producer = session.createProducer(destination);
//create text message
        Message msg = session.createTextMessage(str);
        producer.send(msg);
//shutdown connection
        session.close();
        con.close();
        System.out.println("Finished...");
    }
}

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
//thiết lập môi trường cho JJNDI
        Properties settings = new Properties();
        settings.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        settings.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
//tạo context
        Context ctx = new InitialContext(settings);
//lookup JMS connection factory
        Object obj = ctx.lookup("ConnectionFactory");
        ConnectionFactory factory = (ConnectionFactory) obj;
//lookup destination
        Destination destination
                = (Destination) ctx.lookup("dynamicQueues/thanthidet");
//tạo connection
        Connection con = factory.createConnection("admin", "admin");
//nối đến MOM
        con.start();
//tạo session
        Session session = con.createSession(
                /*transaction*/false,
                /*ACK*/Session.CLIENT_ACKNOWLEDGE
        );
//tạo consumer
        MessageConsumer receiver = session.createConsumer(destination);
//blocked-method for receiving message - sync
//receiver.receive();
//Cho receiver lắng nghe trên queue, chừng có message thì notify - async
        System.out.println("Tý was listened on queue...");

        receiver.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) {//msg là message nhận được
                try {
                    if (msg instanceof TextMessage tm) {
                        String txt = tm.getText();
                        System.out.println("Nhận được " + txt);
                        textField1.setText(txt);
                        msg.acknowledge();//gửi tín hiệu ack
                    } else if (msg instanceof ObjectMessage om) {
                        System.out.println(om);
                    }
//others message type....
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
}

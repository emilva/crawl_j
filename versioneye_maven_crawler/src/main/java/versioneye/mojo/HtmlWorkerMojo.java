package versioneye.mojo;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import versioneye.service.RabbitMqService;

import java.util.Properties;

@Mojo( name = "html_worker", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class HtmlWorkerMojo extends HtmlMojo {

    private final static String QUEUE_NAME = "html_worker";

    public void execute() throws MojoExecutionException, MojoFailureException {
        try{
            super.execute();

            username = null;
            password = null;

            Properties properties = getProperties();
            String rabbitmqAddr = properties.getProperty("rabbitmq_addr");
            String rabbitmqPort = properties.getProperty("rabbitmq_port");
            Connection connection = RabbitMqService.getConnection(rabbitmqAddr, new Integer(rabbitmqPort));
            Channel channel = connection.createChannel();

            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume(QUEUE_NAME, true, consumer);

            while (true) {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                String message = new String(delivery.getBody());
                System.out.println(" [x] Received '" + message + "'");
                processPom(message);
            }
        } catch( Exception exception ){
            exception.printStackTrace();
            getLog().error(exception);
            throw new MojoExecutionException("Oh no! Something went wrong. Get in touch with the VersionEye guys and give them feedback.", exception);
        }
    }

}

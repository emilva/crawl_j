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

            Connection connection = initConnection();
            Channel channel = connection.createChannel();

            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            getLog().info(" [*] Waiting for messages. To exit press CTRL+C");

            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume(QUEUE_NAME, true, consumer);

            while (true) {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                String message = new String(delivery.getBody());
                getLog().info(" [x] Received '" + message + "'");
                processMessage( message );
                getLog().info(" [x] Job done for '" + message + "'");
            }
        } catch( Exception exception ){
            exception.printStackTrace();
            getLog().error(exception);
            throw new MojoExecutionException("Oh no! Something went wrong. Get in touch with the VersionEye guys and give them feedback.", exception);
        }
    }

    private void processMessage(String message){
        try{
            String[] sps = message.split("::");
            String repoName = sps[0].toLowerCase();
            String pomUrl = sps[1];

            setRepository( repoName );

            processPom( pomUrl );
        } catch (Exception exception) {
            exception.printStackTrace();
            getLog().error(exception);
        }
    }

    private Connection initConnection() throws Exception {
        String rabbitmqAddr = System.getenv("RM_PORT_5672_TCP_ADDR");
        String rabbitmqPort = System.getenv("RM_PORT_5672_TCP_PORT");
        if (rabbitmqAddr == null || rabbitmqAddr.isEmpty() || rabbitmqPort == null || rabbitmqPort.isEmpty()){
            Properties properties = getProperties();
            rabbitmqAddr = properties.getProperty("rabbitmq_addr");
            rabbitmqPort = properties.getProperty("rabbitmq_port");
        }
        return RabbitMqService.getConnection(rabbitmqAddr, new Integer(rabbitmqPort));
    }

}

package versioneye.service;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.Test;

import java.util.Date;

public class TimeStampServiceTest {

    private TimeStampService timeStampService;
    private static ApplicationContext context;

    @Test
    public void init(){
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        timeStampService = (TimeStampService) context.getBean("timeStampService");
    }

    @Test(dependsOnMethods = {"init"})
    public void test(){
        Date date = timeStampService.getTimeStampFor("org.apache.kafka", "kafka_2.11", "0.9.0.1");
        assert date != null;
        System.out.println("Date for org.apache.kafka/kafka_2.11:0.9.0.1 is " +  date.toString());
        assert (date.toString().equals("Fri Feb 12 03:20:39 CET 2016") || date.toString().equals("Fri Feb 12 02:20:39 UTC 2016"));
    }

}

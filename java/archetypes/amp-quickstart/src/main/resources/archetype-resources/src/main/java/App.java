package ${package};

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class App {
	
	private static final Logger logger = LoggerFactory.getLogger(App.class);
	
    public static void main(String args[]) {

        ClassPathXmlApplicationContext applicationContext = null;
    	
        try {

            applicationContext = new ClassPathXmlApplicationContext(
                    "ampContext.xml", "AllBussesConfig.xml",
                    "GtsConfigSSL.xml", "AnubisAndTwoWaySSLRabbitConfig.xml");


            // pull AmpService from the container
            AmpService ampService = applicationContext.getBean(AmpService.class);

            // start the service
            ampService.start();
            
            // wait for the user to exit
            logger.info("Press any key to exit.");
            
            try { System.in.read(); } catch (Exception ex) {}

            // stop the service
            ampService.stop();
        }
        catch (Exception ex) {
        	
            logger.error("Failed to start the AMP Service", ex);
        }
        finally {
        		
            if (applicationContext != null){

                applicationContext.close();
            }
        }
    }
}

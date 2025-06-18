package mdt.workflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@SpringBootApplication
@ConfigurationPropertiesScan("mdt")
public class MDTWorkflowManagerApplication {
	public static void main(String[] args) throws Exception {
        SpringApplication.run(MDTWorkflowManagerApplication.class, args);
	}
}

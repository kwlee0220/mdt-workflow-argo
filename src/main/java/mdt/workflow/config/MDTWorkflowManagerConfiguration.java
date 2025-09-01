package mdt.workflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Configuration
@ConfigurationProperties(prefix = "mdt.workflow-manager")
@Getter @Setter
@Accessors(prefix = "m_")
public class MDTWorkflowManagerConfiguration {
	private String m_argoEndpoint;
	private String m_argoNamespace;
	private String m_mdtEndpoint;
	private String m_clientDockerImage;
}

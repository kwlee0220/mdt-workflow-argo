package mdt.workflow.config;

import java.io.File;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Configuration
@ConfigurationProperties(prefix = "mdt.workflow-manager.airflow")
public class AirflowWorkflowManagerConfiguration {
	private String m_mdtEndpoint;
	private String m_airflowBaseUrl;
	private File m_dagsFolder;
	
	public String getMdtEndpoint() {
		return m_mdtEndpoint;
	}
	
	public void setMdtEndpoint(String endpoint) {
		m_mdtEndpoint = endpoint;
	}
	
	public String getAirflowBaseUrl() {
		return m_airflowBaseUrl;
	}
	
	public void setAirflowBaseUrl(String url) {
		m_airflowBaseUrl = url;
	}

	public File getDagsFolder() {
		return m_dagsFolder;
	}
	
	public void setDagsFolder(File dagsFolder) {
		m_dagsFolder = dagsFolder;
	}
}

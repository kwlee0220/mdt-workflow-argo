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
	private String m_mdtUrl;
	private String m_airflowBaseUrl;
	private File m_dagsFolder;
	
	public String getMdtUrl() {
		return m_mdtUrl;
	}
	
	public void setMdtUrl(String url) {
		m_mdtUrl = url;
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

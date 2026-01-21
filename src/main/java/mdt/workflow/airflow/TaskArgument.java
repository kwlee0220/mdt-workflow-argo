package mdt.workflow.airflow;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class TaskArgument {
	private final String m_argId;
	
	public abstract String getPythonStatement();
	
	protected TaskArgument(String argId) {
		m_argId = argId.replaceAll("-", "_");
	}
	
	public String getId() {
		return m_argId;
	}
}
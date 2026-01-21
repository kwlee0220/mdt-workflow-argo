package mdt.workflow.airflow;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import utils.Tuple;
import utils.Utilities;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class AirflowWorkflowId {
	private final String m_dagId;
	private final String m_dagRunId;
	
	public AirflowWorkflowId(String dagId, String dagRunId) {
		m_dagId = dagId;
		m_dagRunId = dagRunId;
	}
	
	public String getDagId() {
		return m_dagId;
	}
	
	public String getRunId() {
		return m_dagRunId;
	}
	
	public String toUrl(String prefix) {
		String dagEncoded = URLEncoder.encode(m_dagId, StandardCharsets.UTF_8);
		String dagRunEncoded = URLEncoder.encode(m_dagRunId, StandardCharsets.UTF_8);

		return String.format("%s/dags/%s/dagRuns/%s", prefix, dagEncoded, dagRunEncoded);
	}
	
	public String toStringExpr() {
		return String.format("%s__%s", m_dagId, m_dagRunId);
	}
	
	@Override
	public String toString() {
		return m_dagRunId;
	}
	
	public static AirflowWorkflowId parse(String taskIdStr) {
		Tuple<String,String> pair = Utilities.split(taskIdStr, "__");
		if ( pair == null ) {
			throw new IllegalArgumentException("invalid Airflow task id: " + taskIdStr);
		}
		
		return new AirflowWorkflowId(pair._1, pair._2);
	}
}

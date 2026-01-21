package mdt.workflow.airflow;

import java.util.List;

import com.google.common.base.Preconditions;

import utils.stream.FStream;

import mdt.workflow.WorkflowModel;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class DagSpec {
	private final String m_dagId;
	private final String m_description;
	private final List<TaskSpec> m_tasks;

	public DagSpec(WorkflowModel wfDesc) {
		m_dagId = wfDesc.getId();
		Preconditions.checkArgument(PythonIdentifierUtil.isValidPythonIdentifier(m_dagId),
				                    "invalid DAG id: %s (invalid Python identifier)", m_dagId);
		m_description = wfDesc.getDescription();
		
		m_tasks = FStream.from(wfDesc.getTaskDescriptors())
										.map(TaskSpec::from)
										.toList();
	}

	public String getId() {
		return m_dagId;
	}
	
	public String getDescription() {
		return m_description;
	}

	public List<TaskSpec> getTasks() {
		return m_tasks;
	}
}
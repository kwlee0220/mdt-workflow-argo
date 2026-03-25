package mdt.workflow;

import mdt.model.ResourceNotFoundException;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface WorkflowInstanceManagerProvider extends WorkflowInstanceManager {
	public String getWorkflowScript(String wfModelId) throws ResourceNotFoundException;
	public void onWorkflowModelAdded(WorkflowModel wfModel) throws MDTWorkflowInstanceManagerException;
	public void onWorkflowModelRemoved(String wfModelId) throws MDTWorkflowInstanceManagerException;
}

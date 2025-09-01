package mdt.workflow.service;

import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import mdt.model.ResourceAlreadyExistsException;
import mdt.model.ResourceNotFoundException;
import mdt.workflow.Workflow;
import mdt.workflow.WorkflowInstanceManager;
import mdt.workflow.WorkflowManager;
import mdt.workflow.WorkflowModel;
import mdt.workflow.WorkflowModelManager;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Service
@RequiredArgsConstructor
public class MDTWorkflowManager implements WorkflowManager {
	private final WorkflowModelManager m_modelManager;
	private final WorkflowInstanceManager m_instanceManager;
	
	@Override
	public List<WorkflowModel> getWorkflowModelAll() {
		return m_modelManager.getWorkflowModelAll();
	}
	
	@Override
	public WorkflowModel getWorkflowModel(String wfModelId) throws ResourceNotFoundException {
		return m_modelManager.getWorkflowModel(wfModelId);
	}
	
	@Override
	public WorkflowModel addWorkflowModel(WorkflowModel desc) throws ResourceAlreadyExistsException {
		return m_modelManager.addWorkflowModel(desc);
	}
	
	@Override
	public WorkflowModel addOrReplaceWorkflowModel(WorkflowModel desc) {
		return m_modelManager.addOrReplaceWorkflowModel(desc);
	}
	
	@Override
	public void removeWorkflowModel(String wfModelId) throws ResourceNotFoundException {
		m_modelManager.removeWorkflowModel(wfModelId);
	}
	
	@Override
	public void removeWorkflowModelAll() {
		m_modelManager.removeWorkflowModelAll();
	}
	
	@Override
	public String getWorkflowScript(String wfModelId, String mdtEndpoint, String clientDockerImage)
		throws ResourceNotFoundException {
		return m_modelManager.getWorkflowScript(wfModelId, mdtEndpoint, clientDockerImage);
	}
	
	@Override
	public List<Workflow> getWorkflowAll() {
		return m_instanceManager.getWorkflowAll();
	}
	
	@Override
	public Workflow getWorkflow(String wfId) throws ResourceNotFoundException {
		return m_instanceManager.getWorkflow(wfId);
	}
	
	@Override
	public Workflow startWorkflow(@NonNull String wfModelId) throws ResourceNotFoundException {
		return m_instanceManager.startWorkflow(wfModelId);
	}
	
	@Override
	public void stopWorkflow(String wfId) throws ResourceNotFoundException {
		m_instanceManager.stopWorkflow(wfId);
	}
	
	@Override
	public Workflow suspendWorkflow(String wfId) throws ResourceNotFoundException {
		return m_instanceManager.suspendWorkflow(wfId);
	}
	
	@Override
	public Workflow resumeWorkflow(String wfId) throws ResourceNotFoundException {
		return m_instanceManager.resumeWorkflow(wfId);
	}
	
	@Override
	public void removeWorkflow(String wfId) throws ResourceNotFoundException {
		m_instanceManager.removeWorkflow(wfId);
	}
	
	@Override
	public void removeWorkflowAll() {
		m_instanceManager.removeWorkflowAll();
	}
	
	@Override
	public String getWorkflowLog(String wfId, String podName) throws ResourceNotFoundException {
		return m_instanceManager.getWorkflowLog(wfId, podName);
	}
}

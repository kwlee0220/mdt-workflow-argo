package mdt.workflow.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;

import lombok.RequiredArgsConstructor;

import utils.func.Try;

import mdt.model.ResourceAlreadyExistsException;
import mdt.model.ResourceNotFoundException;
import mdt.workflow.MDTWorkflowManagerException;
import mdt.workflow.WorkflowModel;
import mdt.workflow.WorkflowModelManager;
import mdt.workflow.argo.ArgoWorkflowDescriptor;
import mdt.workflow.domain.JpaWorkflowModel;
import mdt.workflow.repository.JpaWorkflowModelRepository;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Service
@RequiredArgsConstructor
public class JpaWorkflowModelManager implements WorkflowModelManager {
	private final JpaWorkflowModelRepository m_repo;

	public WorkflowModel getWorkflowModel(String id) {
		return m_repo.findByModelId(id)
						.map(JpaWorkflowModel::asWorkflowModel)
						.orElseThrow(() -> new ResourceNotFoundException("Workflow model not found: id=" + id));
	}
	
    public List<WorkflowModel> getWorkflowModelAll() {
    	return m_repo.findAll().stream()
					.map(JpaWorkflowModel::asWorkflowModel)
					.toList();
    }
    
    public WorkflowModel addWorkflowModel(WorkflowModel wfModel) {
    	try {
			JpaWorkflowModel jpaEntity = new JpaWorkflowModel(wfModel);
			JpaWorkflowModel saved = m_repo.save(jpaEntity);
			return saved.asWorkflowModel();
		}
		catch ( DataIntegrityViolationException e ) {
			throw new ResourceAlreadyExistsException("WorkflowModel", wfModel.getId());	
		}
    }

	public WorkflowModel addOrReplaceWorkflowModel(WorkflowModel wfModel) {
		if ( m_repo.existsByModelId(wfModel.getId()) ) {
			m_repo.deleteByModelId(wfModel.getId());
		}
		
		JpaWorkflowModel saved = m_repo.save(new JpaWorkflowModel(wfModel));
		return saved.asWorkflowModel();
	}
    
    public void removeWorkflowModel(String id) {
		m_repo.deleteByModelId(id);
    }

    public void removeWorkflowModelAll() {
    	Try.run(m_repo::deleteAll);
    }

	private static final YAMLFactory YAML_FACTORY = new YAMLFactory().disable(Feature.WRITE_DOC_START_MARKER);
	public String getWorkflowScript(String id, String mdtEndpoint, String clientDockerImage)
		throws ResourceNotFoundException {
		WorkflowModel wfModel = getWorkflowModel(id);
		
		try {
			ArgoWorkflowDescriptor argoWfDesc = new ArgoWorkflowDescriptor(wfModel, mdtEndpoint, clientDockerImage);
			return JsonMapper.builder(YAML_FACTORY).build()
											.writerWithDefaultPrettyPrinter()
											.writeValueAsString(argoWfDesc);
		}
		catch ( JsonProcessingException e ) {
			throw new MDTWorkflowManagerException("fails to generate workflow script", e);
		}
	}
}

package mdt.workflow;

import java.io.IOException;
import java.util.List;

import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.api.WorkflowServiceApi;
import org.openapitools.client.model.IoArgoprojWorkflowV1alpha1Workflow;
import org.openapitools.client.model.IoArgoprojWorkflowV1alpha1WorkflowCreateRequest;
import org.openapitools.client.model.IoArgoprojWorkflowV1alpha1WorkflowList;
import org.openapitools.client.model.IoArgoprojWorkflowV1alpha1WorkflowResumeRequest;
import org.openapitools.client.model.IoArgoprojWorkflowV1alpha1WorkflowStopRequest;
import org.openapitools.client.model.IoArgoprojWorkflowV1alpha1WorkflowSuspendRequest;
import org.openapitools.client.model.StreamResultOfIoArgoprojWorkflowV1alpha1LogEntry;
import org.springframework.beans.factory.InitializingBean;

import lombok.experimental.Delegate;

import utils.KeyedValueList;
import utils.Utilities;
import utils.http.OkHttpClientUtils;
import utils.stream.FStream;

import mdt.model.MDTModelSerDe;
import mdt.model.ResourceNotFoundException;
import mdt.workflow.argo.ArgoWorkflowDescriptor;
import mdt.workflow.config.MDTWorkflowManagerConfiguration;
import mdt.workflow.model.TaskDescriptor;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class OpenApiArgoWorkflowManager implements WorkflowManager, InitializingBean {
	private static final IoArgoprojWorkflowV1alpha1WorkflowStopRequest STOP_REQUEST
															= new IoArgoprojWorkflowV1alpha1WorkflowStopRequest();
	private static final IoArgoprojWorkflowV1alpha1WorkflowSuspendRequest SUSPEND_REQUEST
															= new IoArgoprojWorkflowV1alpha1WorkflowSuspendRequest();
	private static final IoArgoprojWorkflowV1alpha1WorkflowResumeRequest RESUME_REQUEST
															= new IoArgoprojWorkflowV1alpha1WorkflowResumeRequest();
	
	@Delegate private final WorkflowModelManager m_modelManager;
	private final MDTWorkflowManagerConfiguration m_conf;
	private final String m_namespace;
	
	private WorkflowServiceApi m_wfApi;
	
	public OpenApiArgoWorkflowManager(WorkflowModelManager modelManager, MDTWorkflowManagerConfiguration conf) {
		m_modelManager = modelManager;
		m_conf = conf;
		m_namespace = conf.getArgoNamespace();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
	    ApiClient client = new ApiClient(OkHttpClientUtils.newTrustAllOkHttpClient());
	    client.setBasePath(m_conf.getArgoEndpoint());
	    m_wfApi = new WorkflowServiceApi(client);
	}

	@Override
	public List<Workflow> getWorkflowAll() {
		try {
			IoArgoprojWorkflowV1alpha1WorkflowList wfList =
					m_wfApi.workflowServiceListWorkflows(m_namespace, null, null, null, null, null,
															null, null, null, null, null, null, null);
			return FStream.from(wfList.getItems())
							// 가끔 dag가 정의되지 않는 workflow가 존재하고,
							// 이런 경우 제외시킨다.
							.filter(argoWf -> argoWf.getSpec().getTemplates().get(0).getDag() != null)
							.map(this::toWorkflow)
							.castSafely(Workflow.class)
							.toList();
		}
		catch ( ApiException e ) {
			throw new MDTWorkflowManagerException("fails to get workflow list", e);
		}
	}
	
	@Override
	public Workflow getWorkflow(String name) {
		try {
			IoArgoprojWorkflowV1alpha1Workflow argoWf =
					m_wfApi.workflowServiceGetWorkflow(m_namespace, name, null, null);
			return toWorkflow(argoWf);
		}
		catch ( ApiException e ) {
			switch ( e.getCode() ) {
				case 404:
					throw new ResourceNotFoundException("Workflow", "name=" + name);
				default:
					throw new MDTWorkflowManagerException("fails to get workflow: name=" + name, e);
			}
		}
	}

	@Override
	public void removeWorkflow(String name) {
		try {
			m_wfApi.workflowServiceDeleteWorkflow(m_namespace, name, null, null, null, null, null, null, null);
		}
		catch ( ApiException e ) {
			throw new MDTWorkflowManagerException("fails to remove workflow: name=" + name, e);
		}
	}

	@Override
	public void removeWorkflowAll() {
		try {
			IoArgoprojWorkflowV1alpha1WorkflowList wfList =
							m_wfApi.workflowServiceListWorkflows(m_namespace, null, null, null, null,
																null, null, null, null, null, null, null, null);
			FStream.from(wfList.getItems())
				.forEachOrIgnore(wf -> {
					String namespace = wf.getMetadata().getNamespace();
					String wfName = wf.getMetadata().getName();
					m_wfApi.workflowServiceDeleteWorkflow(namespace, wfName, null, null, null, null, null, null, null);
				});
		}
		catch ( ApiException e ) {
			throw new MDTWorkflowManagerException("fails to remove workflow all", e);
		}
	}

	@Override
	public Workflow startWorkflow(String wfModelId) throws ResourceNotFoundException {
		WorkflowModel wfModel = getWorkflowModel(wfModelId);
		try {
			// MDT Workflow 모델을 Argo Workflow로 변환하고 Json으로 변환한다.
			ArgoWorkflowDescriptor argoWfDesc = new ArgoWorkflowDescriptor(wfModel, m_conf.getMdtEndpoint(),
																			m_conf.getClientDockerImage());
			String wfSpecJson = MDTModelSerDe.getJsonMapper().writeValueAsString(argoWfDesc);
			
			// Argo Workflow 생성 요청 메시지를 생성한다.
			IoArgoprojWorkflowV1alpha1Workflow argoJson = IoArgoprojWorkflowV1alpha1Workflow.fromJson(wfSpecJson);
			IoArgoprojWorkflowV1alpha1WorkflowCreateRequest req = new IoArgoprojWorkflowV1alpha1WorkflowCreateRequest();
			req.setWorkflow(argoJson);
			
			// Argo Workflow를 생성한다 (시작한다).
			IoArgoprojWorkflowV1alpha1Workflow argoWf = m_wfApi.workflowServiceCreateWorkflow(m_namespace, req);
			return toWorkflow(argoWf);
		}
		catch ( IOException | ApiException e ) {
			throw new MDTWorkflowManagerException("fails to start workflow: model=" + wfModelId, e);
		}
	}

	@Override
	public void stopWorkflow(String name) throws ResourceNotFoundException {
		try {
			m_wfApi.workflowServiceStopWorkflow(m_namespace, name, STOP_REQUEST);
		}
		catch ( ApiException e ) {
			throw new MDTWorkflowManagerException("fails to stop workflow: name=" + name, e);
		}
	}

	@Override
	public Workflow suspendWorkflow(String name) throws ResourceNotFoundException {
		try {
			IoArgoprojWorkflowV1alpha1Workflow argoWf = m_wfApi.workflowServiceSuspendWorkflow(m_namespace, name,
																								SUSPEND_REQUEST);
			return toWorkflow(argoWf);
		}
		catch ( ApiException e ) {
			throw new MDTWorkflowManagerException("fails to suspend workflow: name=" + name, e);
		}
	}

	@Override
	public Workflow resumeWorkflow(String name) throws ResourceNotFoundException {
		try {
			IoArgoprojWorkflowV1alpha1Workflow argoWf = m_wfApi.workflowServiceResumeWorkflow(m_namespace, name,
																								RESUME_REQUEST);
			return toWorkflow(argoWf);
		}
		catch ( ApiException e ) {
			throw new MDTWorkflowManagerException("fails to resume workflow: name=" + name, e);
		}
	}

	@Override
	public String getWorkflowLog(String wfName, String podName) throws ResourceNotFoundException {
		try {
			podName = "thickness-simulation-long-sv4pj-1226849899";
			StreamResultOfIoArgoprojWorkflowV1alpha1LogEntry logEntry
								= m_wfApi.workflowServiceWorkflowLogs(m_namespace, wfName, null, null, null, null,
																	null, null, null, null, null, null, null, null, null);
			return logEntry.getResult().getContent();
		}
		catch ( ApiException e ) {
			String msg = String.format("fails to log workflow: name=%s, pod=%s", wfName, podName);
			throw new MDTWorkflowManagerException(msg, e);
		}
	}
	
	@Override
	public String toString() {
        return String.format("ArgoWorkflowManager[%s, namespace=%s, mdt=%s, client-docker=%s]",
        						m_conf.getArgoEndpoint(), m_conf.getArgoNamespace(),
        						m_conf.getMdtEndpoint(), m_conf.getClientDockerImage());
	}
	
	private Workflow toWorkflow(IoArgoprojWorkflowV1alpha1Workflow argoWf) {
		String modelId = Utilities.splitLast(argoWf.getMetadata().getName(), '-')._1;

		KeyedValueList<String, TaskDescriptor> taskDescList;
		try {
			WorkflowModel wfModel = getWorkflowModel(modelId);
			taskDescList = KeyedValueList.from(wfModel.getTaskDescriptors(), TaskDescriptor::getId);
		}
		catch ( ResourceNotFoundException expected ) {
			taskDescList = KeyedValueList.with(TaskDescriptor::getName);
		}

		return ArgoUtils.toWorkflow(argoWf, taskDescList);
		
	}
}

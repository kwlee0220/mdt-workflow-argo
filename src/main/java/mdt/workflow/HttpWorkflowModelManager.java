package mdt.workflow;

import java.io.IOException;
import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.google.common.base.Preconditions;

import utils.http.HttpClientProxy;
import utils.http.HttpRESTfulClient;
import utils.http.HttpRESTfulClient.ErrorEntityDeserializer;
import utils.http.HttpRESTfulClient.ResponseBodyDeserializer;
import utils.http.JacksonErrorEntityDeserializer;

import mdt.model.AASUtils;
import mdt.model.MDTModelSerDe;
import mdt.model.ResourceAlreadyExistsException;
import mdt.model.ResourceNotFoundException;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpWorkflowModelManager implements WorkflowModelManager, HttpClientProxy {
	private final String m_endpoint;
	private final HttpRESTfulClient m_restfulClient;
	
	public HttpWorkflowModelManager(OkHttpClient client, String endpoint) {
		m_endpoint = endpoint;
		ErrorEntityDeserializer errorDeser = new JacksonErrorEntityDeserializer(MDTModelSerDe.MAPPER);
		m_restfulClient = HttpRESTfulClient.builder()
											.httpClient(client)
											.errorEntityDeserializer(errorDeser)
											.build();
	}

	@Override
	public String getEndpoint() {
		return m_endpoint;
	}

	@Override
	public OkHttpClient getHttpClient() {
		return m_restfulClient.getHttpClient();
	}

	@Override
	public List<WorkflowModel> getWorkflowModelAll() {
		String url = String.format("%s/models", m_endpoint);
		return m_restfulClient.get(url, m_wfModelListDeser);
	}

	@Override
	public WorkflowModel getWorkflowModel(@NonNull String id) throws ResourceNotFoundException {
		Preconditions.checkArgument(id != null, "WorkflowModel id is null");
		
		String url = String.format("%s/models/%s", m_endpoint, id);
		return m_restfulClient.get(url, m_wfModelDeser);
	}

	@Override
	public String getWorkflowScript(String id, String mdtEndpoint, String clientDockerImage)
		throws ResourceNotFoundException {
		StringBuilder builder = new StringBuilder();
		if ( mdtEndpoint != null ) {
			builder.append("mdtEndpoint=")
					.append(AASUtils.encodeBase64UrlSafe(mdtEndpoint));
		}
		if ( clientDockerImage != null ) {
			if ( builder.length() > 0 ) {
				builder.append("&");
			}
			builder.append("clientDockerImage=")
					.append(AASUtils.encodeBase64UrlSafe(clientDockerImage));
		}
		
		String paramsStr = builder.length() > 0 ? "?" + builder.toString() : "";
		String url = String.format("%s/models/%s%s", m_endpoint, id, paramsStr);
		return m_restfulClient.get(url, HttpRESTfulClient.STRING_DESER);
	}

	@Override
	public String addWorkflowModel(WorkflowModel desc) throws ResourceAlreadyExistsException {
		String requestJson = MDTModelSerDe.toJsonString(desc);
		RequestBody reqBody = RequestBody.create(requestJson, HttpRESTfulClient.MEDIA_TYPE_JSON);
		return m_restfulClient.post(m_endpoint, reqBody, HttpRESTfulClient.STRING_DESER);
	}

	@Override
	public String addOrUpdateWorkflowModel(WorkflowModel desc) {
		String url = String.format("%s/models?updateIfExists=true", m_endpoint);
		
		String requestJson = MDTModelSerDe.toJsonString(desc);
		RequestBody reqBody = RequestBody.create(requestJson, HttpRESTfulClient.MEDIA_TYPE_JSON);
		return m_restfulClient.post(url, reqBody, HttpRESTfulClient.STRING_DESER);
	}

	@Override
	public void removeWorkflowModel(String id) throws ResourceNotFoundException {
		String url = String.format("%s/models/%s", m_endpoint, id);
		m_restfulClient.delete(url);
	}

	@Override
	public void removeWorkflowModelAll() {
		String url = String.format("%s/models", m_endpoint);
		m_restfulClient.delete(url);
	}
	
	@Override
	public String toString() {
		return String.format("HttpWorkflowModelManager: endpoint=%s", m_endpoint);
	}
	
	private ResponseBodyDeserializer<WorkflowModel> m_wfModelDeser = new ResponseBodyDeserializer<>() {
		@Override
		public WorkflowModel deserialize(Headers headers, String respBody) throws IOException {
			return MDTModelSerDe.getJsonMapper().readValue(respBody, WorkflowModel.class);
		}
	};
	private ResponseBodyDeserializer<List<WorkflowModel>> m_wfModelListDeser = new ResponseBodyDeserializer<>() {
		@Override
		public List<WorkflowModel> deserialize(Headers headers, String respBody) throws IOException {
			return MDTModelSerDe.readValueList(respBody, WorkflowModel.class);
		}
	};
}

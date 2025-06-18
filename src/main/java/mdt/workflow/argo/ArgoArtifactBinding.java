package mdt.workflow.argo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import utils.Named;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
public class ArgoArtifactBinding implements Named {
	@JsonProperty("name") private final String m_name;
	@JsonProperty("from") private final String m_from;
	
	public ArgoArtifactBinding(@JsonProperty("name") String name,
								@JsonProperty("from") String from) {
		m_name = name;
		m_from = from;
	}

	@Override
	public String getName() {
		return m_name;
	}

	public String getFrom() {
		return m_from;
	}
}

package mdt.workflow.argo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

import utils.func.FOption;

import mdt.model.NameValue;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
public class ArgoArgumentsDescriptor {
	private List<NameValue> m_parameters;
	
	@JsonCreator
	public ArgoArgumentsDescriptor(@JsonProperty("parameters") List<NameValue> parameters) {
		m_parameters = FOption.getOrElse(parameters, Lists::newArrayList);
	}
	
	public List<NameValue> getParameters() {
		return m_parameters;
	}
	
	@JsonProperty("parameters")
	public List<NameValue> getParametersForJson() {
		return (m_parameters.size() > 0) ? m_parameters : null;
	}
	
	public int size() {
		return m_parameters.size();
	}
}

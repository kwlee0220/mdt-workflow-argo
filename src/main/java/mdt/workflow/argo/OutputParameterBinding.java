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
public class OutputParameterBinding implements Named {
	private final String m_name;
	private final ValueFrom m_valueFrom;
	
	public OutputParameterBinding(@JsonProperty("name") String name,
								@JsonProperty("valueFrom") ValueFrom valueFrom) {
		m_name = name;
		m_valueFrom = valueFrom;
	}

	@Override
	public String getName() {
		return m_name;
	}

	public ValueFrom getValueFrom() {
		return m_valueFrom;
	}
	
	public static class ValueFrom {
		private final String m_path;
		
		public ValueFrom(@JsonProperty("path") String path) {
			m_path = path;
		}
		
		public String getPath() {
			return m_path;
		}
	}
}

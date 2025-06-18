package mdt.workflow.argo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NonNull;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter
@JsonInclude(Include.NON_NULL)
public class ArgoDagTemplateDescriptor implements ArgoTemplateDescriptor {
	@NonNull private String name;
	@NonNull private DagDescriptor dag;
	
	@JsonCreator
	public ArgoDagTemplateDescriptor(@JsonProperty("name") @NonNull String name,
									@JsonProperty("dag") @NonNull DagDescriptor dag) {
		this.name = name;
		this.dag = dag;
	}
	
	@Getter
	public static class DagDescriptor {
		@NonNull private List<ArgoTaskDescriptor> tasks;
		
		@JsonCreator
		public DagDescriptor(@JsonProperty("tasks") @NonNull List<ArgoTaskDescriptor> tasks) {
			this.tasks = tasks;
		}
	}
}

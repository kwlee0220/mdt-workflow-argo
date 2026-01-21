package mdt.workflow.airflow;

import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import utils.stream.KeyValueFStream;

import mdt.workflow.model.TaskDescriptor;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
@Accessors(prefix = "m_")
public class AASOperationTaskSpec extends TaskSpec {
	private final String m_instanceId;
	private final String m_submodelId;
	
	public AASOperationTaskSpec(String taskId, String instanceId, String submodelId,
								List<TaskArgument> inputs, List<TaskArgument> outputs, Set<String> dependencies) {
		super(taskId, inputs, outputs, dependencies);
		
		m_instanceId = instanceId;
		m_submodelId = submodelId;
	}
	
	public static AASOperationTaskSpec from(TaskDescriptor task) {
		String opPath=task.getOptions().get("operation").getValue();
		String[] parts = opPath.split(":");
		String instanceId = parts[0];
		String submodelIdShort = parts[1];
		
		List<TaskArgument> inputs = KeyValueFStream.from(task.getInputArgumentSpecs())
													.map((argId, arg) -> fromVariable(argId, arg))
													.toList();
		List<TaskArgument> outputs = KeyValueFStream.from(task.getOutputArgumentSpecs())
													.map((argId, arg) -> fromVariable(argId, arg))
													.toList();
		
		return new AASOperationTaskSpec(task.getId(),
							instanceId,
							submodelIdShort,
							inputs, outputs,
							task.getDependencies());

	}
}

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
public class SetTaskSpec extends TaskSpec {
	public SetTaskSpec(String taskId, List<TaskArgument> inputs, List<TaskArgument> outputs, Set<String> dependencies) {
		super(taskId, inputs, outputs, dependencies);
	}
	
	public static SetTaskSpec from(TaskDescriptor task) {
		List<TaskArgument> inputs = KeyValueFStream.from(task.getInputArgumentSpecs())
													.map((argId, arg) -> fromVariable(argId, arg))
													.toList();
		List<TaskArgument> outputs = KeyValueFStream.from(task.getOutputArgumentSpecs())
													.map((argId, arg) -> fromVariable(argId, arg))
													.toList();
		
		return new SetTaskSpec(task.getId(), inputs, outputs, task.getDependencies());

	}
}

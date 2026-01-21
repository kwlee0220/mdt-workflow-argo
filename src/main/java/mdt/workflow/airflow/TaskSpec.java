package mdt.workflow.airflow;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import utils.stream.FStream;

import mdt.task.builtin.AASOperationTask;
import mdt.task.builtin.SetTask;
import mdt.workflow.model.ArgumentSpec;
import mdt.workflow.model.ArgumentSpec.LiteralArgumentSpec;
import mdt.workflow.model.ArgumentSpec.ReferenceArgumentSpec;
import mdt.workflow.model.TaskDescriptor;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
@Accessors(prefix = "m_")
public class TaskSpec {
	private final String m_taskId;
	private final List<TaskArgument> m_inputs;
	private final List<TaskArgument> m_outputs;
	private final Set<String> m_dependencies;
	
	public TaskSpec(String taskId, List<TaskArgument> inputs, List<TaskArgument> outputs, Set<String> dependencies) {
		Preconditions.checkArgument(PythonIdentifierUtil.isValidPythonIdentifier(taskId),
									"invalid Task id: %s (invalid Python identifier)", taskId);
		m_taskId = taskId;
		m_inputs = inputs;
		m_outputs = outputs;
		m_dependencies = FStream.from(dependencies).map(d -> d.replaceAll("-", "_")).toSet();
	}
	
	public String getTaskType() {
		return this.getClass().getSimpleName();
	}
	
	public static TaskSpec from(TaskDescriptor task) {
		if ( AASOperationTask.class.getName().equals(task.getType()) ) {
			return AASOperationTaskSpec.from(task);
		}
		else if ( SetTask.class.getName().equals(task.getType()) ) {
			return SetTaskSpec.from(task);
		}
		else {
			throw new IllegalArgumentException("unsupported task type: " + task.getType());
		}
	}
	
	protected static TaskArgument fromVariable(String argId, ArgumentSpec argSpec) {
		if ( argSpec instanceof ReferenceArgumentSpec refArgSpec ) {
			return new TaskArguments.ReferenceArgument(argId, refArgSpec.getElementReference().toStringExpr());
		}
		else if ( argSpec instanceof LiteralArgumentSpec literalSpec ) {
			try {
				String jsonStr = literalSpec.readValue().toValueJsonString();
				String pythonExpr = JsonToPythonLiteralHybrid.convertToPythonLiteralHybrid(jsonStr);
				return new TaskArguments.LiteralArgument(argId, pythonExpr);
			}
			catch ( IOException e ) {
				throw new RuntimeException("failed to convert variable to JSON: " + argId, e);
			}
		}
		else {
			throw new IllegalArgumentException("unsupported variable: " + argSpec);
		}
	}
}

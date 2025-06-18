package mdt.workflow.argo;

import java.util.List;

import com.google.common.collect.Lists;

import utils.func.Funcs;
import utils.stream.FStream;

import mdt.model.NameValue;
import mdt.model.sm.variable.AbstractVariable.ReferenceVariable;
import mdt.model.sm.variable.AbstractVariable.ValueVariable;
import mdt.model.sm.variable.Variable;
import mdt.workflow.WorkflowModel;
import mdt.workflow.argo.ArgoContainerTemplateDescriptor.ContainerDescriptor;
import mdt.workflow.argo.ArgoDagTemplateDescriptor.DagDescriptor;
import mdt.workflow.model.TaskDescriptor;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ArgoTemplateDescriptorLoader {
//	private static final String MDT_CLIENT_IMAGE_ID = "kwlee0220/mdt-client";
	private static final List<String> COMMAND_JAVA = List.of("java");
	
	private final WorkflowModel m_wfDesc;
	private final String m_mdtEndpoint;
	private final String m_mdtClientImageName;
	
	public ArgoTemplateDescriptorLoader(WorkflowModel wfDesc, String mdtEndpoint, String mdtClientImageName) {
		m_wfDesc = wfDesc;
		m_mdtEndpoint = mdtEndpoint;
		m_mdtClientImageName = mdtClientImageName;
	}
	
	public List<ArgoTemplateDescriptor> load() {
		List<ArgoTemplateDescriptor> argoTemplates = Lists.newArrayList();
		FStream.from(m_wfDesc.getTaskDescriptors())
				.map(this::toArgoContainerTemplate)
				.toCollection(argoTemplates);
		
		List<ArgoTaskDescriptor> argoTasks = Funcs.map(m_wfDesc.getTaskDescriptors(), this::toArgoTask);
		ArgoDagTemplateDescriptor dagTemplate = new ArgoDagTemplateDescriptor("dag",
																		new DagDescriptor(argoTasks));
		argoTemplates.add(0, dagTemplate);
		
		return argoTemplates;
	}
	
	private ArgoTaskDescriptor toArgoTask(TaskDescriptor task) {
		String tmpltId = task.getId() + "-template";
		return new ArgoTaskDescriptor(task.getId(), tmpltId, task.getDependencies());
	}
	
	private ArgoContainerTemplateDescriptor toArgoContainerTemplate(TaskDescriptor task) {
		ContainerDescriptor container = toContainerDescriptor(task);
		return new ArgoContainerTemplateDescriptor(task.getId()+"-template", container);
	}
	
	private ContainerDescriptor toContainerDescriptor(TaskDescriptor task) {
		String taskType = task.getType();
		
		List<String> args = Lists.newArrayList("-cp", "mdt-client-all.jar", taskType + "Runner");
		args.addAll(task.toEncodedString());

//		if ( taskType.equals(SetTask.class.getName()) ) {
//			addSetTaskParameters(task, args);
//		}
//		
//		FStream.from(task.getOptions())
//				.flatMapIterable(Option::toCommandOptionSpec)
//				.forEach(args::add);
//		
//		// 'VARIABLE' type의 input port의 경우에는 dependent task에서 생성한 output parameter 값을
//		// command line 인자로 전달된다.
//		
//		if ( taskType.equals(SetTask.class.getName()) ) {
//			addSetTaskOptions(task, args);
//		}
//		else {
//			for ( Variable inVar: task.getInputVariables() ) {
//				args.add(String.format("--in.%s", inVar.getName()));
//				args.add(toVariableString(inVar));
//			}
//			for ( Variable outVar: task.getOutputVariables() ) {
//				args.add(String.format("--out.%s", outVar.getName()));
//				args.add(toVariableString(outVar));
//			}
//		}
		
		List<NameValue> environs = List.of(
			new NameValue("MDT_ENDPOINT", m_mdtEndpoint)
		);

		return new ContainerDescriptor(m_mdtClientImageName, COMMAND_JAVA, args, environs);
	}
	
	private void addSetTaskParameters(TaskDescriptor task, List<String> args) {
		Variable tar = task.getOutputVariables().getOfKey("target");
		args.add(toVariableString(tar));
		
		Variable src = task.getInputVariables().getOfKey("source");
		args.add("--value");
		args.add(toVariableString(src));
	}
	private void addSetTaskOptions(TaskDescriptor task, List<String> args) { }
	
	private String toVariableString(Variable var) {
		if ( var instanceof ReferenceVariable refVar ) {
			return refVar.getReference().toStringExpr();
		}
		if ( var instanceof ValueVariable vvar ) {
			return vvar.readValue().toString();
		}
		else {
			throw new IllegalArgumentException("Invalid TaskPort type: " + var.getClass());
		}
	}
}

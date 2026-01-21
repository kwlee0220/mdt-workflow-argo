package mdt.workflow.argo;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import utils.func.Funcs;
import utils.stream.FStream;
import utils.stream.KeyValueFStream;

import mdt.model.NameValue;
import mdt.task.builtin.AASOperationTask;
import mdt.task.builtin.HttpTask;
import mdt.task.builtin.ProgramTask;
import mdt.task.builtin.SetTask;
import mdt.workflow.WorkflowModel;
import mdt.workflow.argo.ArgoContainerTemplateDescriptor.ContainerDescriptor;
import mdt.workflow.argo.ArgoDagTemplateDescriptor.DagDescriptor;
import mdt.workflow.model.ArgumentSpec;
import mdt.workflow.model.ArgumentSpec.LiteralArgumentSpec;
import mdt.workflow.model.ArgumentSpec.ReferenceArgumentSpec;
import mdt.workflow.model.Option;
import mdt.workflow.model.TaskDescriptor;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ArgoTemplateDescriptorLoader {
	private static final Logger s_logger = LoggerFactory.getLogger(ArgoTemplateDescriptorLoader.class);
	
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
	
	private static final String MDT_CLIENT_JAR_FILE = "../mdt-client-all.jar";
	
	private ContainerDescriptor toContainerDescriptor(TaskDescriptor task) {
		List<String> args = Lists.newArrayList("-cp", MDT_CLIENT_JAR_FILE, "mdt.cli.MDTCommandsMain", "run");
		
		String taskType = task.getType();
		if ( SetTask.class.getName().equals(taskType) ) {
			args.add("set");
		}
		else {
			args.add("submodel"); args.add(task.getSubmodelRef().toStringExpr());
			if ( AASOperationTask.class.getName().equals(taskType) ) {
				args.add("aas");
			}
			else if ( HttpTask.class.getName().equals(taskType) ) {
				args.add("http");
			}
			else if ( ProgramTask.class.getName().equals(taskType) ) {
				args.add("program");
			}
			else {
				throw new IllegalArgumentException("Unsupported task type: " + taskType);
			}
		}
		
		KeyValueFStream.from(task.getInputArgumentSpecs())
						.forEach((id, arg) -> {
							args.add(String.format("--in.%s", id));
							args.add(toArgumentSpecString(arg));
						});
		KeyValueFStream.from(task.getOutputArgumentSpecs())
						.forEach((id, arg) -> {
							args.add(String.format("--out.%s", id));
							args.add(toArgumentSpecString(arg));
						});
		
		for ( Option opt: task.getOptions().values() ) {
			// option의 이름이 timeout인 경우 null 또는 ""인 경우에는 argument 로 추가하지 않음.
			switch ( opt.getName() ) {
				case "timeout":
					if ( opt.getValue() == null || opt.getValue().equals("") ) {
						continue;
					}
					break;
			}
			opt.toCommandOptionSpec().stream().forEach(args::add);
		}
		
		List<NameValue> environs = List.of(
			new NameValue("MDT_ENDPOINT", m_mdtEndpoint)
		);

//		String argsStr = FStream.from(args).drop(2).join(' ');
//		System.out.println("java -cp $MDT_HOME/mdt-client/mdt-client-all.jar " + argsStr);

		return new ContainerDescriptor(m_mdtClientImageName, COMMAND_JAVA, args, environs);
	}
	
	private void addSetTaskParameters(TaskDescriptor task, List<String> args) {
		ArgumentSpec tar = task.getOutputArgumentSpecs().get("target");
		args.add(toArgumentSpecString(tar));
		
		ArgumentSpec src = task.getInputArgumentSpecs().get("source");
		args.add("--value");
		args.add(toArgumentSpecString(src));
	}
	private void addSetTaskOptions(TaskDescriptor task, List<String> args) { }
	
	private String toArgumentSpecString(ArgumentSpec spec) {
		if ( spec instanceof ReferenceArgumentSpec refSpec ) {
			return refSpec.getElementReference().toStringExpr();
		}
		if ( spec instanceof LiteralArgumentSpec litSpec ) {
			return litSpec.readValue().toString();
		}
		else {
			throw new IllegalArgumentException("Invalid TaskPort type: " + spec.getClass());
		}
	}
}

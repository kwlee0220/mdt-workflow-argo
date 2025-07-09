package mdt.workflow;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import org.openapitools.client.model.IoArgoprojWorkflowV1alpha1NodeStatus;
import org.openapitools.client.model.IoArgoprojWorkflowV1alpha1Workflow;

import com.google.common.collect.Sets;

import lombok.experimental.UtilityClass;

import utils.KeyedValueList;
import utils.Utilities;
import utils.func.FOption;
import utils.stream.FStream;
import utils.stream.KeyValueFStream;

import mdt.workflow.model.TaskDescriptor;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class ArgoUtils {
	public static WorkflowStatus toWorkflowStatus(String status) {
		if ( status == null ) {
			return WorkflowStatus.NOT_STARTED;
		}
		
		switch ( status ) {
			case "Running":
				return WorkflowStatus.RUNNING;
			case "Succeeded":
				return WorkflowStatus.COMPLETED;
			case "Failed":
				return WorkflowStatus.FAILED;
			case "Pending":
				return WorkflowStatus.STARTING;
			case "Skipped":
				return WorkflowStatus.NOT_STARTED;
			default:
				System.err.println("Unknown workflow status: " + status);
				return WorkflowStatus.UNKNOWN;
		}
	}
	
	private static <K,T> T getFirst(List<T> list) {
		return list.isEmpty() ? null : list.get(0);
	}

	public static Workflow toWorkflow(IoArgoprojWorkflowV1alpha1Workflow argoWf,
										KeyedValueList<String,TaskDescriptor> taskDescList) {
		// Dependency 관계를 생성한다.
		Map<String,List<String>> statusDependencies
							= FStream.from(argoWf.getSpec().getTemplates().get(0).getDag().getTasks())
									.toKeyValueStream(dagTask -> dagTask.getName(), dagTask -> dagTask.getDependencies())
									.toMap();
		
		// Task status 매핑을 생성한다.
		Map<String,IoArgoprojWorkflowV1alpha1NodeStatus> taskStatusMap
														= FStream.from(argoWf.getStatus().getNodes().values())
																.filter(nt -> nt.getType().equals("Pod"))
																.tagKey(nt -> Utilities.split(nt.getName(), '.')._2)
																.toMap();
		
		String wfName = argoWf.getMetadata().getName();
		
		List<NodeTask> nodeTaskList
					= taskDescList.fstream()
				                    .outerJoin(KeyValueFStream.from(taskStatusMap))
				    		        .map(kv -> {
				    		        	var pair = kv.value();
				    		        	TaskDescriptor desc = getFirst(pair._1());
				    		        	IoArgoprojWorkflowV1alpha1NodeStatus status = getFirst(pair._2());
				    		        	List<String> statusDeps = (status != null)
				    		        							? statusDependencies.get(status.getDisplayName())
				    		        							: null;
				    		        	return toNodeTask(desc, status, statusDeps);
				    		        })
				    		        .toList();
		
		LocalDateTime created = FOption.map(argoWf.getMetadata().getCreationTimestamp(), OffsetDateTime::toLocalDateTime);
		LocalDateTime started = FOption.map(argoWf.getStatus().getStartedAt(), OffsetDateTime::toLocalDateTime);
		LocalDateTime finished = FOption.map(argoWf.getStatus().getFinishedAt(), OffsetDateTime::toLocalDateTime);
		
		return Workflow.builder()
						.name(wfName)
						.status(toWorkflowStatus(argoWf.getStatus().getPhase()))
						.creationTime(created)
						.startTime(started)
						.finishTime(finished)
						.tasks(nodeTaskList)
						.build();
	}
	
	private static NodeTask toNodeTask(TaskDescriptor task,
										IoArgoprojWorkflowV1alpha1NodeStatus status, List<String> statusDeps) {
		if ( status != null ) {
			String taskId = status.getDisplayName();
			
			LocalDateTime started = FOption.map(status.getStartedAt(), OffsetDateTime::toLocalDateTime);
			LocalDateTime finished = FOption.map(status.getFinishedAt(), OffsetDateTime::toLocalDateTime);
			
			WorkflowStatus wstatus = toWorkflowStatus(status.getPhase());
			return new NodeTask(taskId, wstatus, Sets.newHashSet(statusDeps), started, finished);
		}
		else {
			return new NodeTask(task.getId(), WorkflowStatus.NOT_STARTED,
									task.getDependencies(), null, null);
		}
	}
}
package mdt.test;

import mdt.workflow.OpenApiArgoWorkflowManager;
import mdt.workflow.Workflow;
import mdt.workflow.config.MDTWorkflowManagerConfiguration;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestOpenApiArgoWorkflowManager {
	private static String ARGO_ENDPOINT = "https://129.254.89.182:18080";
	private static String ARGO_NAMESPACE = "default";
	private static String MDT_ENDPOINT = "http://129.254.91.134:12985";
	private static String CLIENT_DOCKER_IMAGE = "kwlee0220/mdt-client:latest";
	
	public static void main(String... args) throws Exception {
		
		MDTWorkflowManagerConfiguration conf = new MDTWorkflowManagerConfiguration();
		conf.setArgoEndpoint(ARGO_ENDPOINT);
		conf.setArgoNamespace(ARGO_NAMESPACE);
		conf.setMdtEndpoint(MDT_ENDPOINT);
		conf.setClientDockerImage(CLIENT_DOCKER_IMAGE);
		
		OpenApiArgoWorkflowManager wfMgr = new OpenApiArgoWorkflowManager(null, conf);
		wfMgr.afterPropertiesSet();
		
//		for ( Workflow wf: wfMgr.getWorkflowAll() ) {
//			System.out.println(wf);
//		}
		
		String wfName = "thickness-simulation-short-rbz9z";
		Workflow wf = wfMgr.getWorkflow(wfName);
		System.out.println(wf);
//		wfMgr.removeWorkflow(wfName);
		
//		FStream.from(wfMgr.getWorkflowAll())
//		    .filter(w -> w.getModelId().equals("thickness-simulation-short"))
//		    .forEach(w -> System.out.println(w));
//
//		FStream.from(wfMgr.getWorkflowAll())
//				.groupByKey(w -> w.getModelId())
//				.stream()
//				.forEach(kv -> {
//					System.out.printf("%s: %d%n", kv.key(), kv.value().size());
//				});
		
//		for ( WorkflowModel wfModel: wfMgr.getWorkflowModelAll() ) {
//			System.out.println(wfModel);
//		}
	}
}

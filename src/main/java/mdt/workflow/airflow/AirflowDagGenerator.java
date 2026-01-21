package mdt.workflow.airflow;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.loader.ClasspathLoader;
import io.pebbletemplates.pebble.template.PebbleTemplate;

import mdt.workflow.WorkflowModel;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class AirflowDagGenerator {
	public static DagSpec generate(WorkflowModel wfDesc, Writer writer) throws IOException {
        ClasspathLoader loader = new ClasspathLoader();
        loader.setPrefix("templates");
        loader.setCharset(StandardCharsets.UTF_8.name());

        PebbleEngine engine = new PebbleEngine.Builder()
								                .loader(loader)
								                .autoEscaping(false)
								                .build();
        PebbleTemplate template = engine.getTemplate("airflow_dag.peb");
        
		DagSpec dagSpec = new DagSpec(wfDesc);
        template.evaluate(writer, Map.of("dag", dagSpec));
        writer.flush();

        return dagSpec;
	}
	
	public static final void main(String[] args) throws IOException {
//		File wfFile = new File("/home/kwlee/mdt/models/test/wf-test2.json");
		File wfFile = new File("/home/kwlee/mdt/models/innercase/inspector/wf_inspector_simulation.json");
		WorkflowModel wfDesc = WorkflowModel.parseJsonFile(wfFile);
		
        StringWriter writer = new StringWriter();
        try ( writer ) {
        	DagSpec dag = generate(wfDesc, writer);
        }
		System.out.println(writer.toString());
	}
}

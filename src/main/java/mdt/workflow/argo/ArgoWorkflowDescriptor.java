package mdt.workflow.argo;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;

import lombok.Getter;
import lombok.NonNull;

import utils.InternalException;

import mdt.model.NameValue;
import mdt.workflow.WorkflowModel;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter
public class ArgoWorkflowDescriptor {
	private final String apiVersion = "argoproj.io/v1alpha1";
	private final String kind = "Workflow";
	private final Metadata metadata;
	private final Spec spec;

	public ArgoWorkflowDescriptor(WorkflowModel wfDesc, String mdtEndpoint, String mdtClientImageName) {
		this.metadata = new Metadata(wfDesc.getId().toLowerCase() + "-");
		
		String paramMdtEndpoint = "{{workflow.parameters.mdt-endpoint}}";
		String paramClientImage = "{{workflow.parameters.mdt-client-image}}";
		ArgoTemplateDescriptorLoader loader = new ArgoTemplateDescriptorLoader(wfDesc, paramMdtEndpoint,
																				paramClientImage);
		
		Arguments args = new Arguments(
			List.of(
				NameValue.of("mdt-endpoint", mdtEndpoint),
				NameValue.of("mdt-client-image", mdtClientImageName)
			)
		);
		List<ArgoTemplateDescriptor> templates = loader.load();
		this.spec = new Spec(wfDesc, args, templates);
	}
	
	@Getter
	public static class Metadata {
		private final String generateName;
		
		public Metadata(@JsonProperty("generateName") String generateName) {
			this.generateName = generateName;
		}
	}
	
	@Getter
	public class Spec {
		@NonNull private final String entrypoint;
		private final Arguments arguments;
		@NonNull private final List<ArgoTemplateDescriptor> templates;
		
		public Spec(WorkflowModel wfDesc, Arguments arguments, List<ArgoTemplateDescriptor> templates) {
			this.entrypoint = "dag";
			this.arguments = arguments;
			this.templates = templates;
		}
	}

	@Getter
	public class Arguments {
		private final List<NameValue> parameters;
		
		public Arguments(List<NameValue> parameters) {
			this.parameters = parameters;
		}
	}
	
	public String toYamlString(boolean prettyPrint) throws JsonProcessingException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(10240);
			toYaml(baos, prettyPrint);
			return baos.toString();
		}
		catch ( JsonProcessingException e ) {
			throw e;
		}
		catch ( IOException e ) {
			throw new InternalException("" + e);
		}
	}
	
	public void toYaml(OutputStream os, boolean prettyPrint) throws IOException {
		try ( BufferedOutputStream bos = new BufferedOutputStream(os) ) {
			YAMLFactory yamlFact = new YAMLFactory().disable(Feature.WRITE_DOC_START_MARKER)
													.enable(Feature.MINIMIZE_QUOTES);
			ObjectMapper mapper = new ObjectMapper(yamlFact);
			if ( prettyPrint ) {
				mapper.writerWithDefaultPrettyPrinter()
						.writeValue(bos, this);
			}
			else {
				mapper.writeValue(bos, this);
			}
		}
	}
}

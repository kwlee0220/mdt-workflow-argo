package mdt.workflow.argo;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import utils.func.FOption;
import utils.stream.FStream;

import lombok.NonNull;
import mdt.model.MDTModelSerDe;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
public class ArgoTaskDescriptor {
	@JsonProperty("name") private final String m_name;
	@JsonProperty("template") @NonNull private final String m_template;
	@JsonProperty("dependencies") private final Set<String> m_dependencies;

	@JsonCreator
	public ArgoTaskDescriptor(@JsonProperty("name") @NonNull String name,
								@JsonProperty("template") @NonNull String template,
								@JsonProperty("dependencies") Set<String> dependencies) {
		Preconditions.checkArgument(name != null, "Null name");
		Preconditions.checkArgument(template != null, "Null template");
		
		this.m_name = name;
		this.m_template = template;
		this.m_dependencies = dependencies;
	}

	public String getName() {
		return m_name;
	}

	public String getTemplate() {
		return m_template;
	}

	public Set<String> getDependencies() {
		return m_dependencies;
	}
	
	@Override
	public String toString() {
		String depsStr = FStream.from(FOption.getOrElse(m_dependencies, Set.of())).join(", ");
		return String.format("%s (dependents: {%s})", m_name, depsStr);
	}
	
	public static final void main(String... args) throws Exception {
		ArgoTaskDescriptor desc = new ArgoTaskDescriptor("taskName", "templateId", Set.of("pred1", "pred2"));
		String jsonStr = MDTModelSerDe.toJsonString(desc);
		System.out.println(jsonStr);
		
		String jsonStr2 = """
		{
			"name": "task2",
			"template": "template2",
			"dependencies": ["a", "b", "c"]
		}""";
		System.out.println(MDTModelSerDe.readValue(jsonStr2, ArgoTaskDescriptor.class));
		
		String jsonStr3 = """
		{
			"name": "task3",
			"template": "template2",
			"dependencies": []
		}""";
		System.out.println(MDTModelSerDe.readValue(jsonStr3, ArgoTaskDescriptor.class));
		
		String jsonStr4 = """
		{
			"name": "task4",
			"template": "template2",
			"dependencies": null
		}""";
		System.out.println(MDTModelSerDe.readValue(jsonStr4, ArgoTaskDescriptor.class));
		
		String jsonStr5 = """
		{
			"name": "task4",
			"template": "template2"
		}""";
		System.out.println(MDTModelSerDe.readValue(jsonStr5, ArgoTaskDescriptor.class));
	}
}

package mdt.workflow.domain;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.Getter;
import lombok.Setter;

import utils.InternalException;

import mdt.model.MDTModelSerDe;
import mdt.workflow.WorkflowModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Entity
@Table(name="workflow_models")
@Getter @Setter
public class JpaWorkflowModel {
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="row_id") private Long rowId;

	@Column(name="id", length=64, unique=true) private String id;
	@Column(columnDefinition = "bytea", nullable = false)
	private byte[] jsonModelBytes;
	
	@SuppressWarnings("unused")
	private JpaWorkflowModel() { }
	
	public JpaWorkflowModel(WorkflowModel wfModel) {
		this.id = wfModel.getId();
		this.jsonModelBytes = MDTModelSerDe.toJsonString(wfModel).getBytes(StandardCharsets.UTF_8);
	}
	
	public JpaWorkflowModel(String wfModelJson) throws JsonProcessingException {
		WorkflowModel wfDesc = WorkflowModel.parseJsonString(wfModelJson);
		
		this.id = wfDesc.getId();
		this.jsonModelBytes = wfModelJson.getBytes(StandardCharsets.UTF_8);
	}
	
	public WorkflowModel asWorkflowModel() {
		try {
			String jsonModel = new String(jsonModelBytes, StandardCharsets.UTF_8);
			return MDTModelSerDe.readValue(jsonModel, WorkflowModel.class);
		}
		catch ( IOException e ) {
			throw new InternalException(e);
		}
	}
}
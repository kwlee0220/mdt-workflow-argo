package mdt.workflow;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;

import utils.InternalException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import mdt.model.MDTModelSerDe;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
@Entity
@Table(
	name="workflow_models",
	indexes = {
		@Index(name="id_idx", columnList="id", unique=true)
	})
public class JpaWorkflowModel {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="row_id") private Long rowId;

	@Column(name="id", length=64, unique=true) private String id;
	@Lob @Column(name="json_model") private String jsonModel;
	@Transient @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE) private WorkflowModel wfModel = null;
	
	@SuppressWarnings("unused")
	private JpaWorkflowModel() { }
	
	public JpaWorkflowModel(WorkflowModel wfModel) {
		this.id = wfModel.getId();
		this.jsonModel = MDTModelSerDe.toJsonString(wfModel);
		this.wfModel = wfModel;
	}
	
	public JpaWorkflowModel(String wfModelJson) throws JsonProcessingException {
		WorkflowModel wfDesc = WorkflowModel.parseJsonString(wfModelJson);
		
		this.id = wfDesc.getId();
		this.jsonModel = wfModelJson;
		this.wfModel = wfDesc;
	}
	
	public WorkflowModel getWorkflowModel() {
		if ( this.wfModel == null ) {
			try {
				this.wfModel = MDTModelSerDe.readValue(jsonModel, WorkflowModel.class);
			}
			catch ( IOException e ) {
				throw new InternalException(e);
			}
		}
		return this.wfModel;
	}
}
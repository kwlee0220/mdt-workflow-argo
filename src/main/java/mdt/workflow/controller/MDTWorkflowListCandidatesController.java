package mdt.workflow.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import utils.Utilities;
import utils.stream.FStream;

import mdt.workflow.WorkflowStatus;
import mdt.workflow.service.MDTWorkflowManager;


/**
*
* @author Kang-Woo Lee (ETRI)
*/
@RestController
@RequestMapping(value={"/list"})
public class MDTWorkflowListCandidatesController {
	@Autowired private MDTWorkflowManager m_wfManager;

    @GetMapping("/models")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> listCandidateModels() {
    	String output = FStream.from(m_wfManager.getWorkflowModelAll())
								.map(model -> model.getId())
								.join(Utilities.getLineSeparator());
    	return ResponseEntity.ok(output);
    }

    @GetMapping("/workflows/{type}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> listWorkflowIds(@PathVariable("type") String type) {
    	List<String> wfIdList = null;
    	if ( type.equals("all") ) {
    		wfIdList = m_wfManager.listWorkflowIds();
    	}
    	else if ( type.equals("running") ) {
    		wfIdList = FStream.from(m_wfManager.listWorkflowIds())
								.filter(wfId -> m_wfManager.getWorkflowStatus(wfId) == WorkflowStatus.RUNNING)
								.toList();
    	}
    	else if ( type.equals("stopped") ) {
    		wfIdList = FStream.from(m_wfManager.listWorkflowIds())
								.filter(wfId -> {
									WorkflowStatus status = m_wfManager.getWorkflowStatus(wfId);
                                    return status == WorkflowStatus.COMPLETED || status == WorkflowStatus.FAILED;
								})
								.toList();
    	}
		String output = FStream.from(wfIdList)
								.join(Utilities.getLineSeparator());
    	return ResponseEntity.ok(output);
    }
}

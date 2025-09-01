package mdt.workflow.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import utils.Throwables;
import utils.func.FOption;
import utils.func.Try;
import utils.http.RESTfulErrorEntity;

import mdt.model.AASUtils;
import mdt.model.ResourceAlreadyExistsException;
import mdt.model.ResourceNotFoundException;
import mdt.workflow.Workflow;
import mdt.workflow.WorkflowModel;
import mdt.workflow.config.MDTWorkflowManagerConfiguration;
import mdt.workflow.service.MDTWorkflowManager;


/**
*
* @author Kang-Woo Lee (ETRI)
*/
@Tag(name = "MDTWorkflowManager", description = "MDT 워크플로우 관리 API")
@RestController
@RequestMapping(value={"/workflow-manager"})
@Slf4j
@RequiredArgsConstructor
public class MDTWorkflowManagerController {
	@Autowired private MDTWorkflowManagerConfiguration m_conf;
	@Autowired private MDTWorkflowManager m_wfManager;

    @Operation(summary = "식별자에 해당하는 워크플로우 모델을 반환한다.")
    @Parameters({
    	@Parameter(name = "id", description = "검색할 MDTInstance 식별자")
    })
    @ApiResponses(value = {
    	@ApiResponse(responseCode = "200", description = "성공",
			content = {
				@Content(schema = @Schema(implementation = WorkflowModel.class), mediaType = "application/json")
			}),
    	@ApiResponse(responseCode = "404", description = "식별자에 해당하는 워크플로우가 등록되지 않은 경우.")
    })
    @GetMapping("/models/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<WorkflowModel> getWorkflowModel(@PathVariable("id") String id) {
    	WorkflowModel wfModel = m_wfManager.getWorkflowModel(id);
		return ResponseEntity.ok(wfModel);
    }

    @Operation(summary = "등록된 모든 워크플로우 모델들을 반환한다.")
    @Parameters()
    @ApiResponses(value = {
    	@ApiResponse(responseCode = "200", description = "성공",
    		content = {
    			@Content(mediaType = "application/json",
    					array = @ArraySchema(schema=@Schema(implementation = WorkflowModel.class)))
    		}
    	)
    })
    @GetMapping("/models")
    @ResponseStatus(HttpStatus.OK)
    public List<WorkflowModel> getWorkflowModelAll() {
    	return m_wfManager.getWorkflowModelAll();
    }

    @Operation(summary = "워크플로우 관리자에 주어진 워크플로우 모델을 등록시킨다.")
    @Parameters({
    	@Parameter(name = "wfDescJson", description = "Json 형식으로 작성된 워크플로우 등록 정보"),
    })
    @ApiResponses(value = {
    	@ApiResponse(responseCode = "201", description = "성공",
			content = {
				@Content(schema = @Schema(implementation = WorkflowModel.class),
						mediaType = "application/json")
			}),
    	@ApiResponse(responseCode = "400",
    				description = "워크플로우 등록 정보 파싱에 실패하였거나,"
    							+ "식별자에 해당하는 워크플로우 등록정보가 이미 존재합니다.")
    })
    @PostMapping({"/models"})
    @ResponseStatus(HttpStatus.CREATED)
    public WorkflowModel addWorkflowModel(@RequestBody WorkflowModel wfDesc,
										@RequestParam(name="updateIfExists", defaultValue="false") boolean updateIfExists)
    	throws ResourceAlreadyExistsException {
    	WorkflowModel wfModel = (updateIfExists)
    							? m_wfManager.addOrReplaceWorkflowModel(wfDesc)
    							: m_wfManager.addWorkflowModel(wfDesc);
    	return wfModel;
    }

    @Operation(summary = "식별자에 해당하는 워크플로우 모델을 삭제한다.")
    @Parameters({
    	@Parameter(name = "id", description = "삭제할 워크플로우 등록 정보 식별자")
    })
    @ApiResponses(value = {
    	@ApiResponse(responseCode = "204", description = "성공"),
    	@ApiResponse(responseCode = "404",
    				description = "식별자에 해당하는 워크플로우 등록 정보가 등록되어 있지 않습니다.")
    })
    @DeleteMapping("/models/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeWorkflowModel(@PathVariable("id") String id) {
    	m_wfManager.removeWorkflowModel(id);
    }
    @Operation(summary = "등록된 모든 워크플로우 모델을 삭제한다.")
    @ApiResponses(value = {
    	@ApiResponse(responseCode = "204", description = "성공")
    })
    @DeleteMapping("/models")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeWorkflowModelAll() {
    	Try.run(m_wfManager::removeWorkflowModelAll);
    }

    @Operation(summary = "식별자에 해당하는 워크플로우 정보를 Argo 워크플로우 구동 스크립트로 변환하여 반환한다.")
    @Parameters({
    	@Parameter(name = "id", description = "변환시킬 워크플로우 등록 정보 식별자")
    })
    @ApiResponses(value = {
    	@ApiResponse(responseCode = "200", description = "성공",
			content = {
				@Content(schema = @Schema(implementation = WorkflowModel.class), mediaType = "application/yaml")
			}),
    	@ApiResponse(responseCode = "404", description = "식별자에 해당하는 워크플로우가 등록되지 않은 경우.")
    })
    @GetMapping("/models/{id}/script")
    @ResponseStatus(HttpStatus.OK)
    public String getArgoWorklfowScript(@PathVariable("id") String id,
										@RequestParam(name="mdt-endpoint", required=false) String mdtEndpoint,
										@RequestParam(name="client-docker-image", required=false) String clientImage)
    	throws JsonProcessingException {
    	mdtEndpoint = FOption.mapOrElse(mdtEndpoint, AASUtils::decodeBase64UrlSafe, m_conf.getMdtEndpoint());
    	clientImage = FOption.mapOrElse(clientImage, AASUtils::decodeBase64UrlSafe, m_conf.getClientDockerImage());
    	
		return m_wfManager.getWorkflowScript(id, mdtEndpoint, clientImage);
    }
    
    @PostMapping("/execution-times/tasks/{smRef}")
    public Double estimateTaskExecutionTime(@PathVariable("smRef") String smId) {
    	return 11.5;
    }


    @Operation(summary = "생성된 모든 워크플로우 인스턴스들을 반환한다.")
    @Parameters()
    @ApiResponses(value = {
    	@ApiResponse(responseCode = "200", description = "성공",
    		content = {
    			@Content(mediaType = "application/json",
    					array = @ArraySchema(schema=@Schema(implementation = Workflow.class)))
    		}
    	)
    })
	@GetMapping("/workflows")
    @ResponseStatus(HttpStatus.OK)
	public List<Workflow> getWorkflowAll() {
		try {
			return m_wfManager.getWorkflowAll();
		}
		catch ( IllegalArgumentException e ) {
			String msg = e.getMessage();
			if ( msg.equals("Expected the field `items` to be an array in the JSON string but got `null`") ) {
				// argo-java-client 라이브러리에서는 버그 때문에 workflow가 하나도 없는 경우 오류가 발생.
				return Collections.emptyList();
			}
			else {
				throw e;
			}
		}
	}

    @Operation(summary = "식별자에 해당하는 워크플로우 인스턴스를 반환한다.")
    @Parameters({
    	@Parameter(name = "id", description = "검색할 워크플로우 인스턴스 식별자")
    })
    @ApiResponses(value = {
    	@ApiResponse(responseCode = "200", description = "성공",
			content = {
				@Content(schema = @Schema(implementation = Workflow.class), mediaType = "application/json")
			}),
    	@ApiResponse(responseCode = "404", description = "식별자에 해당하는 워크플로우 인스턴스가 없는 경우.")
    })
	@GetMapping("/workflows/{wfId}")
    @ResponseStatus(HttpStatus.OK)
	public Workflow getWorkflow(@PathVariable("wfId") String wfId) throws ResourceNotFoundException {
		return m_wfManager.getWorkflow(wfId);
	}

    @Operation(summary = "워크플로우 관리자에 등록된 워크플로우 모델을 이용하여 새로운 워크플로우를 시작시킨다.")
    @Parameters({
    	@Parameter(name = "wfModelId", description = "워크플로우 모델 식별자"),
    })
    @ApiResponses(value = {
    	@ApiResponse(responseCode = "201", description = "성공",
			content = {
				@Content(schema = @Schema(implementation = WorkflowModel.class),
						mediaType = "application/json")
			}),
    	@ApiResponse(responseCode = "404", description = "식별자에 해당하는 워크플로우 모델이 없는 경우.")
    })
	@PostMapping("/models/{modelId}/start")
    @ResponseStatus(HttpStatus.OK)
	public Workflow startWorkflow(@PathVariable("modelId") String wfModelId) throws ResourceNotFoundException {
		return m_wfManager.startWorkflow(wfModelId);
	}

    @Operation(summary = "동작 중인 워크플로우를 종료시킨다.")
    @Parameters({
    	@Parameter(name = "wfId", description = "워크플로우 인스턴스 식별자"),
    })
    @ApiResponses(value = {
    	@ApiResponse(responseCode = "204", description = "성공적으로 종료됨",
			content = {
				@Content(schema = @Schema(implementation = WorkflowModel.class),
						mediaType = "application/json")
			}),
    	@ApiResponse(responseCode = "404", description = "식별자에 해당하는 워크플로우 모델이 없는 경우.")
    })
    @PutMapping("/workflows/{wfId}/stop")
    @ResponseStatus(HttpStatus.NO_CONTENT)
	public void stopWorkflow(@PathVariable("wfId") String wfId) throws ResourceNotFoundException {
		m_wfManager.stopWorkflow(wfId);
	}

    @Operation(summary = "식별자에 해당하는 워크플로우 인스턴스를 수행 중지시킨다.")
    @Parameters({
    	@Parameter(name = "wfId", description = "중지시킬 워크플로우 인스턴스 식별자")
    })
    @ApiResponses(value = {
    	@ApiResponse(responseCode = "200", description = "성공",
			content = {
				@Content(schema = @Schema(implementation = Workflow.class), mediaType = "application/json")
			}),
    	@ApiResponse(responseCode = "404", description = "식별자에 해당하는 워크플로우 인스턴스가 없는 경우.")
    })
	@PutMapping("/workflows/{wfId}/suspend")
    @ResponseStatus(HttpStatus.OK)
	public Workflow suspendWorkflow(@PathVariable("wfId") String wfId) throws ResourceNotFoundException {
		return m_wfManager.suspendWorkflow(wfId);
	}

    @Operation(summary = "수행 중지된 워크플로우 인스턴스를 재개시킨다.")
    @Parameters({
    	@Parameter(name = "wfId", description = "재개시킬 워크플로우 인스턴스 식별자")
    })
    @ApiResponses(value = {
    	@ApiResponse(responseCode = "200", description = "성공",
			content = {
				@Content(schema = @Schema(implementation = Workflow.class), mediaType = "application/json")
			}),
    	@ApiResponse(responseCode = "404", description = "식별자에 해당하는 워크플로우 인스턴스가 없는 경우.")
    })
	@PutMapping("/workflows/{wfId}/resume")
    @ResponseStatus(HttpStatus.OK)
	public Workflow resumeWorkflow(@PathVariable("wfId") String wfId) throws ResourceNotFoundException {
		return m_wfManager.resumeWorkflow(wfId);
	}

    @Operation(summary = "워크플로우 인스턴스를 삭제시킨다.")
    @Parameters({
    	@Parameter(name = "wfId", description = "삭제시킬 워크플로우 인스턴스 식별자")
    })
    @ApiResponses(value = {
    	@ApiResponse(responseCode = "200", description = "성공",
			content = {
				@Content(schema = @Schema(implementation = Workflow.class), mediaType = "application/json")
			}),
    	@ApiResponse(responseCode = "404", description = "식별자에 해당하는 워크플로우 인스턴스가 없는 경우.")
    })
	@DeleteMapping("/workflows/{wfId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
	public void removeWorkflow(@PathVariable("wfId") String wfId) {
		m_wfManager.removeWorkflow(wfId);
	}

    @Operation(summary = "모든 워크플로우 인스턴스를 삭제시킨다.")
    @ApiResponses(value = {
    	@ApiResponse(responseCode = "200", description = "성공",
			content = {
				@Content(schema = @Schema(implementation = Workflow.class), mediaType = "application/json")
			})
    })
	@DeleteMapping("/workflows")
    @ResponseStatus(HttpStatus.NO_CONTENT)
	public void removeWorkflowAll(@RequestParam(name="modelFilter", required=false) String modelFilter) {
    	if ( modelFilter != null ) {
			m_wfManager.getWorkflowAll().stream()
						.filter(wf -> wf.getModelId().equals(modelFilter))
						.forEach(wf -> {
							try {
								m_wfManager.removeWorkflow(wf.getName());
							}
							catch ( ResourceNotFoundException e ) { }
						});
    	}
    	else {
    		m_wfManager.removeWorkflowAll();
    	}
	}

    @Operation(summary = "주어진 POD에서 수행 중인 워크플로우 인스턴스의 로그 정보를 조회한다.")
    @Parameters({
    	@Parameter(name = "wfId", description = "삭제시킬 워크플로우 인스턴스 식별자"),
    	@Parameter(name = "podName", description = "수행 중인 POD 이름")
    })
    @ApiResponses(value = {
    	@ApiResponse(responseCode = "200", description = "성공",
			content = {
				@Content(schema = @Schema(implementation = Workflow.class), mediaType = "application/json")
			}),
    	@ApiResponse(responseCode = "404", description = "식별자에 해당하는 워크플로우 인스턴스가 없는 경우.")
    })
	@GetMapping("/workflows/{wfId}/log/{podName}")
    @ResponseStatus(HttpStatus.OK)
	public String log(@PathVariable("wfId") String wfId, @PathVariable("podName") String podName)
		throws ResourceNotFoundException {
		return m_wfManager.getWorkflowLog(wfId, podName);
	}
    
    @ExceptionHandler()
    public ResponseEntity<RESTfulErrorEntity> handleException(Exception e) {
		Throwable cause = Throwables.unwrapThrowable(e);
    	if ( cause instanceof ResourceNotFoundException ) {
    		return ResponseEntity.status(HttpStatus.NOT_FOUND)
    							.contentType(MediaType.APPLICATION_JSON)
    							.body(RESTfulErrorEntity.of(cause));
    	}
    	else if ( cause instanceof ResourceAlreadyExistsException ) {
    		return ResponseEntity.status(HttpStatus.CONFLICT)
								.contentType(MediaType.APPLICATION_JSON)
								.body(RESTfulErrorEntity.of(cause));
    	}
    	else {
    		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
								.contentType(MediaType.APPLICATION_JSON)
								.body(RESTfulErrorEntity.of(cause));
    	}
    }
}

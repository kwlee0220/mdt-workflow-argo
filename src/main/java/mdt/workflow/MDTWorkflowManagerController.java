package mdt.workflow;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

import utils.Throwables;
import utils.func.FOption;
import utils.func.Try;
import utils.http.RESTfulErrorEntity;

import mdt.model.AASUtils;
import mdt.model.ResourceAlreadyExistsException;
import mdt.model.ResourceNotFoundException;
import mdt.workflow.config.MDTWorkflowManagerConfiguration;


/**
*
* @author Kang-Woo Lee (ETRI)
*/
@RestController
@RequestMapping(value={"/workflow-manager"})
public class MDTWorkflowManagerController implements InitializingBean {
	private final Logger s_logger = LoggerFactory.getLogger(MDTWorkflowManagerController.class);

	@Autowired private MDTWorkflowManagerConfiguration m_conf;
	@Autowired private WorkflowManager m_wfManager;

	@Override
	public void afterPropertiesSet() throws Exception  {
		s_logger.info("Started: MDTWorkflowManagerController {}", m_wfManager);
	}

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
    public String addWorkflowModel(@RequestBody WorkflowModel wfDesc,
									@RequestParam(name="updateIfExists", defaultValue="false") boolean updateIfExists)
    	throws ResourceAlreadyExistsException {
    	String wfId = (updateIfExists) ? m_wfManager.addOrUpdateWorkflowModel(wfDesc)
    									: m_wfManager.addWorkflowModel(wfDesc);
    	return wfId;
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
	
	@GetMapping("/workflows")
    @ResponseStatus(HttpStatus.OK)
	public List<Workflow> getWorkflowAll() {
		return m_wfManager.getWorkflowAll();
	}

	@GetMapping("/workflows/{wfName}")
    @ResponseStatus(HttpStatus.OK)
	public Workflow getWorkflow(@PathVariable("wfName") String wfName) throws ResourceNotFoundException {
		return m_wfManager.getWorkflow(wfName);
	}

	@PutMapping("/models/{modelId}/start")
    @ResponseStatus(HttpStatus.OK)
	public Workflow startWorkflow(@PathVariable("modelId") String wfModelId) throws ResourceNotFoundException {
		return m_wfManager.startWorkflow(wfModelId);
	}

	@PutMapping("/workflows/{wfname}/stop")
    @ResponseStatus(HttpStatus.NO_CONTENT)
	public void stopWorkflow(@PathVariable("wfname") String wfName) throws ResourceNotFoundException {
		m_wfManager.stopWorkflow(wfName);
	}

	@PutMapping("/workflows/{wfName}/suspend")
    @ResponseStatus(HttpStatus.OK)
	public Workflow suspendWorkflow(@PathVariable("wfName") String wfName) throws ResourceNotFoundException {
		return m_wfManager.suspendWorkflow(wfName);
	}

	@PutMapping("/workflows/{wfName}/resume")
    @ResponseStatus(HttpStatus.OK)
	public Workflow resumeWorkflow(@PathVariable("wfName") String wfName) throws ResourceNotFoundException {
		return m_wfManager.resumeWorkflow(wfName);
	}

	@DeleteMapping("/workflows/{wfName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
	public void removeWorkflow(@PathVariable("wfName") String wfName) throws ResourceNotFoundException {
		m_wfManager.removeWorkflow(wfName);
	}

	@GetMapping("/workflows/{wfName}/log/{podName}")
    @ResponseStatus(HttpStatus.OK)
	public String log(@PathVariable("wfName") String wfName, @PathVariable("podName") String podName)
		throws ResourceNotFoundException {
		return m_wfManager.getWorkflowLog(wfName, podName);
	}
    
    @ExceptionHandler()
    public ResponseEntity<RESTfulErrorEntity> handleException(Exception e) {
		Throwable cause = Throwables.unwrapThrowable(e);
    	if ( cause instanceof ResourceNotFoundException ) {
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RESTfulErrorEntity.of(cause));
    	}
    	else if ( cause instanceof ResourceAlreadyExistsException ) {
    		return ResponseEntity.status(HttpStatus.CONFLICT).body(RESTfulErrorEntity.of(cause));
    	}
    	else {
    		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(RESTfulErrorEntity.of(cause));
    	}
    }
}

package mdt.workflow.repository;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import mdt.workflow.domain.JpaWorkflowModel;

import jakarta.transaction.Transactional;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface JpaWorkflowModelRepository extends JpaRepository<JpaWorkflowModel, Long> {
	/**
	 * 워크플로우 모델을 ID로 조회한다.
	 * 
	 * @param modelId 워크플로우 모델의 ID
	 * @return 워크플로우 모델
	 */
	@Query("SELECT w FROM JpaWorkflowModel w WHERE w.id = :modelId")
	public Optional<JpaWorkflowModel> findByModelId(@Param("modelId") String modelId);
	
	/**
	 * 워크플로우 모델의 ID 목록을 조회한다.
	 * 
	 * @return 워크플로우 모델의 ID 목록
	 */
	@Query("SELECT w.id FROM JpaWorkflowModel w")
	public Set<String> findModelIdAll();
	
	@Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END FROM JpaWorkflowModel w WHERE w.id = :modelId")
	public boolean existsByModelId(@Param("modelId") String modelId);

	@Modifying
	@Transactional
	@Query("DELETE FROM JpaWorkflowModel w WHERE w.id = :modelId")
	public void deleteByModelId(@Param("modelId") String modelId);
}
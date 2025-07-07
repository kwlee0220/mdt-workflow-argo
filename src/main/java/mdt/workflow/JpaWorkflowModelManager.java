package mdt.workflow;

import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;
import com.google.common.base.Preconditions;

import utils.InternalException;
import utils.LoggerSettable;
import utils.func.FOption;
import utils.jpa.JpaSessionFactory;

import mdt.model.ResourceAlreadyExistsException;
import mdt.model.ResourceNotFoundException;
import mdt.workflow.argo.ArgoWorkflowDescriptor;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class JpaWorkflowModelManager implements WorkflowModelManager, JpaSessionFactory, LoggerSettable {
	private static final Logger s_logger = LoggerFactory.getLogger(JpaWorkflowModelManager.class);
	private static final YAMLFactory YAML_FACTORY = new YAMLFactory().disable(Feature.WRITE_DOC_START_MARKER);
	
	private final EntityManagerFactory m_emFact;
	private Logger m_logger;

	public JpaWorkflowModelManager(EntityManagerFactory emFact) {
		m_emFact = emFact;
	}
	
	@Override
	public EntityManagerFactory getEntityManagerFactory() {
		return m_emFact;
	}

	@Override
	public WorkflowModel getWorkflowModel(@NonNull String id) throws ResourceNotFoundException {
		return getInJpaSession(em -> getJpaWorkflowModelInGuard(em, id)).getWorkflowModel();
	}

	@Override
	public String getWorkflowScript(String id, String mdtEndpoint, String clientDockerImage)
		throws ResourceNotFoundException {
		WorkflowModel wfModel = getWorkflowModel(id);
		
		try {
			ArgoWorkflowDescriptor argoWfDesc = new ArgoWorkflowDescriptor(wfModel, mdtEndpoint, clientDockerImage);
			return JsonMapper.builder(YAML_FACTORY).build()
											.writerWithDefaultPrettyPrinter()
											.writeValueAsString(argoWfDesc);
		}
		catch ( JsonProcessingException e ) {
			throw new MDTWorkflowManagerException("fails to generate workflow script", e);
		}
	}

	@Override
	public List<WorkflowModel> getWorkflowModelAll() {
		return getInJpaSession(em -> {
			return em.createQuery("select d from JpaWorkflowModel d", JpaWorkflowModel.class)
						.getResultStream()
						.map(JpaWorkflowModel::getWorkflowModel)
						.toList();
		});
	}

	@Override
	public String addWorkflowModel(WorkflowModel desc) throws ResourceAlreadyExistsException {
		JpaWorkflowModel jdesc = new JpaWorkflowModel(desc);
		runInJpaSession(em -> {
			addJpaWorkflowModelInGuard(em, jdesc);
			if ( s_logger.isInfoEnabled() ) {
                s_logger.info("added WorkflowModel: id=" + jdesc.getId());
			}
		});
		return jdesc.getId();
	}
	
	public String addWorkflowModel(String modelJson) throws ResourceAlreadyExistsException {
		try {
			JpaWorkflowModel jmodel = new JpaWorkflowModel(modelJson);
			runInJpaSession(em -> {
				addJpaWorkflowModelInGuard(em, jmodel);
				if ( s_logger.isInfoEnabled() ) {
	                s_logger.info("added WorkflowModel: id=" + jmodel.getId());
				}
			});
			
			return jmodel.getId();
		}
		catch ( JsonProcessingException e ) {
			String msg = String.format("Failed to parse input WorkflowModel in Json: %s", e);
			throw new IllegalArgumentException(msg);
		}
	}
	
	@Override
	public String addOrUpdateWorkflowModel(WorkflowModel desc) throws ResourceAlreadyExistsException {
		JpaWorkflowModel newOne = new JpaWorkflowModel(desc);
		runInJpaSession(em -> {
			try {
				JpaWorkflowModel prev = getJpaWorkflowModelInGuard(em, desc.getId());
				prev.setJsonModel(newOne.getJsonModel());
				if ( s_logger.isInfoEnabled() ) {
	                s_logger.info("update the WorkflowModel: id=" + desc.getId());
				}
			}
			catch ( ResourceNotFoundException e ) {
				em.persist(newOne);
				if ( s_logger.isInfoEnabled() ) {
	                s_logger.info("add WorkflowModel: id=" + desc.getId());
				}
			}
		});

		return desc.getId();
	}

	@Override
	public void removeWorkflowModel(String id) throws ResourceNotFoundException {
		runInJpaSession(em -> {
			JpaWorkflowModel jdesc = getJpaWorkflowModelInGuard(em, id);
			if ( jdesc != null ) {
				em.remove(jdesc);
				if ( s_logger.isInfoEnabled() ) {
	                s_logger.info("removed the WorkflowModel: id=" + jdesc.getId());
				}
			}
			else {
				throw new ResourceNotFoundException("WorkflowModel", "id=" + id);
			}
		});
	}

	@Override
	public void removeWorkflowModelAll() {
		runInJpaSession(em -> {
			em.createQuery("delete from JpaWorkflowModel").executeUpdate();
		});
		if ( s_logger.isInfoEnabled() ) {
            s_logger.info("removed all WorkflowModels");
		}
	}

	private static JpaWorkflowModel getJpaWorkflowModelInGuard(EntityManager em, String id)
		throws ResourceNotFoundException {
		Preconditions.checkArgument(id != null, "WorkflowModel id is null");
		Preconditions.checkState(em != null, "EntityManager is not set");
		
		String sql = "select model from JpaWorkflowModel model where model.id = ?1";
		TypedQuery<JpaWorkflowModel> query = em.createQuery(sql, JpaWorkflowModel.class);
		try {
			query.setParameter(1, id);
			return query.getSingleResult();
		}
		catch ( NoResultException expected ) {
			throw new ResourceNotFoundException("WorkflowModel", "id=" + id);
		}
	}
	
	private static String addJpaWorkflowModelInGuard(EntityManager em, JpaWorkflowModel jdesc)
		throws ResourceAlreadyExistsException {
		Preconditions.checkState(em != null, "EntityManager is not set");
		
		try {
			em.persist(jdesc);
			
			return jdesc.getId();
		}
		catch ( EntityExistsException e ) {
			throw new ResourceAlreadyExistsException("WorkflowModel", "json=" + jdesc.getJsonModel());
		}
		catch ( ConstraintViolationException e ) {
			switch ( e.getKind() ) {
				case UNIQUE:
					throw new ResourceAlreadyExistsException("WorkflowModel", "id=" + jdesc.getId());
				default:
					throw new InternalException("" + e);
			}
		}
	}

	@Override
	public Logger getLogger() {
		return FOption.ofNullable(m_logger).getOrElse(s_logger);
	}

	@Override
	public void setLogger(Logger logger) {
		m_logger = logger;
	}
}

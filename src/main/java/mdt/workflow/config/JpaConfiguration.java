package mdt.workflow.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import utils.jdbc.JdbcConfiguration;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Configuration
@ConfigurationProperties(prefix = "jpa")
@Getter @Setter
@Accessors(prefix = "m_")
public class JpaConfiguration {
	private JdbcConfiguration m_jdbc;
	private Map<String,String> m_properties;
}

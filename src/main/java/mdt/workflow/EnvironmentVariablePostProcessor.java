package mdt.workflow;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import utils.io.EnvironmentFileLoader;
import utils.stream.KeyValueFStream;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class EnvironmentVariablePostProcessor implements EnvironmentPostProcessor, Ordered {
    private static final DeferredLog s_deferredLog = new DeferredLog();

    private static final String ENV_FILE_ENV_VAR = "ENV_FILE";
    private static final String ENV_FILE_PROPERTY = "env.file";
    private static final String ENV_FILE_NAME = "config/env.file";
    
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication application) {
        String fromProp = System.getProperty(ENV_FILE_PROPERTY);
        String fromEnv = System.getenv(ENV_FILE_ENV_VAR);
        String path = firstNonBlank(fromProp, fromEnv, ENV_FILE_NAME);
        
        try {
            File envFile = new File(path);
            s_deferredLog.info("trying to load environment variables from file: "
                                + envFile.getAbsolutePath());
            EnvironmentFileLoader envLoader = EnvironmentFileLoader.from(envFile);
            LinkedHashMap<String, String> variables = envLoader.load();
            KeyValueFStream.from(variables)
                            .forEach(kv -> s_deferredLog.info(String.format("  - EnvVar: %s=%s", kv.key(), kv.value())));
            
            Map<String,Object> vars = KeyValueFStream.from(variables)
                                                        .mapValue(v -> (Object)v)
                                                        .toMap();
            MapPropertySource source = new MapPropertySource("mdtEnv", vars);
            env.getPropertySources().addFirst(source);
        }
        catch ( IOException e ) {
            s_deferredLog.error("failed to load env file: " + path, e);
        }
        
        application.addListeners((ApplicationListener<ApplicationPreparedEvent>) event -> {
        	Log actualLogger = LogFactory.getLog(EnvironmentVariablePostProcessor.class);
			s_deferredLog.replayTo(actualLogger);
		});
    }
    
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
    
    private static String firstNonBlank(String... arr) {
        for ( String s : arr ) {
            if ( s != null && !s.isBlank() )
                return s;
        }
        return null;
    }
}
package com.itb.inf3em.studyconnect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class StudyconnectApplication {

	private static final Logger log = LoggerFactory.getLogger(StudyconnectApplication.class);

	public static void main(String[] args) {
		long t0 = System.currentTimeMillis();
		ApplicationContext ctx = SpringApplication.run(StudyconnectApplication.class, args);
		log.info("[STARTUP] Contexto Spring pronto em {}ms | beans registrados: {}",
				System.currentTimeMillis() - t0,
				ctx.getBeanDefinitionCount());
	}
}
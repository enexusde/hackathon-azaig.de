package de.azaig.web;

import java.sql.SQLException;
import java.util.Locale;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.internal.StandardDialectResolver;
import org.hibernate.engine.jdbc.dialect.spi.DatabaseMetaDataDialectResolutionInfoAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import de.azaig.User;

@Configuration
@EnableWebMvc
@ComponentScan
@EnableTransactionManagement
public class AppConfig extends AbstractAnnotationConfigDispatcherServletInitializer implements WebMvcConfigurer {
	@Override
	protected String[] getServletMappings() {
		return new String[] { "/api/*" };
	}

	@Override
	protected Class<?>[] getRootConfigClasses() {
		return new Class[] { AppConfig.class };
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {
		return null;
	}

	@Bean
	protected LocalContainerEntityManagerFactoryBean emf(DataSource dataSource) throws SQLException {
		final LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
		emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		emf.setPackagesToScan(User.class.getPackage().getName());
		final StandardDialectResolver resolver = new StandardDialectResolver();
		try {
			final Dialect dialect = resolver.resolveDialect(new DatabaseMetaDataDialectResolutionInfoAdapter(dataSource.getConnection().getMetaData()));
			emf.getJpaPropertyMap().put(Environment.DIALECT, dialect.getClass().getCanonicalName());
		} catch (SQLException databaseProblem) {
			throw databaseProblem;
		}
		emf.setDataSource(dataSource);
		return emf;
	}

	@Bean
	protected PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
		final EntityManagerFactory factory = entityManagerFactory.getObject();
		final JpaTransactionManager manager = new JpaTransactionManager(factory);
		return manager;
	}

	@Bean
	protected DataSource dataSource() {
		final JndiDataSourceLookup dsLookup = new JndiDataSourceLookup();
		dsLookup.setResourceRef(true);
		return dsLookup.getDataSource("jdbc/database");
	}

	public static void main(String[] args) {
		System.out.println("I".toLowerCase(new Locale("tr", "TR")));
	}
}

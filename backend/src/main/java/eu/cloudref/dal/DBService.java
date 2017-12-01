package eu.cloudref.dal;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import java.io.File;

public class DBService {

    private static SessionFactory sessionFactory = null;

    public static SessionFactory getSessionFactory() {

        if (sessionFactory == null) {

            Configuration configuration = new Configuration().configure();

            // add absolute path to database
            String cloudRefDirectory = eu.cloudref.Configuration.getCloudRefDirectory();
            new File(cloudRefDirectory).mkdirs();
            configuration.setProperty("hibernate.connection.url", "jdbc:sqlite:" + cloudRefDirectory + "CloudRef.sqlite");
            // enable foreign keys
            configuration.setProperty("hibernate.connection.foreign_keys", "true");

            StandardServiceRegistryBuilder serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
            sessionFactory = configuration.buildSessionFactory(serviceRegistry.build());
        }

        return sessionFactory;
    }
}

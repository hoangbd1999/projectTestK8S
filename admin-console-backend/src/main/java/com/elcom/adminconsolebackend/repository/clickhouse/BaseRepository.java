package com.elcom.adminconsolebackend.repository.clickhouse;

import com.google.common.base.CaseFormat;
import jakarta.persistence.Column;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author anhdv
 */
@Repository
public class BaseRepository {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseRepository.class);
    
    protected SessionFactory sessionFactory;

    protected DataSource clDatasource;

    protected BaseRepository(EntityManagerFactory factory, DataSource chDataSource) {

        if (factory.unwrap(SessionFactory.class) == null)
            throw new NullPointerException("factory is not a hibernate factory");

        this.sessionFactory = factory.unwrap(SessionFactory.class);

        this.clDatasource = chDataSource;
    }

    protected Session openSession() {
        Session session = this.sessionFactory.openSession();
        return session;
    }

    protected void closeSession(Session session) {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

    protected <O> O update(O obj) {
        Session session = null;
        try {
            session = this.openSession();
            Transaction tx = session.beginTransaction();
            
            Field id = null;
            for( Field field : obj.getClass().getDeclaredFields() ){
                Annotation[] declaredAnnotations = field.getDeclaredAnnotations();
                if ( Arrays.stream(declaredAnnotations).anyMatch(annotation -> annotation.annotationType().equals(Id.class)) )
                    id = field;
            }

            if( id == null )
                return null;

            String conditionString = id.getDeclaredAnnotation(Column.class).name() + " = :" + id.getName();
            String sql = " UPDATE " + obj.getClass().getAnnotation(Table.class).name() + " SET " +
                      this.generateSetString(obj) + " where " + conditionString;
            NativeQuery updateQuery = session.createNativeQuery(sql);
            this.setParams(updateQuery, obj);
            int resultUpdate = updateQuery.executeUpdate();
            LOGGER.info("update DB record return -> [ {} ]", resultUpdate);
            if (resultUpdate > 0) {
                tx.commit();
                return obj;
            }
            return null;
        } catch (Exception e) {
            LOGGER.error("BaseRepository.ex: ", e);
            throw e;
        } finally {
            this.closeSession(session);
        }
    }
    
    private String generateSetString(Object obj) {
        Field[] fields = obj.getClass().getDeclaredFields();

        return Arrays.stream(fields)
                .filter(field -> {
                    try {
                        Method method = obj.getClass().getMethod("get" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, field.getName()));
                        Object value = method.invoke(obj);
                        return value != null;
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }).map(field -> {
                    String camelName = field.getName();
                    String snakeName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, camelName);
                    return " " + snakeName + " = :" + camelName + " ";
                }).collect(Collectors.joining(", "));
    }
    
    private void setParams(NativeQuery nativeQuery, Object obj) {
        Field[] fields = obj.getClass().getDeclaredFields();
        Arrays.stream(fields)
                .filter(field -> {
                    try {
                        Method method = obj.getClass().getMethod("get" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, field.getName()));
                        Object value = method.invoke(obj);
                        return value != null;
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }).forEach(field -> {
                    try {
                        Method method = obj.getClass().getMethod("get" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, field.getName()));
                        Object value = method.invoke(obj);
                        nativeQuery.setParameter(field.getName(), value);
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}

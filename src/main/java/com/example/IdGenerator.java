package com.example;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator; 
import java.io.Serializable; 

public class IdGenerator implements IdentifierGenerator {
 
    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException { 
        Long id = new SnowflakeIdGenerator(1).generateId();  
        return id;  
    }

    
}
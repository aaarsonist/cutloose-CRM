package com.barbershop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DatabaseTest implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Подключение к базе данных: " + dataSource.getConnection().getMetaData().getURL());
        System.out.println("Имя пользователя: " + dataSource.getConnection().getMetaData().getUserName());
        System.out.println("Драйвер базы данных: " + dataSource.getConnection().getMetaData().getDriverName());
    }
}

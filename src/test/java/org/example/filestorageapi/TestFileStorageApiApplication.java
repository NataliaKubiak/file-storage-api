package org.example.filestorageapi;

import org.springframework.boot.SpringApplication;

public class TestFileStorageApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(FileStorageApiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}

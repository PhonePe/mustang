package com.phonepe.growth.mustang;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;

public class Sample {

    public static void main(String[] args) throws IOException {
        final Path path = Paths.get("/Users/mohammed.irfanulla/Downloads/dev.keytab");
        final Path path1 = Paths.get("/Users/mohammed.irfanulla/Downloads/dev1.keytab");

        System.out.println(path.toString());

        byte[] bytes = Files.readAllBytes(path);
        final String x1 = new String(Base64.getEncoder()
                .encode(bytes));
        System.out.println(x1);
        Files.write(path1,
                Base64.getDecoder()
                        .decode(x1.getBytes()),
                StandardOpenOption.CREATE);
        final String x2 = new String(Base64.getEncoder()
                .encode(Files.readAllBytes(path1)));
        // System.out.println(x2);
        System.out.println(x1.equals(x2));

        // TODO Auto-generated method stub

    }

}

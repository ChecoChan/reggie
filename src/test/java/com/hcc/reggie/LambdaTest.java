package com.hcc.reggie;

import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LambdaTest {
    @Data
    private static class Demo {
        private Integer id;
        private String name;

        public Demo(Integer id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @Test
    public void demoTest() {
        List<Demo> demos = new ArrayList<>();
        demos.add(new Demo(1, "A"));
        demos.add(new Demo(2, "B"));
        demos.add(new Demo(3, "C"));

        demos = demos.stream().peek((item) -> item.setId(666)).collect(Collectors.toList());

        System.out.println(demos);
    }
}

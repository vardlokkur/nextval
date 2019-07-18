package com.blogspot.vardlokkur.nextval;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author Warlock
 */
@ComponentScan(basePackageClasses = Sequences.class)
@JdbcTest
public class SequencesServiceExpectations {

    @Autowired
    private Sequences sequences;

    private Callable<Integer> callable() {
        return () -> {
            System.out.println(String.format("[%d] - starting", Thread.currentThread().getId()));
            return sequences.nextValue("My Sequence");
        };
    }

    @Test
    public void test() throws Exception {
        final ExecutorService executor = Executors.newFixedThreadPool(3);
        final CompletionService<Integer> completion = new ExecutorCompletionService<>(executor);

        for (int i = 0; i < 3; i++) {
            completion.submit(callable());
        }

        int completed = 0;
        while (completed < 3) {
            final Future<Integer> result = completion.take();
            System.out.println(String.format("Result %d - %d", ++completed, result.get()));
        }
    }

}

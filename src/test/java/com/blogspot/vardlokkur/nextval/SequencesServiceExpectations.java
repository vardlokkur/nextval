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

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        
        for (int completed = 1; completed <= 3; completed++) {
            final Future<Integer> result = completion.take();
            System.out.println(String.format("Result %d - %d", completed, result.get()));
            assertEquals(Integer.valueOf(completed), result.get());
        }
    }

}

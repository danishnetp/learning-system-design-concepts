package com.system.design.low_level_design.design_patterns.transactional_outbox;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionalOutboxPatternExampleTest {

    @Test
    void shouldDemonstrateOutboxFlowWithRetryAndIdempotentConsumer() {
        TransactionalOutboxPatternExample.DemoReport report = TransactionalOutboxPatternExample.runDemo();

        assertTrue(report.dualWriteMessageLossObserved());
        assertEquals(1, report.pendingAfterFirstRelay());
        assertEquals(1, report.publishedAfterSecondRelay());
        assertEquals(1, report.consumerProcessedUniqueMessages());
        assertEquals(1, report.consumerIgnoredDuplicates());
    }

    @Test
    void shouldKeepRetryOutcomeAndConsumerCountsConsistentAcrossRuns() {
        TransactionalOutboxPatternExample.DemoReport first = TransactionalOutboxPatternExample.runDemo();
        TransactionalOutboxPatternExample.DemoReport second = TransactionalOutboxPatternExample.runDemo();

        assertEquals(first.pendingAfterFirstRelay(), second.pendingAfterFirstRelay());
        assertEquals(first.publishedAfterSecondRelay(), second.publishedAfterSecondRelay());
        assertEquals(first.consumerProcessedUniqueMessages(), second.consumerProcessedUniqueMessages());
        assertEquals(first.consumerIgnoredDuplicates(), second.consumerIgnoredDuplicates());
    }

    @Test
    void mainShouldPrintHumanReadableSummary() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputBuffer));

        try {
            TransactionalOutboxPatternExample.main(new String[0]);
        } finally {
            System.setOut(originalOut);
        }

        String output = outputBuffer.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Transactional Outbox Demo"));
        assertTrue(output.contains("Dual-write failure observed: true"));
        assertTrue(output.contains("Outbox pending after first relay attempt: 1"));
        assertTrue(output.contains("Published events after second relay attempt: 1"));
    }
}

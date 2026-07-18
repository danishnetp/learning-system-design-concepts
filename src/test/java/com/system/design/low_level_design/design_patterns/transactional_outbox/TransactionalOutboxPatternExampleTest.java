package com.system.design.low_level_design.design_patterns.transactional_outbox;

import org.junit.jupiter.api.Test;

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
}


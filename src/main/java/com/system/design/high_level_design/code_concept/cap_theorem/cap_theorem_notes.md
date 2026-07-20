# CAP Theorem

## Core Properties

- **C - Consistency:** Every read receives the most recent write or an error.
- **A - Availability:** Every request receives a non-error response, without guarantee that it contains the most recent write.
- **P - Partition Tolerance:** The system continues to operate despite an arbitrary number of messages being dropped or delayed by the network between nodes.

> Desirable properties of distributed systems with replicated data.

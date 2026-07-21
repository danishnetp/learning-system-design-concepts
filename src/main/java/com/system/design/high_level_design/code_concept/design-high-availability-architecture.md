# Design High Availability Architecture

High availability refers to a system design that ensures minimal downtime and maximum uptime, typically aiming for 99.9% to 99.999% availability.

## Alternative Ways to Frame High Availability

The following phrases all describe similar high-availability design goals:

- **Design Data Resilience Architecture**: Build systems that can recover from failures without data loss.
- **Design Architecture to achieve 99.999% Availability**: Create redundancy and failover mechanisms to minimize downtime.
- **Design to avoid Single Points of Failure (SPOF)**: Ensure no single component can bring down the entire system.
- **Active-Passive vs Active-Active Architecture**: Choose between failover and concurrent processing models.

## Availability Levels

The term **"nines"** refers to the number of 9s in the availability percentage. For example:
- 99% = Two 9s
- 99.9% = Three 9s
- 99.99% = Four 9s
- 99.999% = Five 9s

| Availability %    | Downtime per Year | Downtime per Month | Use Case                             |
|-------------------|-------------------|--------------------|--------------------------------------|
| 95% (One 9)       | 18.25 days        | ~1.8 days          | Best-effort services, non-critical   |
| 99% (Two 9s)      | 3.65 days         | ~7 hours           | Basic service tolerance              |
| 99.9% (Three 9s)  | 8.76 hours        | ~43 minutes        | Standard business applications       |
| 99.99% (Four 9s)  | 52.6 minutes      | ~4.3 minutes       | Financial systems, payment platforms |
| 99.999% (Five 9s) | 5.26 minutes      | ~26 seconds        | Critical infrastructure, telecom     |

## Key High Availability Strategies

### 1. Redundancy
- Multiple servers, databases, load balancers
- No single point of failure
- Geographic distribution

### 2. Active-Passive Architecture
- Primary node handles all traffic
- Secondary node on standby for failover
- Faster recovery but underutilizes resources

### 3. Active-Active Architecture
- Multiple nodes handle traffic simultaneously
- Better resource utilization
- More complex coordination (CAP trade-offs)

### 4. Health Monitoring and Failover
- Continuous health checks on all components
- Automated failover when failures detected
- Heartbeat mechanisms

### 5. Data Replication
- Synchronous replication for consistency
- Asynchronous replication for performance
- Multi-region backup

## Single Point of Failure (SPOF) Examples

| Component     | SPOF Risk           | Mitigation                                  |
|---------------|---------------------|---------------------------------------------|
| Load balancer | One LB fails        | Multiple LBs in active-active               |
| Database      | Primary DB down     | Replicated secondaries + automatic failover |
| Cache layer   | Redis instance down | Clustered cache with replication            |
| DNS           | DNS server outage   | Multiple DNS providers                      |

## High Availability Design Diagrams

### 1) Single-Node Style (Has SPOF)

```mermaid
flowchart LR
    C[Client] --> LB[Load Balancer]
    subgraph Server
        A1[App1]
        A2[App2]
    end
    LB --> A1
    LB --> A2
    A1 --> DB[(Primary DB)]
    A2 --> DB
```

If the primary database fails, the system becomes unavailable.

**Advantages**: Simple architecture, easy to implement.

**Disadvantages**: Single point of failure, downtime during maintenance.

### 2) Two-Node Active-Passive

```mermaid
flowchart LR
    C[Client] --> LB[Load Balancer]
    subgraph Server
        A1[App1 - Active]
        A2[App2 - Passive]
    end
    LB --> A1
    LB -->|Failover| A2

    A1 --> DB1[(Primary DB)]
    A2 -->|Standby| DB2[(Replica DB)]
    DB1 -->|Replication| DB2
```

If the active node fails, traffic shifts to the passive node.

**Advantages**: Better availability than single-node setup, controlled failover.

**Disadvantages**: Passive resources are underutilized.

### 3) One Server with Two Nodes

```mermaid
flowchart LR
    C[Client] --> LB[Load Balancer]

    subgraph Server
        subgraph Node1
            A11[App1]
            A12[App2]
            DB1[(DB - Primary)]
            A11 --> DB1
            A12 --> DB1
        end
        subgraph Node2
            A21[App1]
            A22[App2]
            DB2[(DB - Replica)]
            A21 --> DB2
            A22 --> DB2
        end
    end

    LB --> Node1
    LB --> Node2
    DB1 -->|Replication| DB2
```

One server contains two nodes. Each node has multiple apps and one database.

**Advantages**: Better redundancy inside a single server boundary, simple logical separation by node.

**Disadvantages**: Still a single-server risk; if the server fails, both nodes are affected.

### 4) One Load Balancer with Two Data Centers (DR Setup)

```mermaid
flowchart LR
    C[Client] --> LB[Load Balancer]

    subgraph DC1[Data Center 1 - Primary]
        S1[Server]
        A11[App1]
        A12[App2]
        DB1[(DB - Primary)]
        S1 --> A11
        S1 --> A12
        A11 --> DB1
        A12 --> DB1
    end

    subgraph DC2[Data Center 2 - Disaster Recovery]
        S2[Server]
        A21[App1]
        A22[App2]
        DB2[(DB - DR Replica)]
        S2 --> A21
        S2 --> A22
        A21 --> DB2
        A22 --> DB2
    end

    LB --> S1
    LB -->|Failover Traffic| S2
    DB1 -->|Sync / Replication| DB2
```

Notes:

- One load balancer routes traffic to two data centers.
- Each data center has one server running multiple apps and one database.
- DC-2 is used for disaster recovery.
- DC-2 database stays synchronized with DC-1 database.

### 5) One Load Balancer with One Data Center

```mermaid
flowchart LR
    C[Client] --> LB[Load Balancer]

    subgraph DC1[Data Center 1]
        S1[Server]
        A11[App1]
        A12[App2]
        DB1[(DB - Primary)]
        DB2[(DB - Replica)]

        S1 --> A11
        S1 --> A12
        A11 --> DB1
        A12 --> DB1
        DB1 -->|Replication| DB2
    end

    LB --> S1
```

Notes:

- One load balancer routes traffic to a single data center.
- The data center has one server running multiple apps.
- The primary database replicates to a standby replica inside the same data center.
- This improves local availability but does not protect against full data-center outages.


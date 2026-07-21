## Back of the Envelope Estimation

What is Back of the envelope estimation?   

Back of the envelope estimation is a rough calculation or approximation of system capacity, performance, and resource requirements. It helps in system design by estimating:
- How many requests per second (RPS) a system needs to handle
- Storage requirements
- Bandwidth needed
- Number of servers required
- Latency and throughput expectations

## Database Selection Diagram

```mermaid
graph TD
    Start["Start: Choose a Database"]
    
    Start --> Q1{"Need Complex<br/>Relationships<br/>& ACID?"}
    
    Q1 -->|YES| SQL["Use SQL Database<br/>(MySQL, PostgreSQL, Oracle)"]
    Q1 -->|NO| Q2{"Need High<br/>Scalability<br/>& Flexibility?"}
    
    Q2 -->|YES| Q3{"What type of<br/>data structure?"}
    Q2 -->|NO| SQL
    
    Q3 -->|Key-Value| KV["Key-Value Store<br/>(Redis, DynamoDB)"]
    Q3 -->|Documents| Doc["Document DB<br/>(MongoDB, CouchDB)"]
    Q3 -->|Time-Series| Col["Column-Family<br/>(Cassandra, HBase)"]
    Q3 -->|Relationships| Graph["Graph DB<br/>(Neo4j, ArangoDB)"]
    
    SQL --> Benefits1["✓ ACID Compliance<br/>✓ Strong Consistency<br/>✓ Complex Queries"]
    KV --> Benefits2["✓ Ultra-Fast Lookups<br/>✓ Horizontal Scaling<br/>✓ Real-time Data"]
    Doc --> Benefits3["✓ Flexible Schema<br/>✓ JSON-like Storage<br/>✓ Easy to Scale"]
    Col --> Benefits4["✓ High Compression<br/>✓ Analytics Ready<br/>✓ Time-Series Data"]
    Graph --> Benefits5["✓ Relationship Queries<br/>✓ Real-time Insights<br/>✓ Recommendation Engine"]
    
    style SQL fill:#e1f5ff
    style KV fill:#fff3e0
    style Doc fill:#f3e5f5
    style Col fill:#e8f5e9
    style Graph fill:#fce4ec
```

## Quick Comparison Table

| Factor | SQL | NoSQL |
|--------|-----|-------|
| **Scaling** | Vertical | Horizontal |
| **Consistency** | Strong (ACID) | Eventual (BASE) |
| **Data Model** | Relational | Flexible |
| **Query** | SQL | API/Language Specific |
| **Best For** | Financial Systems | High-Volume, Web Apps | 

# Load Balancer and Different Algorithms

A **load balancer** distributes incoming traffic across multiple servers (or service instances) so no single server becomes a bottleneck.

## Why a Load Balancer is Needed

- Improves **availability**: traffic can be routed away from unhealthy instances.
- Improves **scalability**: new servers can be added horizontally.
- Improves **performance**: requests are spread to reduce response time spikes.
- Enables **zero-downtime deployments**: drain traffic from old instances and shift to new ones.

## Types of Load Balancers (By OSI Layer)

### 1) Transport Load Balancer (Layer 4)

A **Transport (L4) load balancer** routes traffic using network-level data such as:
- source IP
- destination IP
- source port
- destination port
- protocol (TCP/UDP)

It does not inspect HTTP headers, paths, cookies, or payload content.

**Advantages**
- Very fast and low latency (less packet inspection).
- Handles massive TCP/UDP throughput efficiently.
- Good for simple pass-through architectures.

**Disadvantages**
- No content-based routing (cannot route by URL, host, header).
- Limited application awareness.
- Harder to apply advanced HTTP-level policies.

**Use Cases**
- TCP databases (MySQL, PostgreSQL) behind a proxy.
- Real-time gaming and UDP workloads.
- High-throughput internal service traffic where speed is top priority.

---

### 2) Application Load Balancer (Layer 7)

An **Application (L7) load balancer** understands application protocols such as HTTP/HTTPS/gRPC and can route based on request content:
- URL path (`/api`, `/images`)
- host/domain (`api.example.com`)
- headers and cookies
- query parameters

**Advantages**
- Smart routing based on business/application logic.
- Supports SSL/TLS termination, auth integration, redirects, and rewrites.
- Better observability and policy control at request level.

**Disadvantages**
- More CPU overhead than L4 due to deeper inspection.
- Higher complexity to configure and tune.
- Can add extra latency if overloaded.

**Use Cases**
- Microservices routing by path/host.
- API gateways and web applications.
- Multi-tenant SaaS platforms requiring fine-grained routing rules.

---

### L4 vs L7 Quick Comparison

| Aspect | Transport LB (L4) | Application LB (L7) |
|---|---|---|
| Routing basis | IP, port, protocol | URL, host, headers, cookies |
| Performance | Higher throughput, lower latency | Lower than L4, but smarter |
| Visibility | Connection-level | Request-level |
| Best for | Raw TCP/UDP speed | HTTP/HTTPS intelligent routing |
| Complexity | Lower | Higher |

## Common Load Balancer Algorithms

> No single algorithm is best for every system. Choose based on workload pattern, session behavior, and server heterogeneity.

### 1) Round Robin

Traffic is sent to backends in a fixed cyclic order.

**Advantages**
- Very simple and fast.
- Works well when all servers have similar capacity.
- Easy to reason about and debug.

**Disadvantages**
- Ignores active load and response time.
- Can overload slower servers if server capacities differ.
- Not ideal for long-lived requests.

**Use Cases**
- Stateless APIs with uniform server specs.
- Small to medium deployments where request cost is similar.

---

### 2) Weighted Round Robin

Like round robin, but each server gets traffic proportional to its configured weight.

**Advantages**
- Supports mixed hardware capacity (e.g., 8-core vs 4-core nodes).
- Still simple and predictable.
- Useful during gradual scaling or partial migrations.

**Disadvantages**
- Static weights may become outdated as runtime conditions change.
- Does not directly measure in-flight requests or latency.

**Use Cases**
- Clusters with non-uniform instance sizes.
- Canary rollout where new version gets smaller percentage of traffic.

---

### 3) Least Connections

New requests go to the server with the fewest active connections.

**Advantages**
- Better for variable-duration requests than round robin.
- Reacts to current load rather than fixed order.

**Disadvantages**
- Connection count may not reflect CPU/memory load exactly.
- More balancing overhead than simple round robin.
- Can behave poorly with keep-alive-heavy traffic patterns.

**Use Cases**
- WebSocket/chat systems.
- Applications with uneven request processing times.

---

### 4) Weighted Least Connections

Combines least-connections with server weight/capacity.

**Advantages**
- Balances dynamic traffic while respecting server capacity.
- Better utilization in heterogeneous clusters.

**Disadvantages**
- More complex to tune and observe.
- Wrong weights can still create imbalance.

**Use Cases**
- Multi-tier production systems with mixed instance types.
- High-traffic services where request duration is unpredictable.

---

### 5) Least Response Time

Routes requests to the server with the lowest observed response time (sometimes combined with active connections).

**Advantages**
- Can improve user-perceived latency.
- Adapts to runtime bottlenecks quickly.

**Disadvantages**
- Sensitive to noisy latency measurements.
- Requires health/metrics collection and smoothing.
- May oscillate if not stabilized.

**Use Cases**
- Latency-sensitive APIs.
- Systems where backend performance changes rapidly.

---

### 6) IP Hash (Source Hash)

A hash of client IP is used to select backend; same client usually maps to same server.

**Advantages**
- Provides basic session stickiness without server-side session sharing.
- Deterministic routing.

**Disadvantages**
- Uneven distribution if IP distribution is skewed.
- Rebalancing causes remapping when backend pool changes.
- Not ideal for NAT-heavy clients (many users behind same IP).

**Use Cases**
- Legacy apps requiring sticky sessions.
- Lightweight personalization caches tied to instance memory.

---

### 7) URL/Path Hash

Request URL/path is hashed to choose backend.

**Advantages**
- Requests for same content can consistently hit same backend cache.
- Can improve cache hit ratio.

**Disadvantages**
- Hot URLs can overload one backend.
- Requires careful handling when server pool changes.

**Use Cases**
- Content-serving systems with cache-heavy workloads.
- Media/CDN edge routing patterns.

---

### 8) Random / Power of Two Choices

- **Random**: pick a backend randomly.
- **Power of Two Choices**: pick 2 random backends and choose the less loaded one.

**Advantages**
- Random is very cheap.
- Power of Two Choices provides near-least-load quality with low overhead.
- Scales well in large clusters.

**Disadvantages**
- Pure random can have short-term imbalance.
- Needs lightweight load signal for the "less loaded" decision.

**Use Cases**
- Very large distributed systems.
- Service mesh sidecars doing high-frequency balancing.

---

### 9) Least Bandwidth / Least Requests

Route to backend currently serving the least bandwidth or request count.

**Advantages**
- Useful when request payload sizes vary a lot.
- Better fit for traffic-heavy or file-serving endpoints.

**Disadvantages**
- Requires accurate real-time metrics.
- Metric lag can lead to wrong choices.

**Use Cases**
- File upload/download services.
- Media or report generation endpoints.

## How to Choose an Algorithm

Use this practical guide:

- If traffic is mostly uniform and stateless -> **Round Robin**.
- If servers have different capacities -> **Weighted Round Robin** or **Weighted Least Connections**.
- If request duration varies a lot -> **Least Connections**.
- If latency is critical -> **Least Response Time**.
- If stickiness is required -> **IP Hash**.
- If huge scale with low overhead is needed -> **Power of Two Choices**.

## Practical Notes

- Always pair algorithm choice with **health checks** and **outlier detection**.
- Revisit algorithm after major architecture changes (e.g., caching layer added).
- Measure impact with real metrics: `p95 latency`, `error rate`, `throughput`, and `backend CPU`.
- First choose LB type (**L4 vs L7**), then choose the balancing algorithm (**round robin, least connections, etc.**).

## Interview Tip

In system design interviews, mention:
1. Why your selected algorithm matches traffic pattern.
2. Trade-off between simplicity and adaptation to live load.
3. Operational safeguards (health checks, retries, circuit breaker).

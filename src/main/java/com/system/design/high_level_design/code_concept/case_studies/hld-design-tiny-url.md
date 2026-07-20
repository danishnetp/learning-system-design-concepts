# Tiny URL System Design

## 1. Requirement Analysis

### a. How short our URL should be?

**Objective:** Determine the optimal length of the shortened URL.

**Considerations:**
- **User-friendly length**: The shortened URL should be short enough to be easily shared via social media, emails, and messaging platforms without being truncated or looking unwieldy.
- **Memorability**: A shorter URL is easier to remember and manually type if needed.
- **URL shortening factor**: Compare the length reduction between original URL and shortened URL.
  - Original URL example: `https://www.example.com/products/electronics/smartphones/iphone-15-pro-max-256gb-silver?utm_source=facebook&utm_medium=social&utm_campaign=summer_sale&discount=20%`
  - Shortened URL example: `https://tiny.url/abc123d`

**Recommendation:**
- **Typical shortened URL length**: 6-8 characters after the domain
- **Common industry standard**: 7 characters is widely used
  - Example: `tiny.url/abcd123`
  - Provides good balance between brevity and collision avoidance

**Trade-offs:**
- **Too short (3-4 chars)**: Risk of URL collisions and limited namespace
- **Too long (10+ chars)**: Defeats the purpose of URL shortening, becomes less memorable
- **Optimal (6-8 chars)**: Balance between collision resistance and usability

---

### b. What would be the traffic - 10 Million in a day?

**Traffic Estimates:**

**Daily Traffic:**
- 10 Million URL shortening requests per day (write operations)
- This includes both:
  - Creating new shortened URLs
  - Redirecting users to original URLs (read operations)

**Traffic Distribution:**
- **Reads vs Writes**: Typically, a URL shortening service experiences 100:1 read-to-write ratio
  - Write operations: 10 Million/day
  - Read operations: ~1 Billion/day (estimated)
  - Ratio: For every URL created, it's accessed ~100 times

**Per-Second Calculations:**
- **Writes (Creating short URLs):**
  - 10 Million / 86,400 seconds = ~115-116 requests per second (RPS)
  - Peak traffic (assuming 5x spike): ~575 RPS

- **Reads (Redirecting to original URL):**
  - 1 Billion / 86,400 seconds = ~11,574 RPS
  - Peak traffic (assuming 5x spike): ~57,870 RPS

**Scalability Requirements:**
- **Database capacity**: Need to handle both read and write queries
- **Caching strategy**: Essential for read-heavy workloads to reduce database load
- **Load balancing**: Required to distribute traffic across multiple servers
- **CDN deployment**: Useful for geographically distributed users

**Storage Estimates (Annual):**
- Assuming 10 Million new URLs created per day
- Annual creation: 10M × 365 = 3.65 Billion URLs per year
- Average storage per URL entry: ~500 bytes (ID, shortened key, original URL, metadata, timestamps)
- Annual storage needed: 3.65B × 500 bytes ≈ 1.8 TB/year

---

### c. What would be the character size of tiny URL - 0-9/a-z/A-Z character sets?

**Character Set Analysis:**

**Available Character Set:**
- **Digits**: 0-9 (10 characters)
- **Lowercase letters**: a-z (26 characters)
- **Uppercase letters**: A-Z (26 characters)
- **Total**: 10 + 26 + 26 = **62 characters**

**Base Conversion:**
This character set represents a **Base-62 encoding system**, where each position can have 62 possible values.

**URL Length vs Namespace Size Calculation:**

| Short URL Length | Total Possible URLs | Capacity      | Suitable For     |
|------------------|---------------------|---------------|------------------|
| 4 characters     | 62^4 = 14.8M        | 14.8 Million  | Small scale      |
| 5 characters     | 62^5 = 916.1M       | 916 Million   | Medium scale     |
| 6 characters     | 62^6 = 56.8B        | 56.8 Billion  | Large scale      |
| 7 characters     | 62^7 = 3.5T         | 3.5 Trillion  | Very large scale |
| 8 characters     | 62^8 = 218T         | 218 Trillion  | Enterprise scale |

**Example Base-62 Character Mapping:**
```
0-9    : 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 (positions 0-9)
a-z    : a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z (positions 10-35)
A-Z    : A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z (positions 36-61)
```

**Recommendation for Given Requirements:**

Given:
- **10 Million URLs per day** = 3.65 Billion URLs per year
- Need headroom for growth (assume 5-10 years)
- Total URLs to support: ~37.25 Billion URLs

**Optimal Length: 7 characters**

**Justification:**
- 62^7 = **3.5 Trillion** possible URLs
- Can handle 3.5 Trillion / 37.25 Billion = ~94x growth
- Sufficient safety margin for future scaling
- Still maintains the "short" aspect of the URL
- Industry standard and well-balanced

**Implementation Considerations:**
- **Collision Prevention**: With 7 characters in base-62, probability of collision is very low
- **Encoding Algorithm**: Use Base-62 encoding to convert numeric ID to shortened URL
  - Example: ID 123456 → Base-62 encoded → `2n9c`
- **Decoding**: Convert shortened URL back to numeric ID to retrieve original URL
- **Uniqueness**: Use database sequence/auto-increment to ensure each URL gets a unique ID

---

## Summary Table

| Parameter                    | Value                   | Details                         |
|------------------------------|-------------------------|---------------------------------|
| **Shortened URL Length**     | 7 characters            | Industry standard, good balance |
| **Daily Traffic**            | 10 Million writes       | 115-116 RPS (peak: 575 RPS)     |
| **Read Traffic**             | 1 Billion/day (est.)    | 11,574 RPS (peak: 57,870 RPS)   |
| **Character Set**            | Base-62 (0-9, a-z, A-Z) | 62 characters total             |
| **Total Capacity (7 chars)** | 3.5 Trillion URLs       | ~94x growth headroom            |
| **Annual Storage**           | ~1.8 TB                 | For 3.65 Billion URLs           |

---

## 2. Functional Requirements

Functional requirements define what the system should do and the features it should provide.

### Core Features

#### 2.1 URL Shortening Service
- **Create Short URL**
  - Accept a long original URL as input
  - Generate a unique, shorter URL
  - Return the shortened URL to the user
  - Support custom short codes (optional)
  - Validate input URLs for correctness
  - Handle duplicate long URLs (return existing short URL or create new one)

#### 2.2 URL Redirection Service
- **Retrieve Original URL**
  - Accept a shortened URL as input
  - Look up the mapping in the database
  - Redirect user to the original long URL using HTTP 301/302 redirect
  - Handle expired or deleted shortened URLs with appropriate error responses
  - Track redirect statistics (click count, timestamps, user location)

#### 2.3 URL Management
- **Delete Shortened URL**
  - Allow users to delete their created shortened URLs
  - Support batch deletion operations
  - Soft delete (mark as inactive) for audit trail
  - Handle authorization and ownership verification

- **Update Shortened URL**
  - Allow updating metadata (expiry date, custom alias, description)
  - Update the target original URL if needed
  - Maintain audit logs of modifications

#### 2.4 URL Analytics & Tracking
- **Click Statistics**
  - Track number of clicks/redirects per shortened URL
  - Record timestamp, user agent, referrer, IP address
  - Geographic location tracking
  - Conversion tracking if applicable

- **User Management**
  - User registration and authentication
  - User dashboard to view all created shortened URLs
  - Personal URL statistics and analytics
  - API key generation for programmatic access

#### 2.5 Batch Operations
- **Create Multiple Short URLs** in a single request
- **Export analytics** in various formats (CSV, JSON)
- **Bulk delete** shortened URLs

---

## 3. Non-Functional Requirements

Non-functional requirements define system qualities like performance, availability, security, and scalability.

### 3.1 Performance Requirements
- **Redirect Latency**: < 100ms for URL redirection (P99)
- **Create Short URL Latency**: < 200ms (P99)
- **Throughput**: Handle 11,574 RPS read operations and 116 RPS write operations
- **Peak Load Handling**: Support 5x traffic spike (57,870 RPS reads)
- **Cache Hit Ratio**: Maintain > 80% cache hit ratio for frequently accessed URLs

### 3.2 Availability & Reliability
- **System Availability**: 99.99% uptime (4 nines) = 43.2 seconds downtime per month
- **Recovery Time Objective (RTO)**: < 1 minute
- **Recovery Point Objective (RPO)**: < 5 minutes (acceptable data loss)
- **Graceful Degradation**: System should degrade gracefully during failures
- **Redundancy**: Multi-region deployment with failover mechanism
- **Data Replication**: Synchronous replication across at least 2 data centers

### 3.3 Scalability Requirements
- **Horizontal Scalability**: Add more servers without system redesign
- **Database Sharding**: Partition data across multiple database instances
- **Load Balancing**: Distribute incoming requests across servers
- **Auto-scaling**: Automatically scale up/down based on traffic patterns
- **Support Growth**: System should support 10x growth without major architectural changes

### 3.4 Security Requirements
- **HTTPS/TLS**: All communications encrypted
- **Authentication**: OAuth 2.0 or JWT-based authentication for user access
- **Authorization**: Role-based access control (RBAC)
- **Rate Limiting**: Prevent abuse (10 requests/second per user)
- **Input Validation**: Sanitize all inputs to prevent SQL injection, XSS attacks
- **Data Protection**: Encrypt sensitive data at rest and in transit
- **Audit Logging**: Log all operations for compliance and security audit trails

### 3.5 Data Consistency & Durability
- **Data Durability**: Guaranteed persistence of all shortened URL mappings
- **Eventual Consistency**: Acceptable for read replicas after write operations
- **ACID Compliance**: Ensure data integrity for write operations
- **Backup Strategy**: Automated daily backups with point-in-time recovery

### 3.6 Maintainability
- **Monitoring & Alerting**: Real-time system monitoring and alerting for anomalies
- **Logging**: Structured logging with centralized log aggregation (ELK Stack)
- **Metrics**: Prometheus metrics for system observability
- **Documentation**: API documentation, deployment guides, runbooks
- **Cost Efficiency**: Optimize infrastructure costs while maintaining performance

---

## 4. Unique URL Generation Strategies

Different strategies to generate unique shortened URLs with their pros and cons.

### 4.1 Database Auto-Increment + Base-62 Encoding

**Approach:**
- Use database auto-increment ID as the unique identifier
- Convert the numeric ID to Base-62 encoding
- Store the mapping: (shortened_url, original_url, id)

**Advantages:**
- ✅ Simple and straightforward implementation
- ✅ No collision risk (sequential IDs are unique)
- ✅ Easy to decode and retrieve original URL
- ✅ Predictable growth, easy capacity planning
- ✅ Good cache locality for sequential reads

**Disadvantages:**
- ❌ Sequential IDs are predictable (security concern)
- ❌ Single point of failure (database must be up)
- ❌ Requires database round-trip for ID generation
- ❌ Single-threaded bottleneck for ID generation in high-throughput scenarios

**Implementation Example:**
```
ID: 123456
Base-62 Encoding: 2n9c
Shortened URL: tiny.url/2n9c
```

**When to Use:**
- Low to medium traffic scenarios
- When predictability is acceptable
- Monolithic architecture

---

### 4.2 UUID + Hashing

**Approach:**
- Generate a UUID (Universally Unique Identifier)
- Hash the UUID using MD5/SHA-256
- Take first 6-7 characters of the hash
- Handle collisions with retry logic

**Advantages:**
- ✅ Globally unique, no coordination needed
- ✅ Non-sequential, less predictable
- ✅ Decentralized generation (no single point of failure)
- ✅ Suitable for distributed systems

**Disadvantages:**
- ❌ Collision handling complexity
- ❌ Cannot reverse-engineer the original hash
- ❌ Requires additional storage to track used hashes
- ❌ Higher computational overhead (hashing)

**Implementation:**
```
UUID: 550e8400-e29b-41d4-a716-446655440000
Hash (MD5): 6a5edccf9be641ca51d923f23421b7cb
First 6 chars: 6a5edc
Shortened URL: tiny.url/6a5edc
Collision Resolution: If collision, regenerate new UUID and retry
```

**When to Use:**
- Distributed/microservices architecture
- When unpredictability is important for security
- High-throughput, distributed systems

---

### 4.3 Snowflake ID (Twitter's ID Generation)

**Approach:**
- Combination of timestamp, machine ID, and sequence number
- Distributed ID generation without central coordinator
- Format: [Timestamp(41 bits) | DataCenter(5 bits) | Worker(5 bits) | Sequence(12 bits)]

**Advantages:**
- ✅ Highly scalable for distributed systems
- ✅ Sortable by timestamp
- ✅ No single point of failure
- ✅ Time-ordered (enables range queries)
- ✅ Very low collision probability

**Disadvantages:**
- ❌ Clock synchronization required across servers
- ❌ More complex implementation
- ❌ Requires careful management of machine IDs
- ❌ Sequential nature may still expose patterns

**Implementation Concept:**
```
Timestamp: 1626249600 (milliseconds since epoch)
DataCenter ID: 01
Worker ID: 02
Sequence: 000
Generated ID: Combines all above
Base-62 Encoded: 8a9kL2
```

**When to Use:**
- Very high-scale distributed systems
- When you need time-ordering of IDs
- Multiple data centers with different time zones

---

### 4.4 Random String Generation + Collision Resolution

**Approach:**
- Generate a random string of 6-7 characters from Base-62 alphabet
- Check if it already exists in the database
- If collision, retry with a new random string
- Use probabilistic data structures (Bloom Filter) for faster collision detection

**Advantages:**
- ✅ Simple and flexible
- ✅ Random generation reduces predictability
- ✅ Easy to implement custom encoding rules
- ✅ Suitable for smaller systems

**Disadvantages:**
- ❌ Requires multiple database lookups for collision checks
- ❌ Performance degradation as namespace fills up (birthday paradox)
- ❌ Retry logic adds complexity
- ❌ Not suitable for very high-throughput systems

**Collision Probability (Birthday Paradox):**
```
For 7-character Base-62 (3.5T possible values):
- After 1.17 Billion URLs: 1% collision probability
- After 58.6 Billion URLs: 50% collision probability
```

**When to Use:**
- Medium traffic scenarios
- When simplicity is prioritized
- Systems with moderate load

---

### 4.5 Hybrid Approach: Zookeeper + Distributed ID Generation

**Approach:**
- Use Zookeeper for distributed coordination
- Multiple ID generator services maintain unique ranges
- Each service allocates IDs from its range
- Prevents collisions through range allocation

**Advantages:**
- ✅ Highly scalable for distributed systems
- ✅ No collisions (coordinated ranges)
- ✅ Supports multiple ID generators
- ✅ Handles machine failures gracefully

**Disadvantages:**
- ❌ Dependency on Zookeeper cluster
- ❌ More operational complexity
- ❌ Zookeeper coordination overhead
- ❌ Requires careful range management

**When to Use:**
- Large-scale distributed systems
- When you need guaranteed uniqueness with coordination
- Systems with multiple ID generation servers

---

## 5. Scale Estimation and Identifying Bottlenecks

### 5.1 Traffic Scale Analysis

**Capacity Planning:**

| Metric | Value | Calculation |
|--------|-------|-------------|
| Daily New URLs | 10 Million | Given |
| Monthly New URLs | 300 Million | 10M × 30 |
| Yearly New URLs | 3.65 Billion | 10M × 365 |
| Read-to-Write Ratio | 100:1 | Industry standard |
| Daily Reads | 1 Billion | 10M × 100 |
| Write RPS | ~116 | 10M / 86,400 sec |
| Read RPS | ~11,574 | 1B / 86,400 sec |
| Peak Read RPS (5x spike) | ~57,870 | 11,574 × 5 |
| Peak Write RPS (5x spike) | ~580 | 116 × 5 |

---

### 5.2 Storage Estimation

**Data Growth:**
```
Per URL Entry:
├── shortened_key: 8 bytes (7 chars + 1 null)
├── original_url: ~200 bytes (average URL length)
├── user_id: 8 bytes (long)
├── created_at: 8 bytes (timestamp)
├── expires_at: 8 bytes (optional expiry)
├── click_count: 8 bytes
└── metadata: 200 bytes
└─ Total per entry: ~440 bytes

Annual Storage:
├── 3.65 Billion URLs created per year
├── 3.65B × 440 bytes ≈ 1.6 TB per year
├── 5-year retention: 8 TB
└── With replication (3x): 24 TB

Analytics Storage (clickstream data):
├── 1 Billion clicks per day
├── 50 bytes per click record
├── 1B × 50 bytes × 365 = ~18 TB per year
└── Typical retention: 90 days = ~1.5 TB
```

---

### 5.3 Bottleneck Analysis

#### 5.3.1 Database Write Bottleneck

**Issue:**
- Single database instance can handle ~5,000-10,000 writes/second
- Our peak write load: ~580 RPS (manageable)
- But with hot URLs and retries, real throughput may be higher

**Solution:**
```
├── Database Sharding by User ID
│   └── Distribute URLs across multiple database instances
│       └── Shard Key: user_id % num_shards
│       └── 8-16 shards for 580 peak RPS
│
├── Write Optimization
│   ├── Batch inserts (10-50 URLs per batch)
│   ├── Connection pooling (200-500 connections)
│   └── Asynchronous writes with durability guarantee
│
└── Replication
    ├── Master-Slave replication for failover
    └── Synchronous replication to 2 replicas
```

#### 5.3.2 Database Read Bottleneck

**Issue:**
- Peak read load: ~57,870 RPS
- Single database: 10,000-15,000 reads/second
- **Need 4-6x database capacity**

**Solution:**
```
├── Multi-Level Caching Strategy
│   ├── L1: In-Memory Cache (Redis) - 50-100GB
│   │   └── Cache hit ratio target: 80%
│   │   └── TTL: 24 hours for popular URLs
│   │   └── Reduces load to: 57,870 × 0.2 = 11,574 RPS
│   │
│   ├── L2: Local In-App Cache (Guava/Caffeine)
│   │   └── 10GB per application instance
│   │   └── Further reduces DB load by 20%
│   │
│   └── L3: Database Read Replicas
│       └── 4-6 read replicas
│       └── Each handles: 11,574 / 6 ≈ 1,929 RPS
│
├── CDN for Static Content
│   └── Cache HTTP redirects at edge nodes
│   └── Geographically distributed cache hits
│
└── Database Query Optimization
    ├── Indexing (shortened_key, user_id)
    ├── Query caching at application level
    └── Connection pooling (1000+ connections)
```

#### 5.3.3 CPU Bottleneck

**Issue:**
- Base-62 encoding/decoding: ~0.1ms per operation
- For 116 writes/sec: ~11ms CPU time per second
- For 11,574 reads/sec: ~1,157ms CPU time per second = 1.157 cores needed

**Solution:**
```
├── Application Server Count
│   └── Peak load 57,870 RPS with 1ms per operation
│   └── Need at least: 57,870ms / 1000 = 57 cores
│   └── With hyperthreading: ~30-40 server instances
│   └── Each instance: 4 cores, 8GB RAM
│
├── Load Balancing
│   ├── Round-robin across instances
│   ├── Health checks every 10 seconds
│   └── Sticky sessions optional (not required)
│
└── Horizontal Scaling
    └── Auto-scale between 20-40 instances based on CPU usage
```

#### 5.3.4 Network Bottleneck

**Issue:**
- 1 Billion daily reads with ~200-byte responses
- 1B × 200 bytes = 200 GB data transfer per day
- Per second: 200GB / 86,400 = 2.3 MB/sec

**Calculation:**
```
Bandwidth needed: 200GB/day = 18.5 Mbps average
Peak (5x): 92.5 Mbps
```

**Not a bottleneck** for standard enterprise networks (1 Gbps available).

**Solution:**
- Gzip compression (reduces response by 70-80%)
- HTTP Keep-Alive for connection reuse
- CDN for geographic distribution

#### 5.3.5 ID Generation Bottleneck

**Issue:**
- Single database sequence can handle 10,000 IDs/sec
- Our peak: 580 RPS (manageable)
- **But** requires network round-trip to database

**Solution:**
```
├── ID Range Server
│   ├── Pre-allocate ID ranges to each app server
│   ├── Each server gets 100k ID range
│   ├── Reduces coordination overhead
│   └── Handles: 100k IDs × 10 servers = 1M IDs buffered
│
├── Distributed ID Generation (Zookeeper)
│   ├── Multiple ID generators with coordinated ranges
│   ├── No single point of failure
│   └── Discussed in detail below
│
└── In-Memory Sequence
    ├── Start: sequence = 0
    ├── Increment: sequence += 1
    └── Every N IDs, persist sequence to DB
```

---

### 5.4 Bottleneck Summary Table

| Bottleneck | Current Load | Capacity | Issue | Solution |
|-----------|--------------|----------|-------|----------|
| **Database Writes** | 580 RPS peak | 5K-10K RPS | No immediate issue | Sharding after 2K RPS |
| **Database Reads** | 57,870 RPS peak | 10-15K RPS | **CRITICAL** | Multi-level cache + read replicas |
| **CPU** | ~58 cores needed | Limited per instance | **HIGH** | Horizontal scaling to 30-40 instances |
| **Memory** | ~40 GB cache | 8GB per instance | **MODERATE** | Distributed cache (Redis cluster) |
| **ID Generation** | 116-580 RPS | 10K RPS | No issue | Range-based pre-allocation for safety |
| **Network I/O** | 18.5 Mbps avg | 1 Gbps available | No issue | Compression + CDN |

---

## 6. API Design

### 6.1 Create Short URL

**Endpoint:** `POST /api/v1/shorten`

**Request Body:**
```json
{
  "original_url": "https://www.example.com/very/long/path?param1=value1&param2=value2",
  "custom_alias": "mycustomurl",        // Optional: custom short code
  "expiry_days": 365,                   // Optional: URL expiry in days
  "tags": ["marketing", "campaign"],    // Optional: for categorization
  "description": "Summer sale link"     // Optional: description
}
```

**Success Response (201 Created):**
```json
{
  "status": "success",
  "data": {
    "short_url": "https://tiny.url/abc123d",
    "shortened_key": "abc123d",
    "original_url": "https://www.example.com/very/long/path?param1=value1&param2=value2",
    "created_at": "2026-07-15T10:30:00Z",
    "expires_at": "2027-07-15T10:30:00Z",
    "clicks": 0,
    "qr_code_url": "https://tiny.url/qr/abc123d.png"  // Optional QR code
  }
}
```

**Error Responses:**
```json
// 400 Bad Request - Invalid URL
{
  "status": "error",
  "code": "INVALID_URL",
  "message": "Provided URL is invalid or malformed"
}

// 409 Conflict - Custom alias already taken
{
  "status": "error",
  "code": "ALIAS_TAKEN",
  "message": "Custom alias 'mycustomurl' is already taken"
}

// 429 Too Many Requests - Rate limit exceeded
{
  "status": "error",
  "code": "RATE_LIMIT_EXCEEDED",
  "message": "You have exceeded the rate limit. Retry after 60 seconds"
}

// 401 Unauthorized - Not authenticated
{
  "status": "error",
  "code": "UNAUTHORIZED",
  "message": "Authentication required"
}
```

**Request Headers:**
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
User-Agent: <client_identifier>
```

---

### 6.2 Redirect to Original URL

**Endpoint:** `GET /{shortened_key}`

**Path Parameters:**
- `shortened_key` (string, 6-8 chars): The shortened URL key

**Response (301/302 Redirect):**
```
HTTP/1.1 301 Moved Permanently
Location: https://www.example.com/very/long/path?param1=value1&param2=value2
Cache-Control: public, max-age=3600
X-Tiny-Url-Id: abc123d
```

**Redirect Choice:**
- **301 (Permanent Redirect)**: Used for permanent URL mappings, cached by browsers
- **302 (Temporary Redirect)**: Used for dynamic/expiring URLs, not cached

**Tracking Query Parameters (Optional):**
```
GET /abc123d?utm_source=email&utm_medium=newsletter&utm_campaign=july2026
```
The parameters are preserved and forwarded to the original URL.

**Error Responses:**

```json
// 404 Not Found - Shortened URL doesn't exist
{
  "status": "error",
  "code": "URL_NOT_FOUND",
  "message": "The shortened URL 'abc123d' was not found"
}

// 410 Gone - URL has been deleted or expired
{
  "status": "error",
  "code": "URL_EXPIRED",
  "message": "The shortened URL has expired or been deleted"
}

// 429 Too Many Requests - Rate limit exceeded
{
  "status": "error",
  "code": "RATE_LIMIT_EXCEEDED",
  "message": "Too many redirect requests from this IP"
}
```

---

### 6.3 Get URL Statistics

**Endpoint:** `GET /api/v1/shorten/{shortened_key}/stats`

**Success Response:**
```json
{
  "status": "success",
  "data": {
    "shortened_key": "abc123d",
    "short_url": "https://tiny.url/abc123d",
    "original_url": "https://www.example.com/very/long/path",
    "created_at": "2026-07-15T10:30:00Z",
    "expires_at": "2027-07-15T10:30:00Z",
    "total_clicks": 45678,
    "clicks_today": 1234,
    "clicks_this_week": 8765,
    "clicks_this_month": 35000,
    "geographic_distribution": {
      "US": 25000,
      "UK": 10000,
      "IN": 8000,
      "Other": 2678
    },
    "referrer_distribution": {
      "direct": 20000,
      "facebook": 15000,
      "twitter": 8000,
      "email": 2678
    },
    "device_distribution": {
      "mobile": 25000,
      "desktop": 18000,
      "tablet": 2678
    },
    "click_trend": [
      {"date": "2026-07-15", "clicks": 1200},
      {"date": "2026-07-14", "clicks": 1100},
      {"date": "2026-07-13", "clicks": 950}
    ]
  }
}
```

---

### 6.4 Delete Shortened URL

**Endpoint:** `DELETE /api/v1/shorten/{shortened_key}`

**Success Response (204 No Content):**
```
HTTP/1.1 204 No Content
```

**Error Responses:**
```json
// 404 Not Found
{
  "status": "error",
  "code": "URL_NOT_FOUND",
  "message": "The shortened URL 'abc123d' was not found"
}

// 403 Forbidden - User is not the owner
{
  "status": "error",
  "code": "FORBIDDEN",
  "message": "You do not have permission to delete this URL"
}

// 401 Unauthorized
{
  "status": "error",
  "code": "UNAUTHORIZED",
  "message": "Authentication required"
}
```

---

### 6.5 Update Shortened URL

**Endpoint:** `PATCH /api/v1/shorten/{shortened_key}`

**Request Body:**
```json
{
  "expiry_days": 180,         // Optional: update expiry
  "description": "Updated",   // Optional: update description
  "tags": ["new_tag"]         // Optional: update tags
  // Note: Cannot change original_url or shortened_key
}
```

**Success Response:**
```json
{
  "status": "success",
  "data": {
    "shortened_key": "abc123d",
    "updated_at": "2026-07-15T11:00:00Z",
    "expires_at": "2027-01-12T11:00:00Z",
    "description": "Updated",
    "tags": ["new_tag"]
  }
}
```

---

### 6.6 List User's URLs

**Endpoint:** `GET /api/v1/shorten?page=1&limit=20&sort=created_at&order=desc`

**Query Parameters:**
- `page` (int, default=1): Page number for pagination
- `limit` (int, default=20, max=100): URLs per page
- `sort` (string, default=created_at): Sort by field (created_at, clicks, expires_at)
- `order` (string, default=desc): Ascending (asc) or Descending (desc)
- `tags` (string, optional): Filter by comma-separated tags
- `search` (string, optional): Search in original URL

**Success Response:**
```json
{
  "status": "success",
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 150,
    "total_pages": 8
  },
  "data": [
    {
      "shortened_key": "abc123d",
      "short_url": "https://tiny.url/abc123d",
      "original_url": "https://www.example.com/path1",
      "created_at": "2026-07-15T10:30:00Z",
      "expires_at": "2027-07-15T10:30:00Z",
      "clicks": 1234,
      "tags": ["marketing"]
    },
    {
      "shortened_key": "xyz789a",
      "short_url": "https://tiny.url/xyz789a",
      "original_url": "https://www.example.com/path2",
      "created_at": "2026-07-14T09:15:00Z",
      "expires_at": "2027-07-14T09:15:00Z",
      "clicks": 5678,
      "tags": ["campaign", "social"]
    }
  ]
}
```

---

### 6.7 Batch Create URLs

**Endpoint:** `POST /api/v1/shorten/batch`

**Request Body:**
```json
{
  "urls": [
    {
      "original_url": "https://example.com/path1"
    },
    {
      "original_url": "https://example.com/path2",
      "custom_alias": "custom2"
    }
  ]
}
```

**Success Response:**
```json
{
  "status": "success",
  "data": {
    "created": 2,
    "failed": 0,
    "urls": [
      {
        "short_url": "https://tiny.url/abc123d",
        "original_url": "https://example.com/path1",
        "status": "success"
      },
      {
        "short_url": "https://tiny.url/custom2",
        "original_url": "https://example.com/path2",
        "status": "success"
      }
    ]
  }
}
```

---

## 7. Collision Handling Using Zookeeper

Zookeeper is a distributed coordination service that helps maintain distributed consensus and prevent collisions in a decentralized URL generation system.

### 7.1 Problem Statement

**Challenges in Distributed ID Generation:**
1. **Duplicate IDs**: Multiple ID generators may generate the same ID
2. **Race Conditions**: Concurrent requests may receive identical IDs
3. **Single Point of Failure**: Central database sequence creates bottleneck
4. **Scalability**: Cannot efficiently handle 1000+ ID generators

### 7.2 Zookeeper Architecture

**Components:**
```
┌─────────────────────────────────────────────────┐
│              ZK Cluster (3-5 nodes)             │
│  ┌────────────────────────────────────────────┐ │
│  │  /idgen/server1: 1000000-1999999           │ │
│  │  /idgen/server2: 2000000-2999999           │ │
│  │  /idgen/server3: 3000000-3999999           │ │
│  │  /idgen/server4: 4000000-4999999           │ │
│  │  /idgen/server5: 5000000-5999999           │ │
│  └────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────┘
         ↑           ↑           ↑
    [Server1]   [Server2]   [Server3]
    (app srv)   (app srv)   (app srv)
```

### 7.3 Zookeeper-Based ID Generation Strategy

**Step 1: Initialize Zookeeper Hierarchy**
```
Create persistent paths:
/idgen                                    # Root node
├── /idgen/config                         # Configuration
│   ├── /idgen/config/range_size = 1000000
│   ├── /idgen/config/current_max = 5000000
│   └── /idgen/config/server_count = 5
├── /idgen/servers                        # Server allocations
│   ├── /idgen/servers/server-1           # Ephemeral nodes
│   ├── /idgen/servers/server-2
│   ├── /idgen/servers/server-3
│   ├── /idgen/servers/server-4
│   └── /idgen/servers/server-5
└── /idgen/ranges                         # Allocated ranges
    ├── /idgen/ranges/0: server-1 → 1000000-1999999
    ├── /idgen/ranges/1: server-2 → 2000000-2999999
    ├── /idgen/ranges/2: server-3 → 3000000-3999999
    ├── /idgen/ranges/3: server-4 → 4000000-4999999
    └── /idgen/ranges/4: server-5 → 5000000-5999999
```

**Step 2: Server Startup and Registration**
```
When a new application server starts:

1. Connect to Zookeeper ensemble
2. Create ephemeral node: /idgen/servers/{server-uuid}
   └── Stores: hostname, port, timestamp
3. Watch /idgen/ranges for configuration changes
4. Allocate a new ID range:
   ├── Read /idgen/config/current_max
   ├── Calculate: max_id + 1 to max_id + range_size
   ├── Create: /idgen/ranges/{sequence}: allocated_range
   └── Update: /idgen/config/current_max = new_max
```

**Code Pseudocode:**
```java
class ZooKeeperIDGenerator {
    private ZooKeeper zk;
    private long currentMin;
    private long currentMax;
    private long sequenceNumber = 0;
    
    public void startup() {
        zk = new ZooKeeper("zk-server:2181", 3000, this::handleEvent);
        
        // Ensure root paths exist
        ensurePath("/idgen/servers");
        ensurePath("/idgen/ranges");
        
        // Register this server
        registerServer();
        
        // Allocate ID range
        allocateIDRange();
    }
    
    private void registerServer() {
        String serverId = UUID.randomUUID().toString();
        String path = "/idgen/servers/" + serverId;
        
        byte[] data = getServerInfo().getBytes();
        zk.create(path, data, 
                  ZooDefs.Ids.OPEN_ACL_UNSAFE,
                  CreateMode.EPHEMERAL);  // Auto-deleted on disconnect
    }
    
    private void allocateIDRange() {
        synchronized (this) {
            // Read current max
            byte[] data = zk.getData("/idgen/config/current_max", null, null);
            long maxId = Long.parseLong(new String(data));
            
            // Calculate new range
            currentMin = maxId + 1;
            currentMax = maxId + RANGE_SIZE;  // e.g., 1,000,000
            sequenceNumber = 0;
            
            // Update in Zookeeper (atomic)
            zk.setData("/idgen/config/current_max",
                      String.valueOf(currentMax).getBytes(),
                      -1);
        }
    }
    
    public synchronized long getNextID() {
        // Check if we need to allocate new range
        if (sequenceNumber >= RANGE_SIZE) {
            allocateIDRange();
        }
        
        return currentMin + (sequenceNumber++);
    }
}
```

---

### 7.4 Collision Handling Scenarios

#### Scenario 1: New Server Joining

**Process:**
```
Server-6 joins the cluster:

1. Connects to Zookeeper
2. Reads /idgen/config/current_max = 5000000
3. Allocates range: 5000001 - 6000000
4. Creates /idgen/servers/server-6 (ephemeral)
5. Creates /idgen/ranges/5: server-6 → 5000001-6000000
6. Starts generating IDs from its range

No collision because:
✓ Range allocation is atomic in Zookeeper
✓ Each server has exclusive range
✓ Ranges don't overlap
```

#### Scenario 2: Server Failure/Graceful Shutdown

**Process:**
```
Server-3 crashes:

1. Ephemeral node /idgen/servers/server-3 auto-deleted (heartbeat lost)
2. Other servers watching /idgen/servers are notified
3. Range /idgen/ranges/2 (3000000-2999999) becomes available
4. New server taking over can request the range again:
   └── Allocates new range (e.g., 6000001-7000000)
   └── Old range may be reused after cleanup process

No collision because:
✓ Zookeeper detects server failure immediately
✓ Ranges are reassigned through consensus
✓ Cleanup process ensures no overlap
```

#### Scenario 3: Network Partition

**Process:**
```
Network split: Servers 1-3 vs Servers 4-5 disconnected

Servers 1-3 (Minority partition):
  - Lose connection to majority Zookeeper quorum
  - STOP generating IDs (fail-safe)
  - Wait for partition healing

Servers 4-5 (Majority partition):
  - Continue normal operation
  - Can generate IDs
  - When partition heals, minority partition reconnects

Result:
✓ No collision: only majority partition continues
✓ CAP Theorem: Chooses Consistency over Availability
✓ When healed, all servers sync with Zookeeper state
```

---

### 7.5 Benefits of Zookeeper Approach

| Benefit | Explanation |
|---------|-------------|
| **No Collisions** | Atomic range allocation ensures unique ID ranges |
| **Scalability** | Can handle hundreds of ID generators |
| **High Availability** | Zookeeper quorum provides fault tolerance |
| **Distributed** | No single point of failure |
| **Auto-recovery** | Ephemeral nodes enable automatic detection of failures |
| **Consensus** | All servers agree on ID allocation through Zookeeper |
| **Easy Debugging** | Zookeeper UI shows ID allocation state |

---

### 7.6 Configuration and Tuning

**Zookeeper Cluster Setup (3 nodes - minimum for production):**

**Node 1 (zoo.cfg):**
```properties
tickTime=2000
dataDir=/var/lib/zookeeper

# Server configuration
server.1=zk-server-1:2888:3888
server.2=zk-server-2:2888:3888
server.3=zk-server-3:2888:3888

# Client port
clientPort=2181

# Session timeout
minSessionTimeout=4000
maxSessionTimeout=40000
```

**Application Configuration:**
```properties
# Zookeeper connection string
zookeeper.hosts=zk-server-1:2181,zk-server-2:2181,zk-server-3:2181

# ID allocation
idgen.range_size=1000000           # IDs per server
idgen.init_max_id=0                # Starting max ID
idgen.reallocation_threshold=0.8   # Reallocate when 80% used

# Zookeeper timeouts
zookeeper.connection_timeout=30000
zookeeper.session_timeout=30000
```

---

### 7.7 Comparison with Other Approaches

| Approach | Collisions | Single Point of Failure | Complexity | Scalability |
|----------|-----------|------------------------|------------|------------|
| **Database Sequence** | ❌ None | ✅ **YES** (DB) | ⭐ Low | ⭐ 5K-10K RPS |
| **UUID + Hashing** | ✅ Possible | ❌ None | ⭐ Low | ⭐⭐ Very High |
| **Zookeeper Ranges** | ❌ None | ❌ None | ⭐⭐⭐ High | ⭐⭐⭐ 100K+ RPS |
| **Snowflake ID** | ❌ None | ❌ None | ⭐⭐ Medium | ⭐⭐⭐ Very High |
| **Random + Retry** | ✅ Possible | ❌ None | ⭐⭐ Medium | ⭐ Medium |

**Recommendation:**
- **Small Scale (< 1K RPS)**: Database Sequence
- **Medium Scale (1K-10K RPS)**: Snowflake ID
- **Large Scale (10K+ RPS)**: Zookeeper Ranges or Distributed UUID

---

## Summary

This comprehensive Tiny-URL system design covers:

1. ✅ **Requirement Analysis**: URL length, traffic scale, character sets
2. ✅ **Functional Requirements**: Core features from URL shortening to analytics
3. ✅ **Non-Functional Requirements**: Performance, availability, security, scalability
4. ✅ **URL Generation Strategies**: 5 different approaches with pros/cons
5. ✅ **Scale Estimation & Bottleneck Analysis**: Detailed capacity planning and solutions
6. ✅ **API Design**: RESTful endpoints with request/response examples
7. ✅ **Collision Handling with Zookeeper**: Distributed ID generation with consensus

**Key Takeaways:**
- 7-character Base-62 encoding provides 3.5 trillion possible URLs
- Multi-level caching is essential to handle 57,870 RPS read load
- Database sharding required for write scalability
- Zookeeper provides robust distributed ID generation without collisions
- API design follows REST principles with comprehensive error handling


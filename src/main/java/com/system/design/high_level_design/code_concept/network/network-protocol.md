# Network Protocols for HLD Interviews

This document summarizes the network protocol concepts most commonly asked in HLD interviews, with an emphasis on how they work and when to use them.

## 1) Application Layer Protocols

### What is the application layer?
The application layer is the top layer of the network stack from the perspective of user-facing software. It defines how applications format, send, interpret, and respond to data.

At this layer, the protocol decides:
- request and response structure
- header and metadata format
- authentication and session handling
- message framing or streaming rules
- error handling and retries

### How application-layer protocols work
Typical flow:
1. A client application creates a request in the protocol’s format.
2. The request is handed to lower layers for transport.
3. The server receives the bytes, reconstructs the message, and parses the protocol fields.
4. The server performs application logic.
5. The server returns a protocol-compliant response.

### Common application-layer responsibilities
- resource addressing
- request method semantics
- message serialization
- content negotiation
- authentication / authorization hooks
- state management or stateless interaction

### Interview point
If asked "How does a protocol work?", answer with a short, repeatable structure:

1. **Role:** What problem the protocol solves.
2. **Connection model:** Request/response, persistent bidirectional, or peer-to-peer.
3. **Message flow:** Handshake, message format, processing, and response/ack.
4. **Reliability behavior:** Retries, ordering, idempotency, timeouts, and failure handling.
5. **Best-fit use case:** When to use it and when not to.

#### Example answer template
"`<Protocol>` is used for `<purpose>`. It uses `<connection model>`. A typical flow is `<step 1> -> <step 2> -> <step 3>`. For reliability, it handles `<key behavior>`. I would choose it for `<use case>` but avoid it when `<limitation>` matters."

#### Example 1: HTTP (client-server API)
- **Role:** Standard protocol for web/mobile APIs.
- **Connection model:** Stateless request/response.
- **Flow:** Client sends method + URL + headers + optional body; server validates, processes, and returns status + body.
- **Reliability details:** Client retries safe/idempotent operations carefully; use timeouts, backoff, and idempotency keys for create APIs.
- **Use case:** CRUD APIs, service integration.

#### Example 2: WebSocket (real-time updates)
- **Role:** Low-latency two-way communication.
- **Connection model:** Persistent full-duplex channel after HTTP upgrade.
- **Flow:** Handshake -> upgrade -> both client and server push messages anytime.
- **Reliability details:** Handle reconnect, heartbeat/ping, and message ordering/duplication rules at app level.
- **Use case:** Chat, notifications, live dashboards.

#### Example 3: WebRTC (peer-to-peer media)
- **Role:** Real-time peer media/data exchange.
- **Connection model:** Peer-to-peer (with signaling + ICE setup).
- **Flow:** Offer/answer signaling -> ICE candidate exchange -> direct media path (or TURN relay fallback).
- **Reliability details:** NAT/firewall traversal, adaptive bitrate, fallback to relay when direct path fails.
- **Use case:** Video calls, voice calls, real-time collaboration media.

#### Common interview mistakes to avoid
- Explaining only definitions without message flow.
- Ignoring failure behavior (timeouts, retries, reconnects).
- Not stating tradeoffs or best-fit scenario.
- Mixing protocol purpose (for example, SMTP send vs IMAP/POP read).

---

## 2) Client-Server Protocols

Client-server protocols are designed around a requestor and a provider. The client initiates communication, and the server listens and responds.

### 2.1 HTTP

#### What is HTTP?
HTTP is the dominant web application protocol for request/response communication.

#### How it works
- Client sends a request with a method, URL, headers, and optional body.
- Server processes the request and sends a status code, headers, and optional body.
- HTTP is stateless by design; state is typically added using cookies, tokens, or server-side sessions.

#### Common methods
- `GET` read resource
- `POST` create or submit data
- `PUT` replace a resource
- `PATCH` partial update
- `DELETE` remove a resource

#### Important interview concepts
- **Status codes and intent:**
  - `200 OK` for successful read/update responses
  - `201 Created` when a new resource is created (usually with location/resource id)
  - `400/401/403/404` for client-side issues (bad input, auth, permission, missing resource)
  - `409 Conflict` for state conflicts (for example duplicate create intent)
  - `429 Too Many Requests` for throttling
  - `5xx` for server/dependency failures
- **Headers and why they matter:**
  - `Authorization` carries identity token/credentials
  - `Content-Type` declares request body format (for example JSON)
  - `Accept` states preferred response format
  - `Cache-Control` controls freshness/caching behavior
  - `Idempotency-Key` can protect create APIs from duplicate retries
- **Idempotency by method:**
  - `GET`, `PUT`, `DELETE` are intended to be idempotent at API semantic level
  - `POST` is not naturally idempotent, so add key-based dedup logic when retries are expected
- **Version/protocol evolution:**
  - HTTP/1.1 uses text framing and often multiple connections
  - HTTP/2 multiplexes streams over one connection
  - HTTP/3 runs over QUIC/UDP with better behavior under packet loss

**Mini example (what to say in interview):**
"For `POST /orders`, I return `201` on first success, store result by `Idempotency-Key`, and return cached response on retry to avoid duplicate orders."

#### When to use
Use HTTP for most web APIs, browser-to-server communication, and standard service APIs.

### 2.2 FTP

#### What is FTP?
FTP is a file transfer protocol used for moving files between a client and a server.

#### How it works
- A control connection manages commands and responses.
- A separate data connection is used for file transfer.
- FTP can operate in active or passive mode depending on who opens the data connection.

#### Important interview concepts
- **Control vs data channels:**
  - Control channel handles commands such as login, list, upload, download
  - Data channel carries actual file bytes
  - This split is the reason FTP behaves differently from single-channel HTTP uploads
- **Active vs passive mode:**
  - Active mode: server initiates data connection back to client
  - Passive mode: client initiates both control and data connections
  - Passive is more firewall/NAT-friendly and most common today
- **Security posture:**
  - Plain FTP is unencrypted (credentials/data visible in transit)
  - In practice, prefer SFTP/FTPS or HTTPS-based upload pipelines
- **Operational concerns:**
  - Resume/retry strategy for large file transfers
  - Throughput limits and transfer scheduling for batch windows

**Mini example:**
"For nightly partner file exchange behind strict NAT, I would use passive mode and encrypted transfer, with checksum validation after upload."

#### When to use
FTP is usually seen in legacy or specialized file-transfer environments. In modern systems, SFTP or HTTPS-based upload flows are often preferred.

### 2.3 SMTP

#### What is SMTP?
SMTP is the protocol used to send email from clients to mail servers and between mail servers.

#### How it works
- A mail client or sending server connects to an SMTP server.
- It issues commands to identify sender, recipients, and message content.
- The server accepts, queues, and forwards the email toward the recipient domain.

#### Important interview concepts
- **Send pipeline:**
  - Client submits mail to outbound SMTP server
  - Server relays mail to recipient domain's SMTP server
  - Receiving server queues and delivers to mailbox subsystem
- **Reliability behavior:**
  - SMTP servers queue and retry transient failures
  - Permanent failures generate bounce responses
  - Retries/backoff are critical for internet-scale mail delivery
- **Separation of concerns:**
  - SMTP is only for sending/relay
  - Mail retrieval is handled by IMAP/POP3
- **Abuse controls you should mention:**
  - SPF, DKIM, DMARC concepts in modern mail ecosystems
  - rate limits and reputation checks on outbound gateways

**Mini example:**
"If email provider is temporarily unavailable, the SMTP relay queues and retries instead of dropping messages immediately."

#### When to use
SMTP is the standard protocol for outbound email delivery.

### 2.4 WebSocket

#### What is WebSocket?
WebSocket is a protocol that upgrades an HTTP connection into a persistent, full-duplex channel.

#### How it works
1. Client starts with an HTTP handshake.
2. Server agrees to upgrade the connection.
3. After upgrade, both sides can send messages independently over the same open connection.

#### Why it is useful
- low-latency bidirectional communication
- avoids repeated HTTP polling
- suitable for real-time updates

#### Common use cases
- chat systems
- live dashboards
- collaborative editing
- multiplayer state updates
- real-time notifications

#### Interview concept
WebSocket is not request/response in the traditional sense; it is persistent and bidirectional after the handshake.

---

## 3) Peer-to-Peer Protocols

Peer-to-peer systems allow endpoints to communicate more directly, often with both sides acting as producers and consumers of media or data.

### 3.1 WebRTC

#### What is WebRTC?
WebRTC is a peer-to-peer communication framework used for real-time audio, video, and data exchange between browsers or applications.

#### How it works
WebRTC usually includes:
- **Signaling:** exchange of metadata needed to establish the session
- **NAT traversal:** techniques such as STUN and TURN to connect peers through routers and firewalls
- **Peer connection:** direct media/data channel between endpoints once negotiated

#### Typical connection flow
1. One peer creates an offer.
2. The other peer responds with an answer.
3. Both peers exchange ICE candidates.
4. A connection path is selected.
5. Audio/video/data flows directly peer-to-peer when possible.

#### Important interview concepts
- signaling is not standardized by WebRTC; apps usually implement it themselves using HTTP/WebSocket
- STUN helps discover public-facing addresses for direct connectivity attempts
- TURN relays traffic when direct peer-to-peer connection fails (higher cost, more latency)
- ICE tries multiple candidates and selects the best working path
- WebRTC supports real-time media and data channels
- codec, bitrate adaptation, and network jitter handling affect user call quality

**Mini example:**
"In a video-calling app, I use WebSocket for signaling, STUN for direct path discovery, and TURN fallback for restrictive corporate networks."

#### When to use
Use WebRTC for video calls, voice calls, and low-latency peer communication where direct connection is beneficial.

#### Tradeoffs
- reduces server bandwidth for media relay when direct P2P succeeds
- NAT/firewall traversal adds complexity
- quality depends on network conditions and fallback relay paths

---

## 4) Transport Layer Protocols

The transport layer is responsible for end-to-end process communication between applications running on hosts.

### What transport layer protocols solve
- multiplexing using ports (many app flows on one host)
- segmentation and reassembly
- reliability and ordered delivery (when required)
- flow and congestion handling
- latency vs reliability tradeoff

### 4.1 TCP

#### What is TCP?
TCP is a connection-oriented, reliable, ordered byte-stream protocol.

#### How it works
1. 3-way handshake establishes connection.
2. Data is sent in segments with sequence numbers.
3. Receiver sends ACKs.
4. Sender retransmits missing segments.
5. Connection is closed gracefully.

#### Important interview concepts
- reliability via ACK + retransmission
- ordered delivery guarantees
- flow control (receiver window)
- congestion control (slow start, congestion avoidance)
- head-of-line blocking at stream level

**Mini example:**
"I use TCP for payment/order APIs where correctness and ordered reliable delivery matter more than ultra-low latency."

### 4.2 UDP

#### What is UDP?
UDP is a connectionless datagram protocol with minimal overhead.

#### How it works
- sender transmits independent datagrams
- no built-in ACK/retransmit/ordering
- application adds reliability only if needed

#### Important interview concepts
- very low latency and low protocol overhead
- packets can be lost, duplicated, or reordered
- ideal when timeliness is more valuable than perfect delivery
- common for real-time voice/video, gaming telemetry, DNS

**Mini example:**
"For live game position updates, UDP is preferred because old packets are less useful than fresh state."

### 4.3 QUIC (transport over UDP)

#### What is QUIC?
QUIC is a modern transport protocol built on UDP that provides reliability, security (TLS), and multiplexed streams with faster connection setup.

#### Important interview concepts
- reduced handshake latency
- stream multiplexing without TCP-style head-of-line across streams
- improved behavior on mobile network changes
- foundation for HTTP/3

**Mini example:**
"For mobile-first APIs with variable networks, HTTP/3 over QUIC can reduce latency and improve resilience during network transitions."

### 4.4 SCTP (optional mention in interviews)

#### What is SCTP?
SCTP is a message-oriented transport protocol supporting multi-streaming and multi-homing.

#### Why mention it
- relevant for telecom/specialized domains
- demonstrates awareness beyond TCP/UDP when interviewer asks alternatives

---

## 5) Network Layer Protocols

The network layer handles host-to-host addressing and routing across networks.

### What network layer protocols solve
- logical addressing of hosts
- routing packets across multiple hops
- fragmentation/routing decisions
- diagnostic and control signaling

### 5.1 IP (Internet Protocol)

#### What is IP?
IP provides addressing and packet delivery across interconnected networks on a best-effort basis.

#### Important interview concepts
- IP is connectionless and best-effort (no delivery guarantee by itself)
- reliability is handled by upper layers like TCP/application logic
- routers forward packets based on destination IP and routing tables

### 5.2 IPv4

#### Key details
- 32-bit addressing
- widespread legacy compatibility
- limited address space (drives NAT usage)

### 5.3 IPv6

#### Key details
- 128-bit addressing with massive address space
- simplified header design and better extensibility
- reduces NAT pressure and improves end-to-end addressing model

### 5.4 ICMP

#### What is ICMP?
ICMP carries control/diagnostic messages at network layer scope.

#### Important interview concepts
- used for diagnostics (`ping`, parts of `traceroute`)
- reports network issues (destination unreachable, time exceeded)
- does not carry business application payloads

**Mini example:**
"If users report intermittent latency, I check path behavior with `traceroute` (ICMP-based mechanisms) to identify hop-level issues."

### Interview-level distinction to always mention
- **Network layer (IP):** where to send packets (addressing/routing)
- **Transport layer (TCP/UDP/QUIC):** how apps communicate end-to-end (reliability/latency semantics)
- **Application layer (HTTP/WebSocket/SMTP/WebRTC signaling):** what the message means to the business logic

---

## 6) Quick Comparison

### Application layer protocols
| Protocol    | Communication style                                | Best for                     | Key note                              |
|-------------|----------------------------------------------------|------------------------------|---------------------------------------|
| HTTP        | request/response                                   | web APIs, browser apps       | stateless by default                  |
| FTP         | client-server file transfer                        | legacy file movement         | separate control and data channels    |
| SMTP        | server-to-server / client-to-server email sending  | outbound email               | send mail, not read mailbox           |
| WebSocket   | persistent bidirectional                           | real-time apps               | upgrade from HTTP                     |
| WebRTC      | peer-to-peer media/data                            | calls, live media            | uses signaling + STUN/TURN            |

### Transport and network protocols
| Protocol     | Layer     | Core property                        | Best for                                      |
|--------------|-----------|--------------------------------------|-----------------------------------------------|
| TCP          | Transport | reliable, ordered stream             | APIs, transactions, correctness-first traffic |
| UDP          | Transport | low-overhead datagrams               | real-time media/gaming/telemetry              |
| QUIC         | Transport | reliable streams over UDP + TLS      | HTTP/3, mobile/low-latency web                |
| IP (v4/v6)   | Network   | addressing and routing               | host-to-host packet delivery                  |
| ICMP         | Network   | diagnostics/control                  | ping, traceroute, path troubleshooting        |

---

## 7) Interview Answer Patterns

### If asked “How does HTTP work?”
Say that the client sends a request, the server parses it, the server executes business logic, and returns a response with a status code and headers. Mention statelessness and common methods.

### If asked “Why use WebSocket instead of HTTP polling?”
Say WebSocket keeps a persistent connection and supports low-latency two-way communication, while polling repeatedly opens new requests and increases overhead.

### If asked “Why is WebRTC peer-to-peer?”
Say WebRTC is designed to connect endpoints directly for real-time media when possible, using signaling, STUN, and TURN to establish the path.

### If asked “Why is SMTP different from HTTP?”
Say SMTP is specialized for email transfer and server relay, while HTTP is a general-purpose request/response protocol for web applications.

---

## 8) Practical HLD Takeaway

In architecture interviews, choose the protocol based on the workload:
- **HTTP** for standard API requests
- **WebSocket** for live server push and bidirectional updates
- **SMTP** for outbound email workflows
- **FTP** for legacy file transfer needs
- **WebRTC** for direct real-time peer communication
- **TCP** when ordered reliable delivery is required
- **UDP** when freshness/latency is more important than perfect delivery
- **QUIC/HTTP3** for modern low-latency web transport
- **IP/ICMP** for routing fundamentals and network diagnostics

The main interview goal is to show that you understand not just the protocol name, but also the connection model, reliability tradeoffs, and when each protocol fits the system design.

---

## 9) One-Page Protocol Comparison (Interview Quick Sheet)

### 9.1 Side-by-side comparison
| Protocol | Layer | Communication model | Connection style | Reliability model | Typical latency profile | Best fit | Common limitation |
|---|---|---|---|---|---|---|---|
| HTTP | Application | Client-server request/response | Usually short-lived per request (can reuse via keep-alive) | Depends on transport (typically TCP/QUIC) + app retries | Low to medium | APIs, CRUD, service integration | Polling overhead for real-time push if not using streaming/WebSocket |
| FTP | Application | Client-server file transfer | Control channel + separate data channel | Transfer-oriented, operational retry/checksum patterns | Medium to high (file-size dependent) | Legacy/batch file movement | Plain FTP is insecure; NAT/firewall handling can be tricky |
| SMTP | Application | Client-to-server and server-to-server relay | Session-based command exchange | Queue + retry + bounce semantics | Medium (store-and-forward) | Outbound email delivery | Not for mailbox retrieval (use IMAP/POP3) |
| WebSocket | Application | Full-duplex client-server messaging | Long-lived persistent connection (after HTTP upgrade) | App-level reconnect, heartbeat, ordering/dup handling | Low | Chat, live dashboards, collaborative apps | Requires connection lifecycle management and scaling strategy |
| WebRTC | Application + media stack | Peer-to-peer media/data (with signaling) | Session-oriented peer connection | ICE/STUN/TURN path selection + adaptive media controls | Very low for direct P2P; higher via relay | Video/voice calls, real-time media | NAT/firewall traversal complexity, TURN relay cost |

### 9.2 Interview-focused differences
| Dimension | HTTP | WebSocket | WebRTC | SMTP | FTP |
|---|---|---|---|---|---|
| Real-time bidirectional | Limited (needs polling/streaming tricks) | Yes | Yes | No | No |
| Message semantics | Request/response | Event/message stream | Media + data channels | Email command/relay | File commands/transfer |
| Stateful vs stateless usage | Mostly stateless API style | Stateful connection | Stateful session | Stateful mail transfer sessions | Stateful transfer sessions |
| Typical reliability strategy | Idempotency + retries + status codes | Reconnect + heartbeat + replay logic | Jitter control + bitrate adapt + relay fallback | Queue/retry/bounce handling | Resume/checksum/retry |
| Security baseline | TLS (`https`) | TLS (`wss`) | DTLS/SRTP in stack + secure signaling | TLS where configured | Prefer SFTP/FTPS over plain FTP |

### 9.3 One-liner answers to memorize
- **HTTP:** "Best for standard APIs where request/response and broad ecosystem support matter most."
- **WebSocket:** "Best for low-latency bidirectional updates without repeated polling overhead."
- **WebRTC:** "Best for direct real-time media between peers, with TURN fallback when direct paths fail."
- **SMTP:** "Best for reliable email sending via store-and-forward relay and retry behavior."
- **FTP:** "Best for legacy file transfer workflows, but modern secure alternatives are usually preferred."

### 9.4 Quick protocol picker
- Need CRUD API and broad compatibility -> **HTTP**
- Need instant server push/chat/live events -> **WebSocket**
- Need audio/video calls -> **WebRTC**
- Need outbound emails -> **SMTP**
- Need legacy partner file exchange -> **FTP** (or preferably SFTP/HTTPS alternatives)


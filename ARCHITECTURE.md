# ARCHITECTURE.md
> Source of truth for project architecture. Read this before writing your first line of code.

---

## Table of Contents
1. [Philosophy and Goals](#1-philosophy-and-goals)
2. [Architectural Pattern](#2-architectural-pattern)
3. [Package Structure](#3-package-structure)
4. [Layers — Detailed Description](#4-layers--detailed-description)
5. [Full Request → Response Flow](#5-full-request--response-flow)
6. [Naming Conventions](#6-naming-conventions)
7. [Example Module — ticket](#7-example-module--ticket)
8. [Testing](#8-testing)
9. [What NOT To Do](#9-what-not-to-do)
10. [FAQ](#10-faq)

---

## 1. Philosophy and Goals

The project uses **Vertical Slice Architecture (VSA) + CQRS (Command Query Responsibility Segregation)**.

### Why not classic layered architecture (controller/service/repository)?

Classic layering groups code by **technical type** — all services together, all repositories together. To understand one feature, you jump across the entire project.

We group code by **use case**. To understand "creating a ticket", you look only at `ticket/application/CreateTicket/`. Everything is there.

### Three Main Goals

1. **One use case = one folder** — `CreateTicket`, `CloseTicket`, `GetTicket` are separate, independent units.
2. **Commands change state, Queries only read** — clear separation between write and read operations.
3. **Simple structure** — no ports, no adapters, no unnecessary abstractions. Repository extends JpaRepository directly.

---

## 2. Architectural Pattern

### Vertical Slice Architecture (VSA)
Each use case (slice) is a self-contained unit in the `application/` folder. A slice contains Command/Query + Handler. Slices do not call each other.

### CQRS
- **Command** — changes system state (create, update, delete). Handler returns the created/modified object or `void`.
- **Query** — read only. Handler returns a DTO (not a domain object).

```
[ HTTP Request ]
       |
       ▼
  [ Controller ]          -- deserializes JSON, calls Handler
       |
       ▼
[ Command / Query ]       -- plain data object, no logic
       |
       ▼
    [ Handler ]           -- orchestrates: fetch → execute → save → return
       |
       ▼
  [ Repository ]          -- extends JpaRepository, talks to DB
```

---

## 3. Package Structure

```
com.helpdesk
│
├── ticket/                          # domain module: tickets
│   ├── domain/
│   │   ├── Ticket.java              # JPA Entity + domain model
│   │   ├── TicketStatus.java        # enum
│   │   └── TicketRepository.java   # extends JpaRepository
│   │
│   ├── application/
│   │   ├── CreateTicket/
│   │   │   ├── CreateTicketCommand.java
│   │   │   └── CreateTicketHandler.java
│   │   ├── CloseTicket/
│   │   │   ├── CloseTicketCommand.java
│   │   │   └── CloseTicketHandler.java
│   │   ├── AssignTicket/
│   │   │   ├── AssignTicketCommand.java
│   │   │   └── AssignTicketHandler.java
│   │   └── GetTicket/
│   │       ├── GetTicketQuery.java
│   │       └── GetTicketHandler.java
│   │
│   └── web/
│       ├── TicketController.java
│       ├── TicketRequest.java       # request DTOs
│       └── TicketResponse.java      # response DTOs
│
├── user/                            # domain module: users / agents
│   └── ...                          # same structure
│
├── comment/                         # domain module: comments on tickets
│   └── ...
│
└── shared/
    └── exception/
        ├── NotFoundException.java
        └── GlobalExceptionHandler.java
```

### Rule: one module = one business domain

`ticket` doesn't dig into `user` internals. If it needs user data, it queries through `UserRepository` or a dedicated DTO.

---

## 4. Layers — Detailed Description

### 4.1 Domain

**What belongs here:** JPA Entity, status enum, Repository interface.

**Rules:**
- Entity is both the JPA model and the domain model — no separate classes (we're keeping it simple)
- Business logic can live as methods on the Entity (e.g., `ticket.close()`)
- Repository just `extends JpaRepository<Ticket, UUID>` — no custom implementations needed for basic operations

**Entity example:**
```java
@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    private UUID assignedAgentId;
    private UUID createdByUserId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Business logic on the entity
    public void close() {
        if (this.status == TicketStatus.CLOSED) {
            throw new IllegalStateException("Ticket is already closed");
        }
        this.status = TicketStatus.CLOSED;
        this.updatedAt = LocalDateTime.now();
    }

    public void assignTo(UUID agentId) {
        this.assignedAgentId = agentId;
        this.status = TicketStatus.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now();
    }
}
```

**Repository example:**
```java
public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    List<Ticket> findByAssignedAgentId(UUID agentId);
    List<Ticket> findByCreatedByUserId(UUID userId);
    List<Ticket> findByStatus(TicketStatus status);
}
```

---

### 4.2 Application

**What belongs here:** Command/Query objects, Handlers (one per use case).

**Rules:**
- One folder = one use case
- Handler orchestrates: fetch data → call domain logic → save → return
- Handler does NOT contain business logic — it delegates to the Entity
- Command/Query is a `record` — only data, no methods
- Handlers are `@Component` or `@Service`

**Command example:**
```java
// application/CreateTicket/CreateTicketCommand.java
public record CreateTicketCommand(
    String title,
    String description,
    UUID createdByUserId
) {}
```

**Handler example:**
```java
// application/CreateTicket/CreateTicketHandler.java
@Component
@RequiredArgsConstructor
public class CreateTicketHandler {

    private final TicketRepository ticketRepository;

    @Transactional
    public UUID handle(CreateTicketCommand cmd) {
        Ticket ticket = new Ticket();
        ticket.setTitle(cmd.title());
        ticket.setDescription(cmd.description());
        ticket.setStatus(TicketStatus.NEW);
        ticket.setCreatedByUserId(cmd.createdByUserId());
        ticket.setCreatedAt(LocalDateTime.now());

        return ticketRepository.save(ticket).getId();
    }
}
```

**Query + Handler example:**
```java
// application/GetTicket/GetTicketQuery.java
public record GetTicketQuery(UUID ticketId) {}

// application/GetTicket/GetTicketHandler.java
@Component
@RequiredArgsConstructor
public class GetTicketHandler {

    private final TicketRepository ticketRepository;

    public TicketResponse handle(GetTicketQuery query) {
        Ticket ticket = ticketRepository.findById(query.ticketId())
            .orElseThrow(() -> new NotFoundException("Ticket not found: " + query.ticketId()));

        return new TicketResponse(
            ticket.getId(),
            ticket.getTitle(),
            ticket.getDescription(),
            ticket.getStatus(),
            ticket.getAssignedAgentId(),
            ticket.getCreatedAt()
        );
    }
}
```

---

### 4.3 Web

**What belongs here:** Controllers, Request DTOs, Response DTOs.

**Rules:**
- One Controller per domain resource
- Controller only: deserialize → map to Command/Query → call Handler → return ResponseEntity
- No business logic in Controller
- Request DTO validated with `@Valid` annotations

**Controller example:**
```java
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final CreateTicketHandler createHandler;
    private final CloseTicketHandler closeHandler;
    private final AssignTicketHandler assignHandler;
    private final GetTicketHandler getHandler;

    @PostMapping
    public ResponseEntity<UUID> create(@RequestBody @Valid CreateTicketRequest req) {
        UUID id = createHandler.handle(req.toCommand());
        return ResponseEntity.status(201).body(id);
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<Void> close(@PathVariable UUID id) {
        closeHandler.handle(new CloseTicketCommand(id));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<Void> assign(@PathVariable UUID id, @RequestBody AssignTicketRequest req) {
        assignHandler.handle(new AssignTicketCommand(id, req.agentId()));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(getHandler.handle(new GetTicketQuery(id)));
    }
}
```

**Request DTO example:**
```java
public record CreateTicketRequest(
    @NotBlank String title,
    @NotBlank String description,
    @NotNull UUID createdByUserId
) {
    public CreateTicketCommand toCommand() {
        return new CreateTicketCommand(title, description, createdByUserId);
    }
}
```

**Response DTO example:**
```java
public record TicketResponse(
    UUID id,
    String title,
    String description,
    TicketStatus status,
    UUID assignedAgentId,
    LocalDateTime createdAt
) {}
```

---

## 5. Full Request → Response Flow

```
POST /api/tickets
        │
        ▼
TicketController                    [web]
  - deserializes JSON to CreateTicketRequest
  - maps Request → CreateTicketCommand
  - calls CreateTicketHandler.handle(command)
        │
        ▼
CreateTicketHandler                 [application]
  - creates new Ticket entity
  - sets initial status = NEW
  - calls ticketRepository.save(ticket)
  - returns saved ticket's UUID
        │
        ▼
TicketRepository                    [domain]
  - Spring Data JPA saves entity to DB
        │
        ▼
TicketController                    [web]
  - returns ResponseEntity with 201 status and UUID
        │
        ▼
HTTP 201 Created + UUID
```

---

## 6. Naming Conventions

| What | Convention | Example |
|---|---|---|
| Entity | `[Name]` | `Ticket` |
| Status enum | `[Name]Status` | `TicketStatus` |
| Command | `[Action][Name]Command` | `CreateTicketCommand` |
| Query | `[Action][Name]Query` | `GetTicketQuery` |
| Handler | `[Action][Name]Handler` | `CreateTicketHandler` |
| Repository | `[Name]Repository` | `TicketRepository` |
| Request DTO | `[Action][Name]Request` | `CreateTicketRequest` |
| Response DTO | `[Name]Response` | `TicketResponse` |
| Controller | `[Name]Controller` | `TicketController` |
| Exception | `[Name]Exception` | `NotFoundException` |

---

## 7. Example Module — ticket

### Full file structure

```
ticket/
├── domain/
│   ├── Ticket.java
│   ├── TicketStatus.java          # enum: NEW, IN_PROGRESS, WAITING, CLOSED
│   └── TicketRepository.java
│
├── application/
│   ├── CreateTicket/
│   │   ├── CreateTicketCommand.java
│   │   └── CreateTicketHandler.java
│   ├── CloseTicket/
│   │   ├── CloseTicketCommand.java
│   │   └── CloseTicketHandler.java
│   ├── AssignTicket/
│   │   ├── AssignTicketCommand.java
│   │   └── AssignTicketHandler.java
│   └── GetTicket/
│       ├── GetTicketQuery.java
│       └── GetTicketHandler.java
│
└── web/
    ├── TicketController.java
    ├── TicketRequest.java
    └── TicketResponse.java
```

---

## 8. Testing

### Strategy

| Layer | Test Type | What You Mock |
|---|---|---|
| Entity (domain logic) | Unit test | nothing — pure Java |
| Handler | Unit test | Repository (Mockito) |
| Controller | WebMvcTest | Handler |
| Repository | Integration test | real DB (H2 or Testcontainers) |

### Testing Entity logic — easiest

```java
class TicketTest {

    @Test
    void should_close_ticket_when_status_is_not_closed() {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.IN_PROGRESS);

        ticket.close();

        assertEquals(TicketStatus.CLOSED, ticket.getStatus());
    }

    @Test
    void should_throw_when_closing_already_closed_ticket() {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.CLOSED);

        assertThrows(IllegalStateException.class, ticket::close);
    }
}
```

No Spring annotations. No database. Clean and fast.

### Testing Handler

```java
@ExtendWith(MockitoExtension.class)
class CreateTicketHandlerTest {

    @Mock
    TicketRepository ticketRepository;

    @InjectMocks
    CreateTicketHandler handler;

    @Test
    void should_create_ticket_with_status_new() {
        var command = new CreateTicketCommand("Title", "Description", UUID.randomUUID());

        when(ticketRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        handler.handle(command);

        verify(ticketRepository).save(argThat(ticket ->
            ticket.getStatus() == TicketStatus.NEW &&
            ticket.getTitle().equals("Title")
        ));
    }
}
```

### Testing Controller

```java
@WebMvcTest(TicketController.class)
class TicketControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean CreateTicketHandler createHandler;

    @Test
    void should_return_201_when_ticket_created() throws Exception {
        UUID ticketId = UUID.randomUUID();
        when(createHandler.handle(any())).thenReturn(ticketId);

        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "Bug in login",
                        "description": "Cannot log in",
                        "createdByUserId": "00000000-0000-0000-0000-000000000001"
                    }
                """))
            .andExpect(status().isCreated());
    }
}
```

---

## 9. What NOT To Do

### Don't put business logic in the Handler

```java
// BAD — business logic in Handler
public void handle(CloseTicketCommand cmd) {
    if (ticket.getStatus() == TicketStatus.CLOSED) {  // NO!
        throw new IllegalStateException("Already closed");
    }
    ticket.setStatus(TicketStatus.CLOSED);
}

// GOOD — Handler delegates to Entity
public void handle(CloseTicketCommand cmd) {
    Ticket ticket = ticketRepository.findById(cmd.ticketId()).orElseThrow(...);
    ticket.close();  // logic lives in the entity
    ticketRepository.save(ticket);
}
```

---

### Don't call one Handler from another Handler

```java
// BAD
@Component
public class CreateTicketHandler(
    private final CloseTicketHandler closeHandler  // NO!
)

// GOOD — use Repository directly or extract shared logic to Entity
```

---

### Don't put logic in the Controller

```java
// BAD
@PostMapping
public ResponseEntity<?> create(@RequestBody CreateTicketRequest req) {
    if (req.title().isBlank()) { ... }   // NO! Use @Valid
    // any other logic here — NO!
}

// GOOD — Controller only maps and delegates
@PostMapping
public ResponseEntity<UUID> create(@RequestBody @Valid CreateTicketRequest req) {
    return ResponseEntity.status(201).body(createHandler.handle(req.toCommand()));
}
```

---

## 10. FAQ

**Q: Where does request validation go (e.g. field not blank)?**
A: Format validation (not-null, min length, regex) — in Request DTO with `@Valid` and `@NotBlank` etc. Business validation (e.g. is the ticket already closed) — in the Entity method.

**Q: What if I have a very simple CRUD with no business logic?**
A: Keep the same structure. Handler will be minimal, Entity will have no extra methods. Don't create exceptions to the rules — someone will add logic later and will know where.

**Q: Where do configuration constants go (e.g. max tickets per agent)?**
A: In `application.yml` as properties, injected via `@Value` or `@ConfigurationProperties` into the Handler.

**Q: Can one Controller have multiple Handlers?**
A: Yes. One Controller per REST resource (`TicketController`) can inject multiple Handlers — one per endpoint/use case.

**Q: Query returns DTO, Command returns what?**
A: Command handler typically returns the ID of the created resource (UUID) or `void`. For updates, `void` is fine — the client can fetch updated state with a Query if needed.

---

*Last updated: 2026*

# Smart Campus API - Backend Infrastructure

## 1. Project Overview

The Smart Campus API is a robust, highly available RESTful web service designed to manage the university's physical infrastructure and IoT telemetry. Built using JAX-RS (Jakarta RESTful Web Services), the system provides a scalable interface for campus facilities managers to monitor Rooms, register hardware Sensors, and track historical environmental Readings (e.g., Temperature, CO2, Occupancy).

The architecture emphasizes deep resource nesting, data consistency through side-effects, and a resilient, "leak-proof" error-handling strategy that prevents internal server details from being exposed to clients.

---

## 2. Build and Run Instructions

To compile and run this API locally using Apache NetBeans:

1. **Clone this GitHub repository** to your local machine.
2. Open **Apache NetBeans** and select **File > Open Project**, then navigate to and select the `smart-campus-api` folder.
3. Ensure you have a local server configured in NetBeans (e.g., **GlassFish** or **Tomcat**).
4. Right-click the project in the Projects pane and select **Clean and Build** to resolve Maven dependencies and package the application.
5. Right-click the project again and select **Run**. NetBeans will automatically deploy the RESTful web service to your server.
6. The API will be accessible at: `http://localhost:8080/smart-campus-api/api/v1`  
   *(Note: Port 8080 is default; adjust if your local server uses a different port).*

---

## 3. Sample cURL Commands

Here are five tested interactions demonstrating the core capabilities of the API:

### 1. Create a New Room
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/rooms \
     -H "Content-Type: application/json" \
     -d '{"id":"LEC-001", "name":"Main Lecture Theatre", "capacity":200}'
```

### 2. Register a New Sensor
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
     -H "Content-Type: application/json" \
     -d '{"id":"OCC-001", "type":"Occupancy", "status":"ACTIVE", "currentValue":0, "roomId":"LIB-301"}'
```

### 3. Filter Sensors by Type
```bash
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/sensors?type=Temperature"
```

### 4. Append a Historical Sensor Reading (Sub-Resource)
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings \
     -H "Content-Type: application/json" \
     -d '{"value":26.5}'
```

### 5. Trigger a "Leak-Proof" 409 Conflict Error
```bash
curl -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-301
```

---

## 4. Conceptual Report

### Part 1: Service Architecture & Setup

**Question:** Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.

**Answer:** By default, JAX-RS resource classes are instantiated **per-request**. A new instance of the class is created for every incoming HTTP request and garbage-collected after the response is sent. Because of this per-request lifecycle, we cannot store state (like our lists of rooms and sensors) as standard instance variables within the resource class itself; they would be wiped out on every call. To prevent data loss, we must decouple the storage mechanism. In our architecture, we managed this by using a separate `InMemoryDataStore` class with `static` variables. Furthermore, because multiple per-request resource instances might attempt to access this shared static memory simultaneously, we utilized thread-safe collections like `ConcurrentHashMap` and `CopyOnWriteArrayList` to prevent race conditions and ensure data integrity.

**Question:** Why is the provision of "Hypermedia" (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

**Answer:** Hypermedia As The Engine Of Application State (HATEOAS) represents the highest maturity level of REST (Richardson Maturity Model Level 3). It makes an API completely self-discoverable. Instead of forcing client developers to rely on static documentation and hardcode URLs into their applications, HATEOAS allows the server to dictate available actions dynamically through embedded links. This heavily benefits client developers by decoupling the client from the server's internal routing logic. If the server changes an endpoint URL in the future, the client will not break, because it navigates the API by reading the provided relation links (e.g., `rel="rooms"`) rather than relying on hardcoded paths.

---

### Part 2: Room Management

**Question:** When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client-side processing.

**Answer:** This represents a classic architectural trade-off between payload size and request volume. Returning only IDs significantly reduces the initial network bandwidth consumed, resulting in a very lightweight JSON payload. However, it negatively impacts client-side processing by introducing the "N+1 Request Problem." If the client needs to display the names and capacities of those rooms, they must make one initial request for the list, plus *N* additional HTTP requests to fetch the details for each specific ID, drastically increasing latency. Returning full objects increases the upfront network bandwidth per request, but it is vastly more efficient for client-side processing. The client acquires all necessary data in a single HTTP request, eliminating round-trip latency.

**Question:** Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

**Answer:** Yes, the `DELETE` operation in this implementation is strictly idempotent. In REST, an operation is idempotent if executing it multiple times leaves the server in the exact same state as executing it once. In our `SensorRoom` class, if a client sends a `DELETE` request for a valid, empty room, the server removes it and returns a `204 No Content` status. If the client mistakenly sends that exact same `DELETE` request again, our logic explicitly checks if the room exists. Finding it already gone, it safely bypasses the deletion logic and returns `204 No Content` again. The server state remains unchanged, and no unexpected server crashes or 500 errors occur.

---

### Part 3: Sensor Operations & Linking

**Question:** We explicitly use the `@Consumes(MediaType.APPLICATION_JSON)` annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as `text/plain` or `application/xml`. How does JAX-RS handle this mismatch?

**Answer:** The `@Consumes` annotation establishes a strict data contract between the client and the server. If a client attempts to send a payload formatted as `text/plain` or `application/xml`, the JAX-RS framework intercepts the request during the routing phase, *before* it even reaches the Java method. Because the framework cannot find a matching method mapped to that specific URI that also consumes the provided `Content-Type`, JAX-RS automatically aborts the request. It then generates and returns an **HTTP 415 Unsupported Media Type** status code to the client. This prevents the underlying JSON parser from crashing while trying to deserialize invalid formats.

**Question:** You implemented this filtering using `@QueryParam`. Contrast this with an alternative design where the type is part of the URL path (e.g., `/api/v1/sensors/type/CO2`). Why is the query parameter approach generally considered superior for filtering and searching collections?

**Answer:** In RESTful design, URL paths should strictly represent the *identity* and *hierarchy* of a physical resource. Query parameters (`?type=CO2`) act as modifiers applied to that collection. The query parameter approach is vastly superior for filtering because it separates resource identity from search criteria. If we used paths like `/sensors/type/CO2`, we falsely imply that "type" is a nested physical resource. Furthermore, query parameters offer massive extensibility. If a client needs to search by multiple criteria, adding `?type=CO2&status=ACTIVE` is seamless. Attempting to do that with path parameters creates brittle, rigid, and unmaintainable URLs.

---

### Part 4: Deep Nesting with Sub-Resources

**Question:** Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., `sensors/{id}/readings/{rid}`) in one massive controller class?

**Answer:** The Sub-Resource Locator pattern enforces the Single Responsibility Principle (SRP) and Separation of Concerns. In a large API, placing all nested operations into a single monolithic controller creates a "God Object" that is incredibly difficult to maintain, test, and read. By using a locator, we create modular, highly cohesive classes. The `SensorResource` only handles sensor metadata, while the `SensorReadingResource` is strictly dedicated to telemetry logic. This encapsulation allows different developers to work on different resource levels simultaneously without merge conflicts, making the API significantly more scalable.

---

### Part 5: Advanced Error Handling, Exception Mapping & Logging

**Question:** Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

**Answer:** HTTP 404 (Not Found) strictly implies that the *Target URI* the client requested does not exist. If a client sends a `POST` request to `/api/v1/sensors`, that endpoint *does* exist, so a 404 is misleading. HTTP 422 (Unprocessable Entity) correctly indicates that the server understands the content type, and the JSON syntax is perfectly valid, but the server cannot process the instructions due to semantic errors—specifically, a business logic failure where a dependent relational ID (like a `roomId`) inside the payload does not exist in the database.

**Question:** From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

**Answer:** Exposing raw stack traces is a critical vulnerability known as "Information Leakage." A Java stack trace acts as a roadmap of the server's internal architecture. An attacker can gather specific reconnaissance data such as: exact framework versions (revealing outdated libraries with known CVE vulnerabilities), internal file system paths, database driver details, and the exact class and line numbers where input validation fails. Attackers use this blueprint to craft targeted injection or exploitation attacks, which is why a "Global Safety Net" is mandatory.

**Question:** Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting `Logger.info()` statements inside every single resource method?

**Answer:** Using JAX-RS Filters enforces the DRY (Don't Repeat Yourself) principle. Manually inserting `Logger.info()` into every method heavily pollutes the business logic with infrastructure code and is highly prone to human error (e.g., forgetting to log a new endpoint). A `ContainerRequestFilter` and `ContainerResponseFilter` act as an interceptor layer. They automatically capture 100% of incoming and outgoing traffic globally, guaranteeing complete API observability from a single, centralized class without modifying the core application code.

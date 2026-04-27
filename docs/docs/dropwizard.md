# Dropwizard Bundle

`mustang-dw-bundle` wraps `MustangEngine` in a [Dropwizard](https://www.dropwizard.io/) `ConfiguredBundle`, registering JAX-RS resources so you can expose Mustang's capabilities over HTTP without writing any glue code.

---

## Dependency

```xml
<dependency>
  <groupId>com.phonepe</groupId>
  <artifactId>mustang-dw-bundle</artifactId>
  <version>3.0.1</version>
</dependency>
```

---

## Registration

Extend `MustangBundle` and implement `getMustangConfig`:

```java
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import com.phonepe.mustang.MustangBundle;
import com.phonepe.mustang.MustangConfig;

public class MyAppConfiguration extends Configuration {
    private MustangConfig mustang;
    public MustangConfig getMustang() { return mustang; }
}

public class MyApp extends Application<MyAppConfiguration> {

    private final MustangBundle<MyAppConfiguration> mustangBundle =
        new MustangBundle<>() {
            @Override
            public MustangConfig getMustangConfig(MyAppConfiguration config) {
                return config.getMustang();
            }
        };

    @Override
    public void initialize(Bootstrap<MyAppConfiguration> bootstrap) {
        bootstrap.addBundle(mustangBundle);
    }

    @Override
    public void run(MyAppConfiguration config, Environment env) {
        // Access the engine for your own use:
        MustangEngine engine = mustangBundle.getMustangEngine();
        // Index your criteria, set up reload schedules, etc.
    }
}
```

**`config.yml`:**

```yaml
mustang:
  serviceName: my-service
```

---

## `MustangConfig` fields

| Field | Type | Required | Description |
|---|---|---|---|
| `serviceName` | `String` | Yes | Logical name of the service; used in metrics/logging |

---

## Authentication

All endpoints require the JAX-RS role `mustang_operator` (constant `MustangBundle.MUSTANG_PERMISSION`). Wire this into your application's authentication/authorization mechanism (e.g., a `@RolesAllowed` filter).

---

## REST API reference

All endpoints:

- Accept `Content-Type: application/json`
- Produce `application/json`
- Return `MustangResponse<T>` wrapper: `{ "success": true, "data": <T> }`
- On error: `{ "success": false, "errorCode": "<CODE>", "message": "..." }`

---

### `POST /mustang/v1/search`

Search the inverted index for all criteria matching an event.

**Request body:**

```json
{
  "indexName": "ads",
  "requestContext": {
    "node": { "country": "IN", "age": 25, "platform": "android" }
  },
  "score": false
}
```

| Field | Type | Required | Description |
|---|---|---|---|
| `indexName` | `String` | Yes | Name of the index to search |
| `requestContext.node` | `JsonNode` | Yes | The event as a JSON object |
| `score` | `boolean` | No | Whether to compute relevance scores (default `false`) |

**Response:**

```json
{
  "success": true,
  "data": ["segment-001", "segment-003"]
}
```

---

### `POST /mustang/v1/scan/index`

Brute-force evaluate all criteria in the index against the event (slow; for validation).

**Request body:**

```json
{
  "indexName": "ads",
  "requestContext": {
    "node": { "country": "IN", "age": 25 }
  }
}
```

**Response:** Same shape as search — `Set<String>` of matching IDs.

---

### `POST /mustang/v1/scan/criteria`

Evaluate a provided list of criteria against the event (no index required).

**Request body:**

```json
{
  "criterias": [ { "form": "DNF", "id": "...", "conjunctions": [...] } ],
  "requestContext": {
    "node": { "country": "IN" }
  }
}
```

**Response:** `List<Criteria>` — the matching criteria objects.

---

### `POST /mustang/v1/debug`

Evaluate a single criteria against an event with per-predicate detail.

**Request body:**

```json
{
  "criteria": { "form": "DNF", "id": "rule-1", "conjunctions": [...] },
  "requestContext": {
    "node": { "country": "IN", "age": 25 }
  }
}
```

**Response:**

```json
{
  "success": true,
  "data": {
    "id": "rule-1",
    "form": "DNF",
    "result": true,
    "compositionDebugResults": [
      {
        "result": true,
        "predicateDebugResults": [
          { "lhs": "$.country", "result": true },
          { "lhs": "$.age",     "result": true }
        ]
      }
    ]
  }
}
```

---

### `POST /mustang/v1/index/export`

Serialize a named index to a JSON string (for backup or migration).

**Request body:**

```json
{ "indexName": "ads" }
```

**Response:** `{ "success": true, "data": "<json-string>" }`

---

### `POST /mustang/v1/index/snapshot`

Return a lightweight diagnostic view of an index's posting list structure.

**Request body:**

```json
{ "indexName": "ads" }
```

**Response:** `{ "success": true, "data": "<snapshot-string>" }`

---

### `POST /mustang/v1/index/ratify`

Trigger or check ratification (anomaly detection) for an index.

**Request body:**

```json
{
  "indexName": "ads",
  "fullFledged": true
}
```

**Response:**

```json
{
  "success": true,
  "data": {
    "status": true,
    "timeTakenMs": 1234,
    "combinations": { "totalCount": 500, "cpCount": 300, "ssCount": 200 },
    "anamolyDetails": []
  }
}
```

---

## Metrics

All resource methods are annotated with `@Timed` and `@ResponseMetered` (Dropwizard Metrics). Metrics are exposed at the standard `/metrics` admin endpoint.

---

## Exception mapping

`MustangExceptionMapper` is registered automatically. It maps `MustangException` error codes to HTTP status codes:

| `ErrorCode` | HTTP status |
|---|---|
| `INDEX_NOT_FOUND` | `404 Not Found` |
| `INDEX_GROUP_EXISTS` | `409 Conflict` |
| All others | `500 Internal Server Error` |

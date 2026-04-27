# Error Codes

All Mustang runtime errors are thrown as `MustangException` with a typed `ErrorCode`.

```java
import com.phonepe.mustang.exception.MustangException;
import com.phonepe.mustang.exception.ErrorCode;

try {
    engine.add("my-index", criteria);
} catch (MustangException e) {
    ErrorCode code = e.getErrorCode();
    // handle
}
```

---

## Error Code Reference

| `ErrorCode` | When thrown | Resolution |
|---|---|---|
| `CRITERIA_NOT_FOUND` | `delete()` called for a criteria ID that is not indexed | Verify the criteria ID exists in the index before deleting |
| `DUPLICATE_CRITERIA` | `add()` called with a criteria ID that is already indexed | Use `update()` to modify an existing criteria |
| `INDEX_NOT_FOUND` | Operation on an index group that does not exist | Ensure the index has been populated with `add()` before searching or replacing |
| `INVALID_CRITERIA` | Criteria is structurally invalid (e.g. empty conjunctions/disjunctions) | Validate that all required fields are present and non-empty |
| `SERIALIZATION_ERROR` | `exportIndexGroup()` / `importIndexGroup()` serialization failure | Check the ObjectMapper configuration; ensure all types are registered |
| `EVALUATION_ERROR` | Error during predicate evaluation (e.g. attribute path not found, type mismatch) | Verify that attribute paths (`lhs`) are valid JSONPath expressions and the event schema matches |

---

## MustangException Fields

```java
public class MustangException extends RuntimeException {
    ErrorCode errorCode;   // typed error code
    String message;        // human-readable description
    Throwable cause;       // underlying cause (may be null)
}
```

---

## Dropwizard Bundle Error Responses

When using `mustang-dw-bundle`, all exceptions are mapped to HTTP responses by `MustangExceptionMapper`:

| `ErrorCode` | HTTP Status |
|---|---|
| `CRITERIA_NOT_FOUND` | `404 Not Found` |
| `DUPLICATE_CRITERIA` | `409 Conflict` |
| `INDEX_NOT_FOUND` | `404 Not Found` |
| `INVALID_CRITERIA` | `400 Bad Request` |
| `SERIALIZATION_ERROR` | `500 Internal Server Error` |
| `EVALUATION_ERROR` | `422 Unprocessable Entity` |

Response body follows `MustangResponse` format:

```json
{
  "status": "ERROR",
  "errorCode": "DUPLICATE_CRITERIA",
  "message": "Criteria with id 'rule-001' already exists in index 'my-index'"
}
```

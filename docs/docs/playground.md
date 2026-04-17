# Playground

Try out Mustang's criteria evaluation directly in your browser. Write a **Criteria** (DNF or CNF) and an **Event** (the JSON object representing an incoming event), then click **Evaluate** to see whether the criteria matches — along with a per-predicate debug trace.

!!! note "Client-side only"
    This playground runs entirely in JavaScript. It mirrors Mustang's Java evaluation logic for the most common predicate and detail types. No data leaves your browser.

---

## Supported features

| Feature | Supported |
|---|---|
| `DNFCriteria` (conjunctions of predicates) | ✅ |
| `CNFCriteria` (disjunctions of predicates) | ✅ |
| `IncludedPredicate` | ✅ |
| `ExcludedPredicate` | ✅ |
| `EqualityDetail` | ✅ |
| `INDetail` (set membership) | ✅ |
| `RangeDetail` (numeric) | ✅ |
| `ExistenceDetail` | ✅ |
| `NonExistenceDetail` | ✅ |
| `RegexDetail` | ✅ |
| `VersioningDetail` | ✅ |
| `ContainsDetail` | ✅ |
| `AnyDetail` | ✅ |
| `UNFCriteria` (arbitrary nesting) | ❌ (use `evaluate()` API in Java) |
| `PreOperation` transforms | ❌ (apply transforms to your event JSON manually) |
| JSONPath `lhs` expressions | ❌ (use simple dot-path keys e.g. `$.age`) |

---

<div id="mustang-playground">

<style>
  #mustang-playground {
    font-family: inherit;
  }
  .pg-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1.2rem;
    margin-bottom: 1rem;
  }
  @media (max-width: 768px) {
    .pg-grid { grid-template-columns: 1fr; }
  }
  .pg-label {
    font-weight: 600;
    margin-bottom: 0.4rem;
    display: block;
    font-size: 0.9rem;
  }
  .pg-textarea {
    width: 100%;
    height: 280px;
    font-family: 'Fira Code', 'Courier New', monospace;
    font-size: 0.82rem;
    padding: 0.7rem;
    border: 1px solid var(--md-default-fg-color--lighter, #ccc);
    border-radius: 4px;
    background: var(--md-code-bg-color, #f5f5f5);
    color: var(--md-code-fg-color, #333);
    resize: vertical;
    box-sizing: border-box;
  }
  .pg-btn-row {
    display: flex;
    gap: 0.7rem;
    margin-bottom: 1rem;
    flex-wrap: wrap;
  }
  .pg-btn {
    padding: 0.5rem 1.4rem;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 0.9rem;
    font-weight: 600;
    transition: opacity 0.15s;
  }
  .pg-btn:hover { opacity: 0.85; }
  .pg-btn-primary {
    background: var(--md-primary-fg-color, #5c6bc0);
    color: #fff;
  }
  .pg-btn-secondary {
    background: var(--md-default-fg-color--lighter, #ddd);
    color: var(--md-default-fg-color, #333);
  }
  #pg-result {
    border-radius: 4px;
    padding: 1rem 1.2rem;
    font-family: 'Fira Code', 'Courier New', monospace;
    font-size: 0.85rem;
    white-space: pre-wrap;
    word-break: break-word;
    min-height: 3rem;
    border: 1px solid var(--md-default-fg-color--lighter, #ccc);
    background: var(--md-code-bg-color, #f5f5f5);
    color: var(--md-code-fg-color, #333);
  }
  #pg-result.match    { border-left: 5px solid #4caf50; }
  #pg-result.no-match { border-left: 5px solid #f44336; }
  #pg-result.error    { border-left: 5px solid #ff9800; color: #b26a00; }
  .pg-examples {
    margin-bottom: 1rem;
    font-size: 0.85rem;
  }
  .pg-examples select {
    padding: 0.3rem 0.5rem;
    border-radius: 4px;
    border: 1px solid var(--md-default-fg-color--lighter, #ccc);
    background: var(--md-code-bg-color, #f5f5f5);
    color: var(--md-code-fg-color, #333);
    font-size: 0.85rem;
    margin-left: 0.4rem;
    cursor: pointer;
  }
</style>

<div class="pg-examples">
  <label for="pg-example-select"><strong>Load example:</strong>
    <select id="pg-example-select" onchange="pgLoadExample(this.value)">
      <option value="">— choose —</option>
      <option value="dnf_ad">DNF — Ad targeting (age + city)</option>
      <option value="cnf_pubsub">CNF — Pub/Sub routing (topic OR priority)</option>
      <option value="dnf_version">DNF — Version gate (semver)</option>
      <option value="dnf_range">DNF — Numeric range</option>
      <option value="dnf_regex">DNF — Regex match</option>
      <option value="dnf_existence">DNF — Existence / NonExistence</option>
      <option value="dnf_excluded">DNF — Excluded predicate (negative match)</option>
    </select>
  </label>
</div>

<div class="pg-grid">
  <div>
    <span class="pg-label">Criteria JSON</span>
    <textarea class="pg-textarea" id="pg-criteria" spellcheck="false" placeholder='{ "@type": "DNFCriteria", ... }'></textarea>
  </div>
  <div>
    <span class="pg-label">Event JSON (RequestContext payload)</span>
    <textarea class="pg-textarea" id="pg-event" spellcheck="false" placeholder='{ "age": 28, "city": "Bangalore" }'></textarea>
  </div>
</div>

<div class="pg-btn-row">
  <button class="pg-btn pg-btn-primary" onclick="pgEvaluate()">Evaluate</button>
  <button class="pg-btn pg-btn-secondary" onclick="pgClear()">Clear</button>
</div>

<div id="pg-result">Enter criteria and event JSON above, then click Evaluate.</div>

</div>

<script>
/* =========================================================
   Mustang Playground — browser-side evaluator
   Supports: DNFCriteria, CNFCriteria, Included/ExcludedPredicate,
   EqualityDetail, INDetail, RangeDetail, ExistenceDetail,
   NonExistenceDetail, RegexDetail, VersioningDetail,
   ContainsDetail, AnyDetail.
   JSONPath: only simple "$.key" and "$.a.b.c" paths.
   ========================================================= */

// ---- JSONPath resolver (simple dot-path only) ----
function pgResolvePath(event, lhs) {
  // Strip leading "$." or "$["
  let path = lhs.replace(/^\$\./, '').replace(/^\$\[['"]?/, '').replace(/['"]?\]$/, '');
  let parts = path.split('.');
  let cur = event;
  for (let p of parts) {
    if (cur == null || typeof cur !== 'object') return undefined;
    cur = cur[p];
  }
  return cur;
}

// ---- Version comparison helper ----
function pgCmpVersion(a, b) {
  // Returns -1, 0, 1
  let pa = String(a).split('.').map(Number);
  let pb = String(b).split('.').map(Number);
  let len = Math.max(pa.length, pb.length);
  for (let i = 0; i < len; i++) {
    let na = pa[i] || 0, nb = pb[i] || 0;
    if (na < nb) return -1;
    if (na > nb) return 1;
  }
  return 0;
}

// ---- Detail evaluators ----
function pgEvalDetail(detail, fieldValue, trace) {
  let t = (detail['@type'] || detail.type || '').replace(/.*\./, '');
  switch (t) {
    case 'EqualityDetail': {
      let v = detail.value;
      let ok = String(fieldValue) === String(v);
      trace.push({ detail: t, expected: v, actual: fieldValue, result: ok });
      return ok;
    }
    case 'INDetail': {
      let values = detail.values || [];
      let ok = values.map(String).includes(String(fieldValue));
      trace.push({ detail: t, values, actual: fieldValue, result: ok });
      return ok;
    }
    case 'RangeDetail': {
      let fv = Number(fieldValue);
      let lo = detail.lowerBound != null ? Number(detail.lowerBound) : null;
      let hi = detail.upperBound != null ? Number(detail.upperBound) : null;
      let loI = detail.includeLowerBound !== false;
      let hiI = detail.includeUpperBound !== false;
      let ok = true;
      if (lo != null) ok = ok && (loI ? fv >= lo : fv > lo);
      if (hi != null) ok = ok && (hiI ? fv <= hi : fv < hi);
      trace.push({ detail: t, lowerBound: lo, upperBound: hi, includeLowerBound: loI, includeUpperBound: hiI, actual: fv, result: ok });
      return ok;
    }
    case 'ExistenceDetail': {
      let ok = fieldValue !== undefined && fieldValue !== null;
      trace.push({ detail: t, actual: fieldValue, result: ok });
      return ok;
    }
    case 'NonExistenceDetail': {
      let ok = fieldValue === undefined || fieldValue === null;
      trace.push({ detail: t, actual: fieldValue, result: ok });
      return ok;
    }
    case 'RegexDetail': {
      let pattern = detail.value || detail.pattern || '';
      let ok = new RegExp(pattern).test(String(fieldValue));
      trace.push({ detail: t, pattern, actual: fieldValue, result: ok });
      return ok;
    }
    case 'VersioningDetail': {
      let cmp = pgCmpVersion(fieldValue, detail.value);
      let ok = false;
      let excludeBase = detail.excludeBase === true;
      switch ((detail.type || detail.versionType || '').toUpperCase()) {
        case 'ABOVE': ok = excludeBase ? cmp > 0 : cmp >= 0; break;
        case 'BELOW': ok = excludeBase ? cmp < 0 : cmp <= 0; break;
        case 'EQUAL': ok = cmp === 0; break;
        default: ok = false;
      }
      trace.push({ detail: t, versionType: detail.type || detail.versionType, value: detail.value, excludeBase, actual: fieldValue, result: ok });
      return ok;
    }
    case 'ContainsDetail': {
      let v = String(detail.value || '');
      let ok = String(fieldValue).includes(v);
      trace.push({ detail: t, substring: v, actual: fieldValue, result: ok });
      return ok;
    }
    case 'AnyDetail': {
      trace.push({ detail: t, actual: fieldValue, result: true });
      return true;
    }
    default:
      trace.push({ detail: t, error: 'Unsupported detail type in playground', result: false });
      return false;
  }
}

// ---- Predicate evaluator ----
function pgEvalPredicate(pred, event, trace) {
  let t = (pred['@type'] || pred.type || '').replace(/.*\./, '');
  let lhs = pred.lhs || '';
  let detail = pred.detail || {};
  let fieldValue = pgResolvePath(event, lhs);
  let pTrace = [];
  let detailResult = pgEvalDetail(detail, fieldValue, pTrace);
  let result = (t === 'ExcludedPredicate') ? !detailResult : detailResult;
  trace.push({ predicate: t, lhs, fieldValue, detail: pTrace[0] || {}, result });
  return result;
}

// ---- Criteria evaluator ----
function pgEvalCriteria(criteria, event) {
  let t = (criteria['@type'] || criteria.type || '').replace(/.*\./, '');
  let trace = [];
  let result;

  if (t === 'DNFCriteria') {
    // OR of conjunctions
    let conjunctions = criteria.conjunctions || [];
    let conjResults = [];
    for (let conj of conjunctions) {
      let preds = conj.predicates || [];
      let predTraces = [];
      let allMatch = preds.every(p => pgEvalPredicate(p, event, predTraces));
      conjResults.push({ conjunction: predTraces, result: allMatch });
      if (allMatch) { result = true; }
    }
    if (result === undefined) result = false;
    trace = conjResults;
  } else if (t === 'CNFCriteria') {
    // AND of disjunctions
    let disjunctions = criteria.disjunctions || [];
    let disjResults = [];
    result = true;
    for (let disj of disjunctions) {
      let preds = disj.predicates || [];
      let predTraces = [];
      let anyMatch = preds.some(p => pgEvalPredicate(p, event, predTraces));
      disjResults.push({ disjunction: predTraces, result: anyMatch });
      if (!anyMatch) result = false;
    }
    trace = disjResults;
  } else {
    throw new Error(`Unsupported criteria type: "${t}". Playground supports DNFCriteria and CNFCriteria only.`);
  }

  return { result, type: t, trace };
}

// ---- Main evaluate handler ----
function pgEvaluate() {
  let out = document.getElementById('pg-result');
  let criteriaText = document.getElementById('pg-criteria').value.trim();
  let eventText = document.getElementById('pg-event').value.trim();

  out.className = '';
  if (!criteriaText || !eventText) {
    out.className = 'error';
    out.textContent = 'Please fill in both Criteria JSON and Event JSON.';
    return;
  }

  let criteria, event;
  try { criteria = JSON.parse(criteriaText); } catch(e) {
    out.className = 'error';
    out.textContent = 'Invalid Criteria JSON:\n' + e.message;
    return;
  }
  try { event = JSON.parse(eventText); } catch(e) {
    out.className = 'error';
    out.textContent = 'Invalid Event JSON:\n' + e.message;
    return;
  }

  let evalResult;
  try { evalResult = pgEvalCriteria(criteria, event); }
  catch(e) {
    out.className = 'error';
    out.textContent = 'Evaluation error:\n' + e.message;
    return;
  }

  out.className = evalResult.result ? 'match' : 'no-match';
  let lines = [];
  lines.push('Result: ' + (evalResult.result ? '✓ MATCH' : '✗ NO MATCH'));
  lines.push('Type:   ' + evalResult.type);
  lines.push('');

  if (evalResult.type === 'DNFCriteria') {
    evalResult.trace.forEach((c, ci) => {
      lines.push(`Conjunction[${ci}]: ${c.result ? '✓' : '✗'}`);
      c.conjunction.forEach((p, pi) => {
        lines.push(`  Predicate[${pi}]: ${p.predicate} on "${p.lhs}" = ${JSON.stringify(p.fieldValue)}`);
        lines.push(`    Detail: ${p.detail.detail || ''} → ${p.detail.result ? '✓' : '✗'}`);
        let d = Object.assign({}, p.detail);
        delete d.detail; delete d.result;
        let extras = Object.entries(d).map(([k,v]) => `${k}=${JSON.stringify(v)}`).join(', ');
        if (extras) lines.push(`    ${extras}`);
        lines.push(`    Predicate result: ${p.result ? '✓ true' : '✗ false'}`);
      });
    });
  } else if (evalResult.type === 'CNFCriteria') {
    evalResult.trace.forEach((d, di) => {
      lines.push(`Disjunction[${di}]: ${d.result ? '✓' : '✗'}`);
      d.disjunction.forEach((p, pi) => {
        lines.push(`  Predicate[${pi}]: ${p.predicate} on "${p.lhs}" = ${JSON.stringify(p.fieldValue)}`);
        lines.push(`    Detail: ${p.detail.detail || ''} → ${p.detail.result ? '✓' : '✗'}`);
        let det = Object.assign({}, p.detail);
        delete det.detail; delete det.result;
        let extras = Object.entries(det).map(([k,v]) => `${k}=${JSON.stringify(v)}`).join(', ');
        if (extras) lines.push(`    ${extras}`);
        lines.push(`    Predicate result: ${p.result ? '✓ true' : '✗ false'}`);
      });
    });
  }

  out.textContent = lines.join('\n');
}

function pgClear() {
  document.getElementById('pg-criteria').value = '';
  document.getElementById('pg-event').value = '';
  let out = document.getElementById('pg-result');
  out.className = '';
  out.textContent = 'Enter criteria and event JSON above, then click Evaluate.';
  document.getElementById('pg-example-select').value = '';
}

// ---- Example library ----
const PG_EXAMPLES = {
  dnf_ad: {
    criteria: JSON.stringify({
      "@type": "DNFCriteria",
      "id": "campaign-001",
      "conjunctions": [
        {
          "predicates": [
            {
              "@type": "IncludedPredicate",
              "lhs": "$.age",
              "detail": { "@type": "RangeDetail", "lowerBound": 18, "upperBound": 35, "includeLowerBound": true, "includeUpperBound": true }
            },
            {
              "@type": "IncludedPredicate",
              "lhs": "$.city",
              "detail": { "@type": "INDetail", "values": ["Bangalore", "Mumbai", "Delhi"] }
            }
          ]
        }
      ]
    }, null, 2),
    event: JSON.stringify({ "age": 28, "city": "Bangalore" }, null, 2)
  },
  cnf_pubsub: {
    criteria: JSON.stringify({
      "@type": "CNFCriteria",
      "id": "sub-payment-alerts",
      "disjunctions": [
        {
          "predicates": [
            { "@type": "IncludedPredicate", "lhs": "$.topic", "detail": { "@type": "EqualityDetail", "value": "payments" } },
            { "@type": "IncludedPredicate", "lhs": "$.topic", "detail": { "@type": "EqualityDetail", "value": "refunds" } }
          ]
        },
        {
          "predicates": [
            { "@type": "IncludedPredicate", "lhs": "$.priority", "detail": { "@type": "INDetail", "values": ["HIGH", "CRITICAL"] } }
          ]
        }
      ]
    }, null, 2),
    event: JSON.stringify({ "topic": "orders", "priority": "HIGH" }, null, 2)
  },
  dnf_version: {
    criteria: JSON.stringify({
      "@type": "DNFCriteria",
      "id": "feature-dark-mode",
      "conjunctions": [
        {
          "predicates": [
            {
              "@type": "IncludedPredicate",
              "lhs": "$.appVersion",
              "detail": { "@type": "VersioningDetail", "type": "ABOVE", "value": "3.5.0", "excludeBase": false }
            }
          ]
        }
      ]
    }, null, 2),
    event: JSON.stringify({ "appVersion": "3.6.1", "platform": "android" }, null, 2)
  },
  dnf_range: {
    criteria: JSON.stringify({
      "@type": "DNFCriteria",
      "id": "high-value-txn",
      "conjunctions": [
        {
          "predicates": [
            {
              "@type": "IncludedPredicate",
              "lhs": "$.amount",
              "detail": { "@type": "RangeDetail", "lowerBound": 10000, "upperBound": 100000, "includeLowerBound": true, "includeUpperBound": false }
            }
          ]
        }
      ]
    }, null, 2),
    event: JSON.stringify({ "amount": 55000, "currency": "INR" }, null, 2)
  },
  dnf_regex: {
    criteria: JSON.stringify({
      "@type": "DNFCriteria",
      "id": "email-domain-check",
      "conjunctions": [
        {
          "predicates": [
            {
              "@type": "IncludedPredicate",
              "lhs": "$.email",
              "detail": { "@type": "RegexDetail", "value": "^[^@]+@phonepe\\.com$" }
            }
          ]
        }
      ]
    }, null, 2),
    event: JSON.stringify({ "email": "alice@phonepe.com" }, null, 2)
  },
  dnf_existence: {
    criteria: JSON.stringify({
      "@type": "DNFCriteria",
      "id": "kyc-required",
      "conjunctions": [
        {
          "predicates": [
            { "@type": "IncludedPredicate", "lhs": "$.userId", "detail": { "@type": "ExistenceDetail" } },
            { "@type": "IncludedPredicate", "lhs": "$.kycStatus", "detail": { "@type": "NonExistenceDetail" } }
          ]
        }
      ]
    }, null, 2),
    event: JSON.stringify({ "userId": "u123" }, null, 2)
  },
  dnf_excluded: {
    criteria: JSON.stringify({
      "@type": "DNFCriteria",
      "id": "non-fraud-txn",
      "conjunctions": [
        {
          "predicates": [
            {
              "@type": "ExcludedPredicate",
              "lhs": "$.riskLabel",
              "detail": { "@type": "INDetail", "values": ["FRAUD", "SUSPECTED_FRAUD", "BLOCKED"] }
            }
          ]
        }
      ]
    }, null, 2),
    event: JSON.stringify({ "riskLabel": "LOW_RISK", "amount": 500 }, null, 2)
  }
};

function pgLoadExample(key) {
  if (!key) return;
  let ex = PG_EXAMPLES[key];
  if (!ex) return;
  document.getElementById('pg-criteria').value = ex.criteria;
  document.getElementById('pg-event').value = ex.event;
  let out = document.getElementById('pg-result');
  out.className = '';
  out.textContent = 'Example loaded. Click Evaluate to run.';
}
</script>

---

## Criteria JSON reference

### DNFCriteria

```json
{
  "@type": "DNFCriteria",
  "id": "my-criteria",
  "conjunctions": [
    {
      "predicates": [
        {
          "@type": "IncludedPredicate",
          "lhs": "$.fieldName",
          "detail": { "@type": "EqualityDetail", "value": "expected" }
        }
      ]
    }
  ]
}
```

The criteria matches if **any** conjunction matches (OR of AND-groups).

### CNFCriteria

```json
{
  "@type": "CNFCriteria",
  "id": "my-criteria",
  "disjunctions": [
    {
      "predicates": [
        {
          "@type": "IncludedPredicate",
          "lhs": "$.fieldName",
          "detail": { "@type": "INDetail", "values": ["a", "b"] }
        }
      ]
    }
  ]
}
```

The criteria matches if **every** disjunction matches (AND of OR-groups).

### Detail types quick reference

| `@type` | Required fields | Notes |
|---|---|---|
| `EqualityDetail` | `value` | String comparison |
| `INDetail` | `values` (array) | Value must be in list |
| `RangeDetail` | `lowerBound`, `upperBound` | Optional `includeLowerBound` / `includeUpperBound` (default `true`) |
| `ExistenceDetail` | — | Field must be present and non-null |
| `NonExistenceDetail` | — | Field must be absent or null |
| `RegexDetail` | `value` (pattern) | Full Java-regex syntax |
| `VersioningDetail` | `type` (`ABOVE`/`BELOW`/`EQUAL`), `value` | Optional `excludeBase` (default `false`) |
| `ContainsDetail` | `value` (substring) | Case-sensitive |
| `AnyDetail` | — | Always matches |

### `lhs` field paths

In the playground, use simple dot-notation JSONPath expressions:

```
$.age          → event["age"]
$.user.city    → event["user"]["city"]
$.order.amount → event["order"]["amount"]
```

Full JSONPath expressions (filters, wildcards, array indexing) require the Java engine.

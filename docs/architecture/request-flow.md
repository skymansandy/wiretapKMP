# Request Flow

## HTTP Request Lifecycle

```mermaid
sequenceDiagram
    participant App as App (HTTP Client)
    participant Plugin as Plugin (Ktor / OkHttp)
    participant Rules as FindMatchingRuleUseCase
    participant Orchestrator as WiretapOrchestrator
    participant DB as WiretapDatabase
    participant Logger as WiretapLogger

    App->>Plugin: Outgoing HTTP Request

    Plugin->>Rules: findMatchingRule(url, method, headers, body)
    Rules-->>Plugin: WiretapRule | null

    Plugin->>Orchestrator: logRequest(entry)
    Orchestrator->>DB: INSERT (in-progress)
    Orchestrator->>Logger: logHttp(entry)
    Orchestrator-->>Plugin: logEntryId

    alt Mock Rule Matched
        Plugin->>Plugin: Create mock response
        Plugin->>Orchestrator: updateEntry(mockResponse)
        Orchestrator->>DB: UPDATE (source=Mock)
        Plugin-->>App: Mock Response
    else Throttle Rule Matched
        Plugin->>Plugin: delay(delayMs)
        Plugin->>Plugin: Proceed to network
        Plugin->>Orchestrator: updateEntry(response)
        Orchestrator->>DB: UPDATE (source=Throttle)
        Plugin-->>App: Real Response (delayed)
    else No Rule
        Plugin->>Plugin: Proceed to network
        Plugin->>Orchestrator: updateEntry(response)
        Orchestrator->>DB: UPDATE (source=Network)
        Plugin-->>App: Real Response
    end
```

## WebSocket Lifecycle

```mermaid
sequenceDiagram
    participant App as App
    participant Plugin as Plugin
    participant Orchestrator as WiretapOrchestrator
    participant DB as WiretapDatabase

    App->>Plugin: Open WebSocket

    Plugin->>Orchestrator: openSocketConnection(entry)
    Orchestrator->>DB: INSERT (status=Open)
    Orchestrator-->>Plugin: socketId

    loop Messages
        App->>Plugin: send(frame)
        Plugin->>Orchestrator: logSocketMessage(Sent)
        Orchestrator->>DB: INSERT message

        Plugin-->>App: receive(frame)
        Plugin->>Orchestrator: logSocketMessage(Received)
        Orchestrator->>DB: INSERT message
    end

    alt Graceful Close
        Plugin->>Orchestrator: updateSocketConnection(Closed)
        Orchestrator->>DB: UPDATE status=Closed
    else Error
        Plugin->>Orchestrator: updateSocketConnection(Failed)
        Orchestrator->>DB: UPDATE status=Failed
    end
```

## Response Status Codes

| Code | Meaning |
|------|---------|
| `-2` | Request in progress (no response yet) |
| `-1` | Request cancelled |
| `0` | Network error (exception thrown) |
| `1xx–5xx` | Standard HTTP status codes |

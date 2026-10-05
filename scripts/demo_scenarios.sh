#!/bin/bash
# AI-ROUTE VIVA - 5 Primary Demonstration Scenarios (Linux / macOS / Git Bash)
set -e

BASE_URL="http://localhost:8082"

echo "================================================================================"
echo "   AI-ROUTE VIVA: Demonstration of 5 Primary Scenarios & Baseline Comparison"
echo "   Problem Statement: PS1 (Integrated with PS5 Viva Proctoring)"
echo "================================================================================"

echo ""
echo "[STEP 0] Checking Service Health & Real Host Resources..."
curl -s "$BASE_URL/health" | jq .
curl -s "$BASE_URL/resources" | jq .

curl -s -X POST "$BASE_URL/cache/clear" > /dev/null

echo ""
echo "================================================================================"
echo "SCENARIO 1: Normal resources -> Higher-capability local execution when feasible"
echo "================================================================================"
curl -s -X POST "$BASE_URL/simulate/condition" \
  -H "Content-Type: application/json" \
  -d '{"mode":"CUSTOM","ramUsage":40.0,"ramAvailableMb":4900,"cpuUsage":25.0}' > /dev/null

curl -s -X POST "$BASE_URL/route" \
  -H "Content-Type: application/json" \
  -d '{"requestId":"DEMO-SCENARIO-1","workloadType":"ANSWER_EVALUATION","complexity":"HIGH","priority":"HIGH"}' | jq .

echo ""
echo "================================================================================"
echo "SCENARIO 2: High resource pressure -> Lightweight local selected"
echo "================================================================================"
curl -s -X POST "$BASE_URL/simulate/condition" \
  -H "Content-Type: application/json" \
  -d '{"mode":"HIGH_RAM"}' > /dev/null

curl -s -X POST "$BASE_URL/route" \
  -H "Content-Type: application/json" \
  -d '{"requestId":"DEMO-SCENARIO-2","workloadType":"SPEECH_TO_TEXT","complexity":"MEDIUM","priority":"HIGH"}' | jq .

echo ""
echo "================================================================================"
echo "SCENARIO 3: Network failure -> Simulated cloud path becomes infeasible"
echo "================================================================================"
curl -s -X POST "$BASE_URL/simulate/condition" \
  -H "Content-Type: application/json" \
  -d '{"mode":"OFFLINE_NET"}' > /dev/null

curl -s -X POST "$BASE_URL/route" \
  -H "Content-Type: application/json" \
  -d '{"requestId":"DEMO-SCENARIO-3","workloadType":"OBJECT_DETECTION","complexity":"MEDIUM","priority":"MEDIUM"}' | jq .

echo ""
echo "================================================================================"
echo "SCENARIO 4: Repeated request -> Deterministic cache hit"
echo "================================================================================"
curl -s -X POST "$BASE_URL/simulate/condition" \
  -H "Content-Type: application/json" \
  -d '{"mode":"RESET"}' > /dev/null

PAYLOAD='{"requestId":"REQ-CACHE-RUN1","workloadType":"ANSWER_EVALUATION","complexity":"HIGH","priority":"HIGH","payload":{"question":"Explain ACID","answer":"Atomicity Consistency Isolation Durability"},"execute":true}'

echo "Request 1 (Cold):"
curl -s -X POST "$BASE_URL/route" -H "Content-Type: application/json" -d "$PAYLOAD" | jq '{selectedPath, cacheHit, latencyMs}'

echo "Request 2 (Warm / Cache Hit):"
PAYLOAD_2='{"requestId":"REQ-CACHE-RUN2","workloadType":"ANSWER_EVALUATION","complexity":"HIGH","priority":"HIGH","payload":{"question":"Explain ACID","answer":"Atomicity Consistency Isolation Durability"},"execute":true}'
curl -s -X POST "$BASE_URL/route" -H "Content-Type: application/json" -d "$PAYLOAD_2" | jq '{selectedPath, cacheHit, latencyMs, reasoning}'

echo ""
echo "================================================================================"
echo "SCENARIO 5: Resource recovery -> Higher-capability feasible again"
echo "================================================================================"
echo "Applying stress:"
curl -s -X POST "$BASE_URL/simulate/condition" -H "Content-Type: application/json" -d '{"mode":"HIGH_RAM"}' > /dev/null
curl -s -X POST "$BASE_URL/route" -H "Content-Type: application/json" \
  -d '{"requestId":"DEMO-STRESS","workloadType":"FOLLOW_UP_GENERATION","complexity":"HIGH","priority":"MEDIUM"}' | jq '{selectedPath}'

echo "Recovering resources:"
curl -s -X POST "$BASE_URL/simulate/condition" -H "Content-Type: application/json" \
  -d '{"mode":"CUSTOM","ramUsage":38.0,"ramAvailableMb":5100,"cpuUsage":22.0}' > /dev/null
curl -s -X POST "$BASE_URL/route" -H "Content-Type: application/json" \
  -d '{"requestId":"DEMO-RECOVER","workloadType":"FOLLOW_UP_GENERATION","complexity":"HIGH","priority":"MEDIUM"}' | jq '{selectedPath, reasoning}'

echo ""
echo "================================================================================"
echo "BASELINE BENCHMARK: AI-ROUTE Dynamic vs FIXED_EXECUTION"
echo "================================================================================"
curl -s -X POST "$BASE_URL/simulate/condition" -H "Content-Type: application/json" -d '{"mode":"HIGH_RAM"}' > /dev/null
curl -s -X POST "$BASE_URL/benchmark/compare?fixedPath=HIGHER_CAPABILITY_LOCAL" \
  -H "Content-Type: application/json" \
  -d '{"workloadType":"ANSWER_EVALUATION","complexity":"HIGH","priority":"HIGH"}' | jq .

echo ""
echo "================================================================================"
echo "ROUTING METRICS"
echo "================================================================================"
curl -s "$BASE_URL/metrics" | jq .

# Reset condition
curl -s -X POST "$BASE_URL/simulate/condition" -H "Content-Type: application/json" -d '{"mode":"RESET"}' > /dev/null
echo ""
echo "Demo finished successfully!"

# AI-ROUTE VIVA — Full End-to-End Control Plane Demo Script
# HackWithAMYPO 2026 | PS1 + PS5
# Demonstrates: React -> Spring Boot -> Go Router -> Persistence -> Reports -> WebSocket

Write-Host "================================================================================" -ForegroundColor Cyan
Write-Host "   AI-ROUTE VIVA — Spring Boot Control Plane Full Lifecycle Demo" -ForegroundColor Yellow
Write-Host "   HackWithAMYPO 2026 (PS1: AI Request Router + PS5: Viva Proctoring)" -ForegroundColor Cyan
Write-Host "================================================================================" -ForegroundColor Cyan

$SpringUrl = "http://localhost:8080"
$GoUrl = "http://localhost:8082"

# 1. Check Go Router Health
Write-Host "`n[STEP 1] Checking Go Router (PS1 Engine) on $GoUrl..." -ForegroundColor Green
try {
    $goHealth = Invoke-RestMethod -Uri "$GoUrl/health" -Method Get
    Write-Host "  -> Go Router status: $($goHealth.status)" -ForegroundColor White
} catch {
    Write-Host "  [!] Go Router not responding at $GoUrl. Starting binary..." -ForegroundColor Yellow
    Start-Process -FilePath ".\bin\router.exe" -WindowStyle Hidden
    Start-Sleep -Seconds 2
}

# 2. Check Spring Boot Health
Write-Host "`n[STEP 2] Verifying Spring Boot Control Plane on $SpringUrl..." -ForegroundColor Green
try {
    $apiDocs = Invoke-RestMethod -Uri "$SpringUrl/v3/api-docs" -Method Get
    Write-Host "  -> Spring Boot Control Plane is UP! OpenAPI Title: $($apiDocs.info.title)" -ForegroundColor White
} catch {
    Write-Host "  [NOTE] Spring Boot is not running yet. Run 'mvn spring-boot:run' inside the 'backend' folder." -ForegroundColor Yellow
    exit 0
}

# 3. Create Student
Write-Host "`n[STEP 3] Registering Student STU-DEMO-01..." -ForegroundColor Green
$studentBody = @{
    studentId = "STU-DEMO-01"
    name = "Aarav Sharma"
} | ConvertTo-Json

$student = Invoke-RestMethod -Uri "$SpringUrl/api/students" -Method Post -Body $studentBody -ContentType "application/json"
Write-Host "  -> Student Registered: $($student.studentId) - $($student.name)" -ForegroundColor White

# 4. Create Viva Session
Write-Host "`n[STEP 4] Creating Viva Session on 'Computer Architecture'..." -ForegroundColor Green
$sessionBody = @{
    studentId = "STU-DEMO-01"
    topic = "Computer Architecture and Cache Coherence"
    questions = @(
        @{
            questionText = "Explain the difference between write-through and write-back caches."
            orderNumber = 1
            questionType = "STANDARD"
        },
        @{
            questionText = "What is cache coherence and how does MESI protocol solve it?"
            orderNumber = 2
            questionType = "DEEP_DIVE"
        }
    )
} | ConvertTo-Json

$session = Invoke-RestMethod -Uri "$SpringUrl/api/viva/sessions" -Method Post -Body $sessionBody -ContentType "application/json"
$sessionId = $session.sessionId
Write-Host "  -> Session Created: $sessionId (Status: $($session.status))" -ForegroundColor White
Write-Host "  -> Questions Loaded: $($session.questions.Count)" -ForegroundColor White

# 5. Start Session
Write-Host "`n[STEP 5] Starting Viva Session ($sessionId)..." -ForegroundColor Green
$started = Invoke-RestMethod -Uri "$SpringUrl/api/viva/sessions/$sessionId/start" -Method Post
Write-Host "  -> Session Status: $($started.status) at $($started.startedAt)" -ForegroundColor White

# 6. Record Identity Verification Proctoring Event
Write-Host "`n[STEP 6] Proctoring Check: Identity Verification Workload..." -ForegroundColor Green
$procEventBody = @{
    sessionId = $sessionId
    eventType = "IDENTITY_VERIFIED"
    severity = "INFO"
    description = "Candidate facial biometric verified against student database"
} | ConvertTo-Json

$procEvent = Invoke-RestMethod -Uri "$SpringUrl/api/proctoring/events" -Method Post -Body $procEventBody -ContentType "application/json"
Write-Host "  -> Event Logged: $($procEvent.eventId) | $($procEvent.eventType) ($($procEvent.severity))" -ForegroundColor White

# 7. Submit Answer 1
Write-Host "`n[STEP 7] Submitting Answer for Question 1..." -ForegroundColor Green
$q1Id = $session.questions[0].questionId
$ans1Body = @{
    questionId = $q1Id
    transcript = "In a write-through cache, every write to the cache is immediately written to main memory. In a write-back cache, writes are performed only in cache, and the dirty block is written to main memory only when evicted."
    durationMs = 8200
} | ConvertTo-Json

$ans1 = Invoke-RestMethod -Uri "$SpringUrl/api/viva/sessions/$sessionId/answers" -Method Post -Body $ans1Body -ContentType "application/json"
Write-Host "  -> Answer Submitted: $($ans1.answerId)" -ForegroundColor White
Write-Host "  -> AI Semantic Score: $($ans1.semanticScore)" -ForegroundColor Yellow
Write-Host "  -> Router Path: $($ans1.evaluationRoutingPath)" -ForegroundColor Cyan
Write-Host "  -> Evaluator Feedback: $($ans1.evaluationFeedback)" -ForegroundColor White

# 8. Check Real-Time Go Router Decision History for Session
Write-Host "`n[STEP 8] Querying Routing Dashboard for Session ($sessionId)..." -ForegroundColor Green
$routes = Invoke-RestMethod -Uri "$SpringUrl/api/routing/$sessionId" -Method Get
Write-Host "  -> Total AI Workloads Routed: $($routes.Count)" -ForegroundColor White
foreach ($r in $routes) {
    Write-Host "     * Workload: $($r.workloadType) | Complexity: $($r.complexity) | Path: $($r.selectedPath) | Cost: `$$($r.estimatedCost) | Reason: $($r.reasoning)" -ForegroundColor DarkCyan
}

# 9. Query Current System Resources (via Spring Boot -> Go)
Write-Host "`n[STEP 9] Fetching Current Resource Telemetry (Spring Boot -> Go)..." -ForegroundColor Green
$res = Invoke-RestMethod -Uri "$SpringUrl/api/resources/current" -Method Get
Write-Host "  -> CPU Usage: $($res.cpuUsage)% | RAM Usage: $($res.ramUsage)% | Available RAM: $($res.ramAvailableMb) MB | Network: $($res.networkStatus)" -ForegroundColor White

# 10. Complete Session
Write-Host "`n[STEP 10] Completing Viva Session ($sessionId)..." -ForegroundColor Green
$completed = Invoke-RestMethod -Uri "$SpringUrl/api/viva/sessions/$sessionId/complete" -Method Post
Write-Host "  -> Session Status: $($completed.status) at $($completed.endedAt)" -ForegroundColor White

# 11. Generate Consolidated Final Report
Write-Host "`n[STEP 11] Generating Consolidated Final Report (Section 34)..." -ForegroundColor Green
$report = Invoke-RestMethod -Uri "$SpringUrl/api/reports/$sessionId" -Method Get
Write-Host "  -> Report Generated for Session: $($report.sessionId)" -ForegroundColor Yellow
Write-Host "     --- VIVA RESULTS ---" -ForegroundColor White
Write-Host "     Questions: $($report.vivaResults.questionCount), Answered: $($report.vivaResults.answeredCount)" -ForegroundColor White
Write-Host "     Proctoring Events: $($report.vivaResults.proctoringEventCount)" -ForegroundColor White
Write-Host "     --- AI ROUTING INFRASTRUCTURE RESULTS ---" -ForegroundColor White
Write-Host "     Total AI Workloads Routed : $($report.aiRoutingResults.totalAIRequests)" -ForegroundColor Cyan
Write-Host "     Local Executions          : $($report.aiRoutingResults.localExecutions)" -ForegroundColor Cyan
Write-Host "     Cache Hits                : $($report.aiRoutingResults.cacheHits)" -ForegroundColor Cyan
Write-Host "     Cloud Executions          : $($report.aiRoutingResults.simulatedCloudExecutions)" -ForegroundColor Cyan
Write-Host "     Fallback Executions       : $($report.aiRoutingResults.fallbackExecutions)" -ForegroundColor Cyan
Write-Host "     Avg Latency               : $($report.aiRoutingResults.averageLatency) ms" -ForegroundColor Cyan
Write-Host "     Total Estimated Cost      : `$$($report.aiRoutingResults.totalEstimatedCost)" -ForegroundColor Cyan
Write-Host "     Peak CPU / RAM            : $($report.aiRoutingResults.peakCPU)% / $($report.aiRoutingResults.peakRAM)%" -ForegroundColor Cyan

Write-Host "`n================================================================================" -ForegroundColor Cyan
Write-Host "   DEMO COMPLETE — Spring Boot Control Plane Orchestration Fully Verified!" -ForegroundColor Green
Write-Host "================================================================================" -ForegroundColor Cyan

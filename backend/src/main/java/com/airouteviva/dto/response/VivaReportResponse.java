package com.airouteviva.dto.response;

import com.airouteviva.entity.enums.ExecutionPath;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class VivaReportResponse {

    private String reportId;
    private String sessionId;
    private Instant generatedAt;

    // Part 1: VIVA ACADEMIC & PROCTORING RESULTS
    private VivaDomainSection vivaResults;

    // Part 2: AI ROUTING & INFRASTRUCTURE RESULTS (PS1 Core)
    private AIRoutingSection aiRoutingResults;

    public VivaReportResponse() {
    }

    public static class VivaDomainSection {
        private StudentResponse student;
        private String topic;
        private String sessionStatus;
        private Instant startedAt;
        private Instant endedAt;
        private Double overallScore;
        private String evaluatorSummary;
        private int questionCount;
        private int answeredCount;
        private List<VivaAnswerResponse> answers;
        private List<ProctoringEventResponse> proctoringEvents;
        private Map<String, Object> communicationMetrics;

        public VivaDomainSection() {}

        public StudentResponse getStudent() { return student; }
        public void setStudent(StudentResponse student) { this.student = student; }
        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
        public String getSessionStatus() { return sessionStatus; }
        public void setSessionStatus(String sessionStatus) { this.sessionStatus = sessionStatus; }
        public Instant getStartedAt() { return startedAt; }
        public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
        public Instant getEndedAt() { return endedAt; }
        public void setEndedAt(Instant endedAt) { this.endedAt = endedAt; }
        public Double getOverallScore() { return overallScore; }
        public void setOverallScore(Double overallScore) { this.overallScore = overallScore; }
        public String getEvaluatorSummary() { return evaluatorSummary; }
        public void setEvaluatorSummary(String evaluatorSummary) { this.evaluatorSummary = evaluatorSummary; }
        public int getQuestionCount() { return questionCount; }
        public void setQuestionCount(int questionCount) { this.questionCount = questionCount; }
        public int getAnsweredCount() { return answeredCount; }
        public void setAnsweredCount(int answeredCount) { this.answeredCount = answeredCount; }
        public List<VivaAnswerResponse> getAnswers() { return answers; }
        public void setAnswers(List<VivaAnswerResponse> answers) { this.answers = answers; }
        public List<ProctoringEventResponse> getProctoringEvents() { return proctoringEvents; }
        public void setProctoringEvents(List<ProctoringEventResponse> proctoringEvents) { this.proctoringEvents = proctoringEvents; }
        public Map<String, Object> getCommunicationMetrics() { return communicationMetrics; }
        public void setCommunicationMetrics(Map<String, Object> communicationMetrics) { this.communicationMetrics = communicationMetrics; }
    }

    public static class AIRoutingSection {
        private long totalAIRequests;
        private long localExecutions;
        private long cacheHits;
        private long fallbackExecutions;
        private long simulatedCloudExecutions;
        private double averageLatencyMs;
        private double totalEstimatedCost;
        private double peakCPU;
        private double peakRAM;
        private long proctoringEventCount;
        private List<DecisionTimelineEntry> timeline;

        public AIRoutingSection() {}

        public long getTotalAIRequests() { return totalAIRequests; }
        public void setTotalAIRequests(long totalAIRequests) { this.totalAIRequests = totalAIRequests; }
        public long getLocalExecutions() { return localExecutions; }
        public void setLocalExecutions(long localExecutions) { this.localExecutions = localExecutions; }
        public long getCacheHits() { return cacheHits; }
        public void setCacheHits(long cacheHits) { this.cacheHits = cacheHits; }
        public long getFallbackExecutions() { return fallbackExecutions; }
        public void setFallbackExecutions(long fallbackExecutions) { this.fallbackExecutions = fallbackExecutions; }
        public long getSimulatedCloudExecutions() { return simulatedCloudExecutions; }
        public void setSimulatedCloudExecutions(long simulatedCloudExecutions) { this.simulatedCloudExecutions = simulatedCloudExecutions; }
        public double getAverageLatencyMs() { return averageLatencyMs; }
        public void setAverageLatencyMs(double averageLatencyMs) { this.averageLatencyMs = averageLatencyMs; }
        public double getTotalEstimatedCost() { return totalEstimatedCost; }
        public void setTotalEstimatedCost(double totalEstimatedCost) { this.totalEstimatedCost = totalEstimatedCost; }
        public double getPeakCPU() { return peakCPU; }
        public void setPeakCPU(double peakCPU) { this.peakCPU = peakCPU; }
        public double getPeakRAM() { return peakRAM; }
        public void setPeakRAM(double peakRAM) { this.peakRAM = peakRAM; }
        public long getProctoringEventCount() { return proctoringEventCount; }
        public void setProctoringEventCount(long proctoringEventCount) { this.proctoringEventCount = proctoringEventCount; }
        public List<DecisionTimelineEntry> getTimeline() { return timeline; }
        public void setTimeline(List<DecisionTimelineEntry> timeline) { this.timeline = timeline; }
    }

    public static class DecisionTimelineEntry {
        private String requestId;
        private String workloadType;
        private String complexity;
        private String priority;
        private ExecutionPath selectedPath;
        private String reasoning;
        private Double latencyMs;
        private Double estimatedCost;
        private Boolean cacheHit;
        private Double cpuUsage;
        private Double ramUsage;
        private String networkStatus;
        private Instant timestamp;

        public DecisionTimelineEntry() {}

        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }
        public String getWorkloadType() { return workloadType; }
        public void setWorkloadType(String workloadType) { this.workloadType = workloadType; }
        public String getComplexity() { return complexity; }
        public void setComplexity(String complexity) { this.complexity = complexity; }
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        public ExecutionPath getSelectedPath() { return selectedPath; }
        public void setSelectedPath(ExecutionPath selectedPath) { this.selectedPath = selectedPath; }
        public String getReasoning() { return reasoning; }
        public void setReasoning(String reasoning) { this.reasoning = reasoning; }
        public Double getLatencyMs() { return latencyMs; }
        public void setLatencyMs(Double latencyMs) { this.latencyMs = latencyMs; }
        public Double getEstimatedCost() { return estimatedCost; }
        public void setEstimatedCost(Double estimatedCost) { this.estimatedCost = estimatedCost; }
        public Boolean getCacheHit() { return cacheHit; }
        public void setCacheHit(Boolean cacheHit) { this.cacheHit = cacheHit; }
        public Double getCpuUsage() { return cpuUsage; }
        public void setCpuUsage(Double cpuUsage) { this.cpuUsage = cpuUsage; }
        public Double getRamUsage() { return ramUsage; }
        public void setRamUsage(Double ramUsage) { this.ramUsage = ramUsage; }
        public String getNetworkStatus() { return networkStatus; }
        public void setNetworkStatus(String networkStatus) { this.networkStatus = networkStatus; }
        public Instant getTimestamp() { return timestamp; }
        public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    }

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public Instant getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Instant generatedAt) { this.generatedAt = generatedAt; }
    public VivaDomainSection getVivaResults() { return vivaResults; }
    public void setVivaResults(VivaDomainSection vivaResults) { this.vivaResults = vivaResults; }
    public AIRoutingSection getAiRoutingResults() { return aiRoutingResults; }
    public void setAiRoutingResults(AIRoutingSection aiRoutingResults) { this.aiRoutingResults = aiRoutingResults; }
}

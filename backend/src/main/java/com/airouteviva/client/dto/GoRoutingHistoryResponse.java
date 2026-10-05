package com.airouteviva.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoRoutingHistoryResponse {
    private Integer total;
    private List<GoRouteResponse> history;

    public GoRoutingHistoryResponse() {
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<GoRouteResponse> getHistory() {
        return history;
    }

    public void setHistory(List<GoRouteResponse> history) {
        this.history = history;
    }
}

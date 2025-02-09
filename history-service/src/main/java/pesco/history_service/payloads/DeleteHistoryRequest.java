package pesco.history_service.payloads;

import java.util.List;

public class DeleteHistoryRequest {
    private List<Long> ids;

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }
}   

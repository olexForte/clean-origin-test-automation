package automation.entities;

import automation.datasources.JSONConverter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Persistent data object (Global item. For example: from Google sheets)
 */
public class PersistentDataObject extends BaseEntity{
    String id;
    LocalDateTime date;
    String status;
    String type;
    Map<String, String> data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public String getDataAsJSONString() {
        return JSONConverter.objectToJson(data);
    }

    public void setDataFromJSON(String json){
        data = JSONConverter.toHashMapFromJsonString(json);
    }
}

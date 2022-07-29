package io.exercise.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DashboardUser extends BaseModel {

    String name;
    String description;
    String parentId;
    List<String> readACL;
    List<String> writeACL;
}

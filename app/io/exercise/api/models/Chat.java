package io.exercise.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Chat extends BaseModel{
    String roomId;
    String name;
    List<String> readACL;
    List<String> writeACL;

}

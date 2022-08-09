package io.exercise.api.models;

import java.util.List;

public class Chat extends BaseModel{
    String roomId;
    List<String> readACL;
    List<String> writeACL;

}

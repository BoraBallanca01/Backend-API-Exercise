package io.exercise.api.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.exercise.api.models.content.ContentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonIgnore;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Dashboard extends BaseModel {
    @NotNull
    String name;
    @NotNull
    String description;

    String parentId;
    List<String> readACL;
    List<String> writeACL;

    List<ContentType> content=new ArrayList<>();



}

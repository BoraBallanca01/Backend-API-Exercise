package io.exercise.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Roles extends BaseModel  {

    String name;

//    public Roles (String id, String name) {
//        super.setId(new ObjectId(id));
//        this.name = name;
//    }
}
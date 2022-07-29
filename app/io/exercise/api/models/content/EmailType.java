package io.exercise.api.models.content;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import java.util.List;

@BsonDiscriminator(key="type",value = "EMAIL")
@Data
public class EmailType extends ContentType {
    @Override
    public Type getType(){
        return Type.EMAIL;
    }
    String text;
    String subject;
    String email;
    List<String> realACL;
    List<String> writeACL;
}

package io.exercise.api.models.content;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import java.util.List;

@BsonDiscriminator(key="type", value = "TEXT")
@Data
public class TextType extends ContentType {
    @Override
    public Type getType(){
        return Type.TEXT;
    }
    String text;

}

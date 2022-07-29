package io.exercise.api.models.content;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import java.util.List;

@BsonDiscriminator(key = "type", value = "LINE")
@Data
public class LineType extends ContentType {
    @Override
    public Type getType() {
        return Type.LINE;
    }

    List<TypeData> data;
}

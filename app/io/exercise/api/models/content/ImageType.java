package io.exercise.api.models.content;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;


@BsonDiscriminator(key="type", value="IMAGE")
@Data
public class ImageType extends ContentType {
    @Override
    public Type getType(){
        return Type.IMAGE;
    }
    String url;
}

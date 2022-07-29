package io.exercise.api.models.content;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.exercise.api.models.BaseModel;
import io.exercise.api.mongo.serializers.ObjectIdDeSerializer;
import io.exercise.api.mongo.serializers.ObjectIdStringSerializer;
import lombok.Data;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;

import java.util.List;

@Data
@ToString
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = EmailType.class, name = "EMAIL"),
        @JsonSubTypes.Type(value = TextType.class, name = "TEXT"),
        @JsonSubTypes.Type(value = ImageType.class, name = "IMAGE"),
        @JsonSubTypes.Type(value = LineType.class, name = "LINE")
})
@BsonDiscriminator(key = "type", value = "NONE")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContentType extends BaseModel {
    @BsonId
    @JsonSerialize(using = ObjectIdStringSerializer.class)
    @JsonDeserialize(using = ObjectIdDeSerializer.class)
    Type type = Type.NONE;
    ObjectId dashboardId;

}

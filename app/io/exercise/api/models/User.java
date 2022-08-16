package io.exercise.api.models;


import lombok.Data;
import lombok.*;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.hibernate.validator.constraints.Length;
//import org.hibernate.validator.constraints.URL;
import java.net.URL;
import javax.validation.constraints.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User extends BaseModel {


    @NotNull(message = "Username cannot be null")
    String username;

    @NotNull(message = "Password cannot be null")
    String password;

    List<String> roles;
}
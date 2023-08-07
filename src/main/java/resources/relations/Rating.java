package resources.relations;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import resources.enums.RatingType;

@Getter @Setter @AllArgsConstructor
public class Rating
{
    String owningUser;
    String userListingID;
    RatingType type;
    Float rating;
    LocalDateTime timestamp;
}

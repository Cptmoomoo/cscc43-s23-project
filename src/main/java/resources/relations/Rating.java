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

    @Override
    public String toString()
    {
        return String.format("[%s: %s]\n (%s) %s\nRating: %.1f/5.0", type.toString(), userListingID, owningUser, timestamp.toString(), rating);
    }
}

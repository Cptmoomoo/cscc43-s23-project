package resources.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import resources.enums.CommentType;

@Getter @Setter @AllArgsConstructor
public class Comment
{
    private String commentID;
    private String commentOwner;
    private String userListingID;
    private CommentType type;
    private String text;
    private LocalDateTime timestamp;

    public Comment(String commentOwner, String userListingID, CommentType type, String text)
    {
        this(UUID.randomUUID().toString(), commentOwner, userListingID, type, text, LocalDateTime.now());
    }

    @Override
    public boolean equals(Object right)
    {
        if (!(right instanceof Comment))
            return false;

        return commentID == ((Comment) right).getCommentID();
    }

    @Override
    public String toString()
    {
        return String.format("[%s: %s]\n (%s) %s\n%s", type.toString(), userListingID, commentOwner, timestamp.toString(), text);
    }
}

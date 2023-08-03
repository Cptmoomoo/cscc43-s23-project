package resources.entities;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class Comment
{
    private String commentID;
    private String text;
    private LocalDateTime timestamp;

    public Comment(String text)
    {
        this(null, text, LocalDateTime.now());
    }

    // public Comment(String commentID, String text, LocalDateTime timestamp)
    // {
    //     this(commentID, text, timestamp);
    // }

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
        return String.format("(%s) %s\n%s", commentID, timestamp.toString(), text);
    }
}

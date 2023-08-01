package resources.entities;

import resources.enums.UserType;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @ AllArgsConstructor
public class User
{
    private String username;
    private UserType userType;
    private String SIN;
    private String occupation;
    private LocalDate birthday;
    private String firstName;
    private String lastName;
    // Maybe change type
    private String hashedPass;

    public User(String username, UserType userType, String SIN, String occupation,
        String birthday, String firstName, String lastName, String hashedPass)
    {
        this(username, userType, SIN, occupation, LocalDate.parse(birthday), firstName, lastName, hashedPass);
    }

    @Override
    public boolean equals(Object right)
    {
        if (!(right instanceof User))
            return false;

        return username == ((User) right).getUsername();
    }

    @Override
    public String toString()
    {
        return String.format("%s %s (%s)", firstName, lastName, username);
    }
}

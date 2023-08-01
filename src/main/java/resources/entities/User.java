package resources.entities;

import resources.enums.UserType;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @ AllArgsConstructor
public class User
{
    private String UID;
    private UserType userType;
    private String SIN;
    private String occupation;
    private LocalDate birthday;
    private String firstName;
    private String lastName;
    private String userName;
    // Maybe change type
    private String hashedPass;

    public User(String UID, UserType userType, String SIN, String occupation,
        String birthday, String firstName, String lastName, String userName, String hashedPass)
    {
        this(UID, userType, SIN, occupation, LocalDate.parse(birthday), firstName, lastName, userName, hashedPass);
    }

    public User(UserType userType, String SIN, String occupation,
        String birthday, String firstName, String lastName, String userName, String hashedPass)
    {
        this("NULL", userType, SIN, occupation, LocalDate.parse(birthday), firstName, lastName, userName, hashedPass);
    }

    @Override
    public boolean equals(Object right)
    {
        if (!(right instanceof User))
            return false;

        return UID == ((User) right).getUID();
    }

    @Override
    public String toString()
    {
        return String.format("%s %s (%s)", firstName, lastName, UID);
    }
}

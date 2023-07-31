package resources.entities;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class PaymentInfo 
{
    private String cardNum;
    private String securityCode;
    private String firstName;
    private String lastName;
    private LocalDate expDate;
    private String postalCode;

    public PaymentInfo(String cardNum, String securityCode, String firstName,
        String lastName, String expDate, String postalCode)
    {
        this(cardNum, securityCode, firstName, lastName, LocalDate.parse(expDate), postalCode);
    }

    @Override
    public boolean equals(Object right)
    {
        if (!(right instanceof PaymentInfo))
            return false;

        return cardNum == ((PaymentInfo) right).getCardNum();
    }

    @Override
    public String toString()
    {
        return String.format("%s", cardNum);
    }
}

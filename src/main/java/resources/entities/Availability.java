package resources.entities;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class Availability
{
    private LocalDate startDate;
    private LocalDate endDate;
    private String listingID;
    private Float pricePerDay;


    public float getTotalPrice()
    {
        return pricePerDay * ChronoUnit.DAYS.between(startDate, endDate);
    }

    @Override
    public boolean equals(Object right)
    {
        if (!(right instanceof Availability))
            return false;

        Availability right2 = (Availability) right;

        return startDate == right2.getStartDate() && endDate == right2.getEndDate();
    }

    @Override
    public String toString()
    {
        return String.format("%s - %s", startDate, endDate);
    }
}

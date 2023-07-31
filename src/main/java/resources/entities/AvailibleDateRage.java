package resources.entities;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class AvailibleDateRage
{
    private LocalDate startDate;
    private LocalDate endDate;

    @Override
    public boolean equals(Object right)
    {
        if (!(right instanceof AvailibleDateRage))
            return false;

        AvailibleDateRage right2 = (AvailibleDateRage) right;

        return startDate == right2.getStartDate() && endDate == right2.getEndDate();
    }

    @Override
    public String toString()
    {
        return String.format("%s - %s", startDate, endDate);
    }
}

package resources.relations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class Booking 
{
    private String listingID;
    private LocalDate startDate;
    private String renterID;
    private String totalPrice;
    private String cardNum;
    private String cancelled_by;
}

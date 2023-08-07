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
    private String bookingID;
    private String listingID;
    private LocalDate startDate;
    private LocalDate endDate;
    private String renterID;
    private Float totalPrice;
    private String cardNum;
    private String cancelledBy;

    public Booking(String listingID, LocalDate startDate, LocalDate endDate, String renterID, Float totalPrice, String cardNum, String cancelledBy)
    {
        this(UUID.randomUUID().toString(), listingID, startDate, endDate, renterID, totalPrice, cardNum, cancelledBy);
    }
}

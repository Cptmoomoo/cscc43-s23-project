package resources.relations;

import java.time.LocalDate;
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

    @Override
    public String toString()
    {
        String str = String.format("Booking ID: %s\nListing ID: %s\nFrom %s to %s\nRented by: %s\nPaid with card: %s\nTotal price: $%.2f",
                                 bookingID,
                                 listingID,
                                 startDate.toString(),
                                 endDate.toString(),
                                 renterID,
                                 cardNum,
                                 totalPrice);
        if (!cancelledBy.isEmpty())
            str = String.format("[THIS BOOKING WAS CANCELLED BY %s AND IS NOW INACTIVE]\n", cancelledBy) + str;
    
        return str;
    }
}

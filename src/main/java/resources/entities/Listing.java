package resources.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import resources.enums.AmenityType;
import resources.enums.ListingType;

@Getter @Setter @AllArgsConstructor
public class Listing
{
    private String listingID;
    private ListingType listingType;
    private String suiteNum;
    private Boolean isActive;
    private Float pricePerDay;
    private LocalDateTime timeListed;

    private ArrayList<AmenityType> amenities;
    private Location location;

    public Listing(ListingType listingType, Float pricePerDay, ArrayList<AmenityType> amenities)
    {
        this(UUID.randomUUID().toString(), listingType, "", true, pricePerDay, LocalDateTime.now(), amenities, null);
    }

    public Listing(ListingType listingType, String suiteNum, Float pricePerDay, ArrayList<AmenityType> amenities)
    {
        this(UUID.randomUUID().toString(), listingType, suiteNum, true, pricePerDay, LocalDateTime.now(), amenities, null);
    }

    public Listing(ListingType listingType, Float pricePerDay)
    {
        this(UUID.randomUUID().toString(), listingType, "", true, pricePerDay, LocalDateTime.now(), new ArrayList<AmenityType>(), null);
    }

    public Listing(ListingType listingType, String suiteNum, Float pricePerDay)
    {
        this(UUID.randomUUID().toString(), listingType, suiteNum, true, pricePerDay, LocalDateTime.now(), new ArrayList<AmenityType>(), null);
    }

    @Override
    public boolean equals(Object right)
    {
        if (!(right instanceof Listing))
            return false;

        return listingID == ((Listing) right).getListingID();
    }

    @Override
    public String toString()
    {
        // Need to print nice format, with nice format of location
        if (suiteNum != null)
            return String.format("%s (%s) %s", listingType, listingID, amenities.toString());
        
        return String.format("%s %s (%s) %s", suiteNum, listingType, listingID, amenities.toString());
    }

}

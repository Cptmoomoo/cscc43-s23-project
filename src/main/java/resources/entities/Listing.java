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
    private LocalDateTime timeListed;

    private ArrayList<AmenityType> amenities;
    private Location location;

    public Listing(ListingType listingType, ArrayList<AmenityType> amenities)
    {
        this(UUID.randomUUID().toString(), listingType, "", true, LocalDateTime.now(), amenities, null);
    }

    public Listing(ListingType listingType, String suiteNum, ArrayList<AmenityType> amenities)
    {
        this(UUID.randomUUID().toString(), listingType, suiteNum, true, LocalDateTime.now(), amenities, null);
    }

    public Listing(ListingType listingType)
    {
        this(UUID.randomUUID().toString(), listingType, "", true, LocalDateTime.now(), new ArrayList<AmenityType>(), null);
    }

    public Listing(ListingType listingType, String suiteNum)
    {
        this(UUID.randomUUID().toString(), listingType, suiteNum, true, LocalDateTime.now(), new ArrayList<AmenityType>(), null);
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

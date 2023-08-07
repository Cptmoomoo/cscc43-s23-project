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
    private Integer maxGuests;

    public Listing(ListingType listingType, ArrayList<AmenityType> amenities, Location location, Integer maxGuests)
    {
        this(UUID.randomUUID().toString(), listingType, "", true, LocalDateTime.now(), amenities, location, maxGuests);
    }

    public Listing(ListingType listingType, String suiteNum, ArrayList<AmenityType> amenities, Location location, Integer maxGuests)
    {
        this(UUID.randomUUID().toString(), listingType, suiteNum, true, LocalDateTime.now(), amenities, location, maxGuests);
    }

    public Listing(ListingType listingType)
    {
        this(UUID.randomUUID().toString(), listingType, "", true, LocalDateTime.now(), new ArrayList<AmenityType>(), null, 0);
    }

    public Listing(ListingType listingType, String suiteNum)
    {
        this(UUID.randomUUID().toString(), listingType, suiteNum, true, LocalDateTime.now(), new ArrayList<AmenityType>(), null, 0);
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
        Location location2 = location;
        if (location2 == null)
            location2 = new Location((float) 0, (float) 0, "", "", "", "");

        // Need to print nice format, with nice format of location
        if (suiteNum != null)
            return String.format("%s (%s) %s\n%s", listingType, listingID, amenities.toString(), location2.toString());
        
        return String.format("%s %s (%s) %s\n%s", suiteNum, listingType, listingID, amenities.toString(), location2.toString());
    }

}

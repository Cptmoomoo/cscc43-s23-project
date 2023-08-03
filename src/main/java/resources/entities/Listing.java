package resources.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

import resources.enums.ListingType;

@Getter @Setter @AllArgsConstructor
public class Listing
{
    private String listingID;
    private ListingType listingType;
    private String suiteNum;
    private Boolean isActive;
    private Float pricePerDay;

    public Listing(ListingType listingType, Float pricePerDay)
    {
        this(null, listingType, "", true, pricePerDay);
    }

    public Listing(ListingType listingType, String suiteNum, Float pricePerDay)
    {
        this(null, listingType, suiteNum, true, pricePerDay);
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
        if (suiteNum != null)
            return String.format("%s (%s)", listingType, listingID);
        
        return String.format("%s %s (%s)", suiteNum, listingType, listingID);
    }

}

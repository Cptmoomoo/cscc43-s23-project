package resources.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import resources.enums.ListingType;

@Getter @Setter @AllArgsConstructor
public class Listing
{
    private String listingID;
    private ListingType listingType;
    private Integer suiteNum;
    private Boolean isActive;
    private Float pricePerDay;

    public Listing(ListingType listingType, Float pricePerDay)
    {
        this("NULL", listingType, null, true, pricePerDay);
    }

    public Listing(ListingType listingType, Integer suiteNum, Float pricePerDay)
    {
        this("NULL", listingType, suiteNum, true, pricePerDay);
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

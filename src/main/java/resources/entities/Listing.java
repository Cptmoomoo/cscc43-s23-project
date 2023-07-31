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
    private String suiteNum;
    private boolean isActive;
    private float pricePerDay;

    public Listing(ListingType listingType, float pricePerDay)
    {
        this("NULL", listingType, "", true, pricePerDay);
    }

    public Listing(ListingType listingType, String suiteNum, float pricePerDay)
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
        if (suiteNum.isEmpty())
            return String.format("%s (%s)", listingType, listingID);
        
        return String.format("%s %s (%s)", suiteNum, listingType, listingID);
    }

}

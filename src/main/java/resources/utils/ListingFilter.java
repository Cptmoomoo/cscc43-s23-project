package resources.utils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.c43backend.daos.AvailabilityDAO;

import resources.entities.Listing;
import resources.enums.AmenityType;

public class ListingFilter
{
    public final AvailabilityDAO aDAO;

    public ListingFilter(AvailabilityDAO aDAO)
    {
        this.aDAO = aDAO;
    }

    public void sortListByPrice(ArrayList<Listing> listings, Boolean asc)
    {
        Collections.sort(listings, sortbyPrice(asc));
    }

    public ArrayList<Listing> filterByPostalCode(ArrayList<Listing> listings, String postalCode)
    {
        ArrayList<Listing> filtered = new ArrayList<Listing>();

        for (Listing l : listings)
        {
            if (postalCode.substring(0, 3).equals(l.getLocation().getPostalCode().substring(0, 3)))
                filtered.add(l);
        }

        return filtered;
    }

    public ArrayList<Listing> filterByAmenities(ArrayList<Listing> listings, ArrayList<AmenityType> amenities)
    {
        ArrayList<Listing> filtered = new ArrayList<Listing>();
        Boolean cond = true;

        for (Listing l : listings)
        {
            cond = true;
            for (AmenityType a : amenities)
            {
                if (!l.getAmenities().contains(a))
                {
                    cond = false;
                    break;
                }
            }

            if (cond)
                filtered.add(l);
        }

        return filtered;
    }

    public ArrayList<Listing> filterByPriceRange(ArrayList<Listing> listings, Float max, Float min)
    {
        ArrayList<Listing> filtered = new ArrayList<Listing>();
        Float avg;

        for (Listing l : listings)
        {
            avg = l.getAvgPrice();
            if (max >= avg && min <= avg)
                filtered.add(l);
        }

        return filtered;
    }

    public ArrayList<Listing> filterByDate(ArrayList<Listing> listings, LocalDate start, LocalDate end)
    {
        ArrayList<Listing> filtered = new ArrayList<Listing>();

        for (Listing l : listings)
        {
            if (aDAO.isAvailible(start, end, l.getListingID()))
                filtered.add(l);
        }

        return filtered;
    }

    private Comparator<Listing> sortbyPrice(Boolean asc)
    {
        if (asc)
            return new Comparator<Listing>()
                {
                    @Override
                    public int compare(Listing l1, Listing l2)
                    {
                        return Float.compare(l1.getAvgPrice(), l2.getAvgPrice());
                    }
                };
        else
            return new Comparator<Listing>()
            {
                @Override
                public int compare(Listing l1, Listing l2)
                {
                    return Float.compare(l2.getAvgPrice(), l1.getAvgPrice());
                }
            };
    }
}

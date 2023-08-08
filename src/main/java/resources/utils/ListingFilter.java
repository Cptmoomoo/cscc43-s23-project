package resources.utils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.c43backend.daos.AvailabilityDAO;

import resources.entities.Availability;
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

        for (Listing l : listings)
        {
            for (AmenityType a : amenities)
            {
                if (!l.getAmenities().contains(a))
                    continue;
            }

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
            avg = getAvgPriceOfListing(l);
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
                        return Float.compare(getAvgPriceOfListing(l2), getAvgPriceOfListing(l1));
                    }
                };
        else
            return new Comparator<Listing>()
            {
                @Override
                public int compare(Listing l1, Listing l2)
                {
                    return Float.compare(getAvgPriceOfListing(l1), getAvgPriceOfListing(l2));
                }
            };
    }

    private Float getAvgPriceOfListing(Listing l)
    {
        ArrayList<Availability> avails;
        Float total = (float) 0;

        avails = aDAO.getAvailabilitiesByListing(l.getListingID());

        if (avails.isEmpty())
            return (float) 0;

        for (Availability a : avails)
            total += a.getPricePerDay();

        return total / avails.size();
    }
}

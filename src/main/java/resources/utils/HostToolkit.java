package resources.utils;

import java.util.ArrayList;
import java.util.HashMap;

import org.javatuples.Quartet;

import resources.enums.AmenityType;
import resources.enums.ListingType;

public class HostToolkit
{
    private static final HashMap<AmenityType, Float> amenityWeight = new HashMap<AmenityType, Float>()
    {
        {
            put(AmenityType.POOL, (float) 0.25);
            put(AmenityType.WIFI, (float) 0.05);
            put(AmenityType.KITCHEN, (float) 0.15);
            put(AmenityType.JACUZZI, (float) 0.15);
            put(AmenityType.AIR_CONDITIONING, (float) 0.1);
            put(AmenityType.HEATER, (float) 0.2);
            put(AmenityType.PETS_ALLOWED, (float) 0.1);
            put(AmenityType.WASHER_DRYER, (float) 0.1);
            put(AmenityType.KITCHENWARE, (float) 0.05);
            put(AmenityType.BREAKFAST, (float) 0.1);
            put(AmenityType.STEP_FREE_ENTRANCE, (float) 0.05);
            put(AmenityType.WIDE_ENTRANCE, (float) 0.05);
            put(AmenityType.WIDE_HALLWAYS, (float) 0.1);
            put(AmenityType.ACCESSIBLE_BATHROOM, (float) 0.1);
        }
    };

    private static final HashMap<ListingType, Float> pricePerListingType = new HashMap<ListingType, Float>()
    {
        {
            put(ListingType.ENTIRE_PLACE, (float) 125);
            put(ListingType.SHARED_ROOM, (float) 50);
            put(ListingType.HOTEL_ROOM, (float) 100);
            put(ListingType.PRIVATE_ROOM, (float) 75);
        }
    };

    public static Float suggestPrice(ListingType type, Integer maxGuests, ArrayList<AmenityType> amenities)
    {
        Float basePricePerPerson = pricePerListingType.get(type);
        Float totalBase = basePricePerPerson * maxGuests;
        Float total = totalBase;

        for (AmenityType a : amenities)
        {
            total += basePricePerPerson * amenityWeight.get(a);
        }

        return total;
    }

    public static Quartet<AmenityType, AmenityType, AmenityType, Float> suggestAmenities(ArrayList<AmenityType> amenities)
    {
        AmenityType one = null;
        AmenityType two = null;
        AmenityType three = null;
        Float weight;
        Float total = (float) 0;


        if (amenities.size() == AmenityType.values().length)
        {
            System.out.println("You already have all amenities!");
            return null;
        }

        for (AmenityType a : amenityWeight.keySet())
        {
            if (amenities.contains(a))
                continue;

            if (one == null)
            {
                one = a;
                total += amenityWeight.get(a);
                continue;
            }
            else if (two == null)
            {
                two = a;
                total += amenityWeight.get(a);
                continue;
            }
            else if (three == null)
            {
                three = a;
                total += amenityWeight.get(a);
                continue;
            }

            weight = amenityWeight.get(a);

            if (weight > amenityWeight.get(one))
            {
                total -= amenityWeight.get(three);
                total += amenityWeight.get(a);
                three = two;
                two = one;
                one = a;
                continue;
            }
            else if (weight > amenityWeight.get(two))
            {
                total -= amenityWeight.get(three);
                total += amenityWeight.get(a);
                three = two;
                two = a;
                continue;
            }
            else if (weight > amenityWeight.get(three))
            {
                total -= amenityWeight.get(three);
                total += amenityWeight.get(a);
                three = a;
                continue;
            }
        }


        return  new Quartet<AmenityType, AmenityType, AmenityType, Float>(one, two, three, total);

    }
}

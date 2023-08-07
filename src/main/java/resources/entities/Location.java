package resources.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import resources.utils.Coordinate;

@Getter @Setter @AllArgsConstructor
public class Location 
{
    private Coordinate coordinate;
    private String postalCode;
    private String streetNum;
    private String street;
    private String city;
    private String country;
    private String province;
   

    public Location(Float longitude, Float latitude,
        String postalCode, String streetNum, String street, String country, String province, String city)
    {
        this(new Coordinate(latitude, longitude), postalCode, streetNum, street, city, country, province);
    }

    @Override
    public boolean equals(Object right)
    {
        if (!(right instanceof Location))
            return false;

        return coordinate == ((Location) right).getCoordinate();
    }

    @Override
    public String toString()
    {
        return String.format("(%s)\n%s %s\n%s %s %s, %s", coordinate, streetNum, street, city, province, country, postalCode);
    }
}

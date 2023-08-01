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
    private String city;
    private String country;
    private String province;

    public Location(Float longitude, Float latitude,
        String postalCode, String country, String province, String city)
    {
        this(new Coordinate(latitude, longitude), postalCode, city, country, province);
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
        return String.format("%s, %s, %s (%s)", city, province, country, coordinate.toString());
    }
}

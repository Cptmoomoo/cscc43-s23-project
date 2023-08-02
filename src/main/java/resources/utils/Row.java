package resources.utils;

import java.util.ArrayList;

import lombok.Getter;

@Getter
public class Row
{
    private ArrayList<Object> row;
    private Integer size = 0;
    private Integer maxSize = 0;

    public Row(Integer numCols)
    {
        this.row = new ArrayList<Object>(numCols);
        this.maxSize = numCols;
    }

    public Boolean addTo(Object item)
    {
        if (row.size() >= maxSize)
            return false;

        size++;
        row.add(item);
        return true;
    }

    public Object getColumn(Integer colNum)
    {
        return row.get(colNum);
    }
}

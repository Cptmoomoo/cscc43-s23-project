package resources.utils;

import java.util.ArrayList;
import org.javatuples.Triplet;
import lombok.Getter;

@Getter
public class Table
{
    private ArrayList<Triplet<String, Integer, Class<?>>> columnMetaData;
    private ArrayList<Row> table;
    private Integer numCols;
    private Integer numRows;

    public Table(Integer numCols, Integer numRows, ArrayList<Triplet<String, Integer, Class<?>>> columnMetaData)
    {
        this.columnMetaData = columnMetaData;
        table = new ArrayList<Row>(numRows);
        this.numCols = numCols;
        this.numRows = numRows;
    }

    public Boolean addRow(Row row)
    {
        if (row.getSize() != numCols)
            return false;
        
        for (int i = 0; i < numCols; i++)
        {

            if (!row.getColumn(i).getClass().equals(getTripletByIdx(i).getValue2()))
                return false;
        }

        return table.add(row);
    }

    public Integer size()
    {
        return table.size();
    }

    public void clearTable()
    {
        table.clear();
    }

    public Row getRow(Integer rowNum)
    {
        return table.get(rowNum);
    }

    public Boolean isEmpty()
    {
        return table.isEmpty();
    }

    public Object extractValueFromRowByName(Integer rowNum, String name)
    {
        Row row = getRow(rowNum);

        for (Triplet<String, Integer, Class<?>> t : columnMetaData)
        {
            if (t.getValue0().equals(name))
                return row.getColumn(t.getValue1());
        }

        return null;
    }

    private Triplet<String, Integer, Class<?>> getTripletByIdx(Integer idx)
    {
        for (Triplet<String, Integer, Class<?>> t : columnMetaData)
        {
            if (t.getValue1() == idx)
                return t;
        }

        return null;
    }
}

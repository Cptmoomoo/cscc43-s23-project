package resources.utils;

import java.util.ArrayList;
import java.util.HashMap;

import org.javatuples.Pair;

import lombok.Getter;

@Getter
public class Table
{
    private ArrayList<Pair<String, Class>> columnMetaData;
    private ArrayList<Row> table;
    private Integer numCols;
    private Integer numRows;

    public Table(Integer numCols, Integer numRows, ArrayList<String> colNames, ArrayList<Class> classes)
    {
        columnMetaData = new ArrayList<Pair<String, Class>>(numCols);
        table = new ArrayList<Row>(numRows);
        this.numCols = numCols;
        this.numRows = numRows;

        for (int i = 0; i < numCols; i++)
        {
            columnMetaData.add(new Pair<String, Class>(colNames.get(i), classes.get(i)));
        }
    }

    public boolean addRow(Row row)
    {
        if (row.getSize() != numCols)
            return false;
        
        for (int i = 0; i < numCols; i++)
        {

            if (!row.getColumn(i).getClass().equals(columnMetaData.get(i).getValue1()))
                return false;
        }

        return table.add(row);
    }

    public Row getRow(Integer rowNum)
    {
        return table.get(rowNum);
    }


}

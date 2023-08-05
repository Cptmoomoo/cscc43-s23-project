package resources.utils;

import java.util.ArrayList;
import java.util.Iterator;

import org.javatuples.Triplet;

import lombok.Getter;
import resources.exceptions.RowShapeMismatchException;

@Getter
public class Table implements Iterable<Row>
{
    private ArrayList<Triplet<String, Integer, Class<?>>> columnMetaData;
    private ArrayList<Row> table;
    private Integer numCols;

    public Table(Integer numCols, ArrayList<Triplet<String, Integer, Class<?>>> columnMetaData)
    {
        this.columnMetaData = columnMetaData;
        table = new ArrayList<Row>();
        this.numCols = numCols;
    }

    public Boolean addRow(Row row)
    {
        if (row.getSize() != numCols)
            return false;
        
        for (int i = 0; i < numCols; i++)
            if (!row.getColumn(i).getClass().equals(getTripletByIdx(i).getValue2()))
                throw new RowShapeMismatchException(row.getColumn(i).getClass(), getTripletByIdx(i).getValue2());

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

    @Override
    public Iterator<Row> iterator()
    {
        return table.iterator();
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

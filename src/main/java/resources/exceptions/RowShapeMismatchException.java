package resources.exceptions;

public class RowShapeMismatchException extends RuntimeException
{
    public RowShapeMismatchException(Class<?> rowClass, Class<?> tableClass)
    {
        super(String.format("Trying to insert row with type %s into table with type %s.", rowClass.toString(), tableClass.toString()));
    }
}

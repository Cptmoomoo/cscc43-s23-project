package resources.exceptions;

public class RunQueryException extends RuntimeException
{
    public RunQueryException()
    {
        super("There was a problem executing/setting a query! Should not happen unless there is a syntax error or the query is wrong!");
    }
}

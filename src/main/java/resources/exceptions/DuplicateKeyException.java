package resources.exceptions;

public class DuplicateKeyException extends Exception
{
    String keys;

    public DuplicateKeyException(String keys)
    {
        super(String.format("The keys %s already exist!", keys));
        this.keys = keys;
    }
}

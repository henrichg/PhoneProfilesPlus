package sk.henrichg.phoneprofilesplus;

/**
 * Exception thrown when writng nfc tags
 */
public class NFCTagWriteException extends Exception {
    //private static final long serialVersionUID = 4647185067874734143L;

    public enum NFCErrorType {
        ReadOnly, NoEnoughSpace, tagLost, formattingError, noNdefError, unknownError
    };

    NFCErrorType type;

    public NFCTagWriteException(NFCErrorType type) {
        super();
        this.type = type;
    }

    public NFCTagWriteException(NFCErrorType type, Exception e) {
        super(e);
        this.type = type;
    }

    /**
     * @return type cause of the exception
     */
    public NFCErrorType getType() {
        return type;
    }
}

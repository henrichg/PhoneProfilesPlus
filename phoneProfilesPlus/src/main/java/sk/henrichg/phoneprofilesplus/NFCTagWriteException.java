package sk.henrichg.phoneprofilesplus;

/**
 * Exception thrown when writng nfc tags
 */
class NFCTagWriteException extends Exception {
    //private static final long serialVersionUID = 4647185067874734143L;

    enum NFCErrorType {
        ReadOnly, NoEnoughSpace, tagLost, formattingError, noNdefError, unknownError
    }

    private NFCErrorType type;

    NFCTagWriteException(NFCErrorType type) {
        super();
        this.type = type;
    }

    NFCTagWriteException(NFCErrorType type, Exception e) {
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

package src.de.htwsaar.chessbot.engine.model;

import java.util.ArrayList;

public class Pieces {

    public static final int PAWN = 0;
    public static final int ROOK = 1;
    public static final int KNIGHT = 2;
    public static final int BISHOP = 3;
    public static final int QUEEN = 4;
    public static final int KING = 5;

    private static Pieces INSTANCE;

    private Piece[] prototypes;
    private ArrayList<Piece> existingPieces;

    private Pieces() {
        this.prototypes = new Piece[6];
        this.existingPieces = new ArrayList<Piece>();
    }

    public static Pieces getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Pieces();
        }

        return INSTANCE;
    }

    public Piece getPiece(int pieceType, Position position, boolean isWhite, boolean hasMoved) {
        //TODO: Fehleberhandlung f�r ung�ltige Parameter
        Piece piece = getExistingPiece(pieceType, position, isWhite, hasMoved);
        if(piece == null) {
            piece = this.getPrototypeCopy(pieceType);
            piece.setPosition(position);
            piece.setIsWhite(isWhite);
            piece.setHasMoved(hasMoved);
            this.existingPieces.add(piece);
        }
        return piece;
    }

    private Piece getExistingPiece(int pieceType, Position position, boolean isWhite, boolean hasMoved) {
        //TODO: Fehleberhandlung f�r ung�ltige Parameter
        Piece foundPiece = null;
        for(int i = 0; i < existingPieces.size() && foundPiece == null; i++) {
            Piece piece = existingPieces.get(i);
            if(piece.getPosition().equals(position) && piece.isWhite() == isWhite && piece.hasMoved() == hasMoved) {
                foundPiece = piece;
            }
        }

        return foundPiece;
    }

    private Piece getPrototypeCopy(int pieceType) {
        //TODO: Fehlerbehandlung f�r ung�ltigen PieceType
        if(this.prototypes[pieceType] == null) {
            Piece newPrototype;
            switch(pieceType) {
                case PAWN:
                    newPrototype = new Pawn(new Position(), false, false);
                    break;
                case ROOK:
                    newPrototype = new Rook(new Position(), false, false);
                    break;
                case KNIGHT:
                    newPrototype = new Knight(new Position(), false, false);
                    break;
                case BISHOP:
                    newPrototype = new Bishop(new Position(), false, false);
                    break;
                case QUEEN:
                    newPrototype = new Queen(new Position(), false, false);
                    break;
                case KING:
                    newPrototype = new King(new Position(), false, false);
                    break;
            }
            this.prototypes[pieceType] = newPrototype;
        }

        return this.prototypes[pieceType].clone();
    }

}
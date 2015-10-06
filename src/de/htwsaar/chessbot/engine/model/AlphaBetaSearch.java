package de.htwsaar.chessbot.engine.model;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import de.htwsaar.chessbot.engine.model.GameTree.Node;

/**
 * 
 * @author David Holzapfel
 * @author Dominik Becker
 *
 */

public class AlphaBetaSearch extends Thread implements DeepeningInterrupter {

	public static void main(String[] args) throws IOException {
		Game game = new Game(
				//"8/2p1pp2/8/4k3/8/1Q6/PPP4P/RN5K b - - 0 1"
				);

		AlphaBetaSearch alphaBetaSearch = new AlphaBetaSearch(game);
		alphaBetaSearch.setMaxSearchDepth(10000);
		alphaBetaSearch.setTimeLimit(0);
		alphaBetaSearch.setPondering(true);
		alphaBetaSearch.start();
		alphaBetaSearch.startSearch();
	}

	private Game game;
	//private GameTree gameTree;
	//private BackgroundDeepener bgDeepener;
	private final EvaluationFunction evalFunc = new Evaluator();

	private int maxSearchDepth;
	private int maxTime = 0;
	private Collection<Move> limitedMoveList = null;
	private boolean isPondering = false;
	private boolean ponderHit = false;

	private long startTime = 0;
	private long nodesSearched = 0;
	private double nodesPerSecond = 0;
	private volatile boolean exitSearch = false;

	private Move currentBestMove;
	private Move currentPonderMove;
	private Node ponderNode;
	private int currentBestScore = 0;
	private int currentPonderScore = 0;
	private int currentMoveNumber = 0;
	private int currentSearchDepth = 0;

	/**
	 * Erstellt einen BestMove-Sucher der mit AlphaBeta Search arbeitet.
	 * 
	 * @param game  GameState
	 */
	public AlphaBetaSearch(Game game) {
		this.game = game;
		this.exitSearch = true;
		/*
		this.bgDeepener = new BackgroundDeepener(this);
		this.bgDeepener.setMaxDepth(15);
		this.bgDeepener.setPriority(Thread.MIN_PRIORITY);
		this.bgDeepener.start();
		*/
	}

	public void setGame(Game game) {
		this.game = game;
		//this.gameTree = null;
		this.ponderHit = false;
	}

	public void startSearch() {
		this.exitSearch = false;
	}

	@Override
	public void run() {
		while(true) {
			while(exitSearch) {
				/*
				if((boolean)Config.getInstance().getOption("Ponder").getValue()) {
					this.bgDeepener.beginDeepening();
				}
				*/
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			//this.bgDeepener.endDeepening();
			this.startTime = System.currentTimeMillis();
			startAlphaBeta(this.ponderHit);
		}
	}

	public void stopSearch() {
		this.exitSearch = true;
	}
	
	public boolean getSearchStopped() {
		if(this.maxTime > 0 && getPassedTime() >= this.maxTime) {
			this.exitSearch = true;
		}
		
		return this.exitSearch;
	}

	/**
	 * Setzt die maximale Suchtiefe für den Baum.
	 * 
	 * @param depth
	 */
	public synchronized void setMaxSearchDepth(int depth) {
		this.maxSearchDepth = depth;
	}

	/**
	 * Setzt der Suche ein Zeitlimit, nach dem automatisch abgebrochen wird.
	 * 
	 * @param millis	Millisekunden bis zum Abbruch
	 */
	public void setTimeLimit(int millis) {
		this.maxTime = millis;
	}
	
	public int getPassedTime() {
		return (int) (System.currentTimeMillis() - this.startTime);
	}
	
	public void setPondering(boolean isPondering) {
		this.isPondering = isPondering;
	}
	
	public void ponderhit() {
		this.ponderHit = true;
	}
	
	/**
	 * Gibt dem Algorithmus eine Liste von Zuegen vor, die ausschließlich
	 * untersucht werden soll.
	 * 
	 * @param moveList
	 */
	public void setLimitedMoveList(Collection<Move> moveList) {
		this.limitedMoveList = moveList;
	}

	/**
	 * Hebt die Zuglistenbeschraenkung auf.
	 */
	public void resetLimitMoveList() {
		this.limitedMoveList = null;
	}

	/**
	 * Gibt den zum jetztigen Zeitpunkt besten Zug zurueck, der
	 * ermittelt wurde.
	 * 
	 * @return
	 */
	public synchronized Move getCurrentBestMove() {
		return this.currentBestMove;
	}

	private void ponderMove(Move move, int score, Node ponderNode) {
		this.currentPonderMove = move;
		this.currentPonderScore = score;
		this.ponderNode = ponderNode;
	}

	//Sendet Informationen ueber den Zustand der Suche an die GUI
	private void sendInfo(Move currentMove) {
		UCISender.getInstance().sendToGUI(
				"info currmove " + currentMove +
				" currmovenumber " + this.currentMoveNumber +
				" depth " + this.currentSearchDepth +
				" time " + getPassedTime() +
				" nps " + (int)this.nodesPerSecond +
				" nodes " +  this.nodesSearched
				);
	}

	//Sendet den "besten" Zug nachdem die Suche beendet wurde
	public void sendBestMove() {
		String bestMove = "bestmove " + this.currentBestMove;
		if(this.isPondering) {
			bestMove += " ponder " + this.currentPonderMove;
		}
		UCISender.getInstance().sendToGUI(bestMove);
	}

	//Startet die Suche
	private void startAlphaBeta(boolean ponderHit) {
		this.currentBestMove = null;
		this.exitSearch = false;
		this.nodesSearched = 0;
		boolean startMax = this.game.getCurrentBoard().isWhiteAtMove();

		for(int i = 1; i <= maxSearchDepth && !getSearchStopped(); i++) { 
			this.currentSearchDepth = i;
			this.currentMoveNumber = 0;
			//alphaBeta(this.gameTree.getRoot(), Integer.MIN_VALUE, Integer.MAX_VALUE, i, startMax);
			alphaBeta(this.game.getCurrentBoard(), Integer.MIN_VALUE, Integer.MAX_VALUE, startMax, 0);

			this.nodesPerSecond = 
					(1000d * this.nodesSearched) / getPassedTime();
		}

		sendBestMove();
		UCISender.getInstance().sendToGUI("info string Search completed in " + getPassedTime() + "ms ("
				+ "to depth " + this.currentSearchDepth + ")");
		this.exitSearch = true;
	}
	
	private int alphaBeta(Board currentBoard, int alpha, int beta, boolean max, int depth) {
	    
	    if(depth >= currentSearchDepth || getSearchStopped()) {
	        return evaluate(currentBoard);
	    }
	    
	    long boardHash = currentBoard.hash();
	    TranspositionTable tTable = TranspositionTable.getInstance();
	    if(tTable.hasBetterResults(boardHash, depth)) {
	        return tTable.getScore(boardHash);
	    }
	    
	    List<Board> boardList = (List<Board>) currentBoard.getBoardList();
	    
	    if(depth == 0) {
	        bestMove(boardList.get(0));
	        if(boardList.size() == 1) {
	            return 0;
	        }
	    }
	    
	    
	    if(max) {
	        for(Board board : boardList) {
	        	if(getSearchStopped()) {
	        		break;
	        	}
	        	
	        	if(depth == 0) {
	        		currentMoveNumber++;
	        	}
	        	
	            int result = alphaBeta(board, alpha, beta, !max, depth + 1);
	            tTable.put(board.hash(), depth, result);
	            if(result > alpha) {
	                alpha = result;
	                if(depth == 0) {
	                    bestMove(board);
	                }
	            }
	            if(alpha >= beta) {
	                return alpha;
	            }
	            
	            sendInfo(board.getMove());
	        }
	        return alpha;
	    }
	    else {
	        for(Board board : boardList) {
	        	if(getSearchStopped()) {
	        		break;
	        	}
	        	
	        	if(depth == 0) {
	        		currentMoveNumber++;
	        	}
	        	
	            int result = alphaBeta(board, alpha, beta, !max, depth + 1);
	            tTable.put(board.hash(), depth, result);
	            if(result < beta) {
	                beta = result;
	                if(depth == 0) {
	                    bestMove(board);
	                }
	            }
	            if(beta <= alpha) {
	                return beta;
	            }
	            
	    	    sendInfo(board.getMove());
	        }
	        return beta;
	    }
	      
	}

	private void bestMove(Board board) {
	    currentBestMove = board.getMove();
	}

	private int evaluate(Board board) {
	    return evalFunc.evaluate(board);
	}

	/*
	private void alphaBeta(Node n, int alpha, int beta, int depth, boolean max) {

		if(getSearchStopped()) {
			return;
		}

		this.nodesSearched++;
		UCISender.getInstance().sendToGUI("info nodes " + this.nodesSearched);

		Board board = n.getBoard();
		TranspositionTable tTable = TranspositionTable.getInstance();

		if(tTable.contains(board.hash(), max) && tTable.getDepth(board.hash(), max) >= depth) {
			n.setScore(tTable.getScore(board.hash(), max));
			return;
		} else if(n.getChildren().isEmpty()) {
			n.setScore(this.gameTree.getEvaluationFunction().evaluate(board));
			return;
		}

		if(n.isRoot() && n.childCount() == 1) {
			bestMove(((ArrayList<Move>)n.getBoard().getMoveList()).get(0), 0);
			return;
		}

		
		for(Node child : n.getChildren()) {
			
			if(getSearchStopped()) {
				return;
			}
			
			Move move = child.getLeadsTo();
			
			if(n.isRoot() && this.limitedMoveList != null) {
				if(!this.limitedMoveList.contains(move)) {
					continue;
				}
			}

			if(n.isRoot()) {
				this.currentMoveNumber++;
			}
			
			alphaBeta(child, alpha, beta, depth - 1, !max);

			sendInfo(move);
			
			if(max) {

				if(child.getScore() > alpha) {
					alpha = child.getScore();
					if(alpha >= beta) {
						n.setScore(alpha);
						tTable.put(n.getBoard().hash(), depth, alpha, max);
						
						//this.gameTree.cutoff(n, child, this.currentSearchDepth - depth);
						break;
					}
					if(n.isRoot()) {
						bestMove(move, alpha);
					}
					else if(n.getParent().isRoot()) {
						ponderMove(move, alpha, child);
					}
				}

			} else {

				if(child.getScore() < beta) {
					beta = child.getScore();
					if(beta <= alpha) {
						n.setScore(beta);
						tTable.put(n.getBoard().hash(), depth, beta, max);
						
						//this.gameTree.cutoff(n, child, this.currentSearchDepth - depth);
						break;
					}
					if(n.isRoot()) {
						bestMove(move, beta);
					}
					else if(n.getParent().isRoot()) {
						ponderMove(move, beta, child);
					}
				}

			}

		}
		
	}
	*/

	@Override
	public boolean stopDeepening() {
		return this.getSearchStopped();
	}

}
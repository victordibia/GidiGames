package com.denvycom.gidigames;


import java.util.ArrayList;
import java.util.Random;
import org.cocos2d.actions.instant.CCCallFuncN;
import org.cocos2d.actions.interval.CCDelayTime;
import org.cocos2d.actions.interval.CCMoveTo;
import org.cocos2d.actions.interval.CCScaleBy;
import org.cocos2d.actions.interval.CCSequence;
import org.cocos2d.layers.CCColorLayer;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.menus.CCMenu;
import org.cocos2d.menus.CCMenuItemImage;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCNode;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.nodes.CCTextureCache;
import org.cocos2d.opengl.CCBitmapFontAtlas;
import org.cocos2d.opengl.CCTextureAtlas;
import org.cocos2d.particlesystem.CCParticleSystem;
import org.cocos2d.particlesystem.CCQuadParticleSystem;
import org.cocos2d.sound.SoundEngine;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGSize;
import org.cocos2d.types.ccColor3B;
import org.cocos2d.types.ccColor4B;
import org.cocos2d.types.ccColor4F;
import org.cocos2d.utils.CCFormatter;  
 

import android.content.Context;
import android.database.Cursor;





public class TicTacLayer extends CCLayer {

	CCTextureAtlas textureAtlas;
	private int toppoint = 0;
	private CCBitmapFontAtlas bestScoreLabel;
	private int topleft = 0 ;

	private float timehold = 0 ;

	public static  ArrayList<CCNodeExt> spriteList = new ArrayList<CCNodeExt>();
	public static  int[] tileNumbers ;  
	public static  int  tileIndex ;  
	private static CGSize screenSize;
	private static int TILE_SQUARE_SIZE = 150 ;
	private static int NUM_ROWS = 0 ;
	private static int NUM_COLUMNS = 0 ;
	private static Context appcontext ;
	private static int thetime = 0 ;
	private static int moves = 0 ;

	private static int SCORE_LABEL_TAG = 100 ;
	private static int BEST_SCORE_LABEL_TAG = 102 ;
	private static int TIMER_LABEL_TAG = 103 ;
	private static int TILE_NODE_TAG = 105 ;
	private static int MOVES_LABEL_TAG = 106 ;
	private static final int GAME_PUASES_LABEL_TAG = 107;
	private static int PAUSE_OVERLAY_TAG = 322 ;
	private static int SCROLL_TOP_TAG = 321 ;
	private static int PAUSE_MENU_TAG = 348;
	private static int RESUME_MENU_TAG = 347;

	final static int MSG_COMPUTER_TURN = 1;
	final static float COMPUTER_DELAY_MS = 0.4f;
	private static boolean boardplayable = false ;
	private static boolean gameover = false ;

	static Random mRnd = new Random();
	final static State[] mData = new State[9];
	private static State mCurrentPlayer = State.UNKNOWN;

	private State mWinner = State.EMPTY;

	private boolean gamepaused = false ;
	private static final String PLAYER_1_TURN = "Your Turn (Circle)";
	private static final String PLAYER_2_TURN = "Computers Turn (Cross)";
	private static final String GAME_OUTPUT_TIE = "CHAI!! E DON TIE WRAPPER!";
	private static final String GAME_OUTPUT_YOU = "CONGRATULATION!!! YOU WIN";
	private static final String GAME_OUTPUT_COMPUTER = "OOOPS - THE COMPUTER WINS";

	DbAdapter mDbHelper ;
	private float tilescalefactor = 0.0f;
	private int useableheight = 0;
	private float scalefactor = 0.0f;
	private CCBitmapFontAtlas movesLabel;
	private CCParticleSystem emitter;




	public static CCScene scene(int player)
	{

		GidiGamesActivity.nextscene= "TicTacMenuLayer" ;
		 GidiGamesActivity.currentscene = "TicTacLayer" ;
		CCScene scene = CCScene.node();
		CCLayer layer = new TicTacLayer(player);
		scene.addChild(layer);

		return scene;
	}

	public TicTacLayer(int player) {
		super();


		appcontext = CCDirector.sharedDirector().getActivity();
		
		  GidiGamesActivity.checkWordBank();

		mDbHelper = new DbAdapter(appcontext);
		mDbHelper.open();
		int labelbestmoves; 

		Cursor ScoreCursor = mDbHelper.fetchBestScorebyTime("tictactoe", "");
		if(ScoreCursor.getCount() > 0){
			ScoreCursor.moveToFirst();
			labelbestmoves = ScoreCursor.getInt(ScoreCursor.getColumnIndex(
					DbAdapter.KEY_GAME_TIME)) ;
		}else{
			labelbestmoves = 0 ;
		}		

		screenSize = CCDirector.sharedDirector().winSize();
		tilescalefactor   = (screenSize.height/480) ;
		for (int i = 0; i < mData.length; i++) {
			mData[i] = State.EMPTY;
		}

		mDbHelper.close();
		gameover = false ;
		gamepaused = false ;

		//Add Background Sprite Image
		CCSprite background = CCSprite.sprite("background.jpg");
		background.setScale(screenSize.width / background.getContentSize().width);
		background.setAnchorPoint(CGPoint.ccp(0f,1f)) ;
		background.setPosition(CGPoint.ccp(0, screenSize.height));
		addChild(background,-5);



		// Add Timer Label
		CCBitmapFontAtlas timerLabel = CCBitmapFontAtlas.bitmapFontAtlas ("00:00", "bionic.fnt");
		timerLabel.setScale(1.5f*tilescalefactor);
		timerLabel.setAnchorPoint(1f,1f);
		timerLabel.setColor(ccColor3B.ccc3(50, 205, 50));
		timerLabel.setPosition(CGPoint.ccp(screenSize.width - 25*tilescalefactor , screenSize.height - 10*tilescalefactor ));
		addChild(timerLabel,-2,TIMER_LABEL_TAG);


		// Add Moves Label
		  movesLabel = CCBitmapFontAtlas.bitmapFontAtlas ("Moves : 000", "bionic.fnt");
		movesLabel.setScale(0.8f*tilescalefactor);
		movesLabel.setAnchorPoint(1f,0f);
		movesLabel.setColor(ccColor3B.ccc3(50, 205, 50));
		movesLabel.setPosition(CGPoint.ccp(screenSize.width - 25*tilescalefactor, timerLabel.getPosition().y - timerLabel.getContentSize().height*tilescalefactor - 10*tilescalefactor -  timerLabel.getContentSize().height*tilescalefactor));
		addChild(movesLabel,-2,MOVES_LABEL_TAG);


		//Add Best Label
		bestScoreLabel = CCBitmapFontAtlas.bitmapFontAtlas ("BEST : " + CCFormatter.format("%02d:%02d", (labelbestmoves /60) , labelbestmoves % 60 ),  "bionic.fnt");
		bestScoreLabel.setAnchorPoint(1f,0f);
		bestScoreLabel.setScale(0.8f*tilescalefactor);		
		bestScoreLabel.setColor(ccColor3B.ccc3(255, 242, 75));
		bestScoreLabel.setPosition( CGPoint.ccp(screenSize.width - 25*tilescalefactor, movesLabel.getPosition().y - movesLabel.getContentSize().height*tilescalefactor  ));
		addChild(bestScoreLabel,-2, BEST_SCORE_LABEL_TAG);

		//Add Score Label

		CCBitmapFontAtlas scoreLabel = CCBitmapFontAtlas.bitmapFontAtlas ("Tap to Begin (Circle)", "bionic.fnt");
		scoreLabel.setScale(0.9f*tilescalefactor);
		scoreLabel.setAnchorPoint(1f,0f);
		scoreLabel.setPosition( CGPoint.ccp( screenSize.width - 25*tilescalefactor, bestScoreLabel.getPosition().y - bestScoreLabel.getContentSize().height*tilescalefactor));
		addChild(scoreLabel,-2, SCORE_LABEL_TAG);



		//Add Overlay with Start button and Instruction
		CCColorLayer pauseOverlay = CCColorLayer.node(ccColor4B.ccc4(25, 25, 25, 0));
		pauseOverlay.setOpacity(180);
		addChild(pauseOverlay,200,PAUSE_OVERLAY_TAG);

		String helptext =  "1.) PLACE YOUR CIRCLES TO FORM A 3-MARK LINE  \n     diagonal, horizontal or vertical \n"	+
				"2.) WIN by forming a complete line! \n" +
				"3.) Goodluck!!"	;

		CCBitmapFontAtlas easyFontAtlas = CCBitmapFontAtlas.bitmapFontAtlas( helptext , "dalek.fnt");
		easyFontAtlas.setScale(0.9f*tilescalefactor);
		easyFontAtlas.setPosition(CGPoint.make(screenSize.width/2, screenSize.height/2));
		pauseOverlay.addChild(easyFontAtlas, 3,1);



		CCMenuItemImage startbtn = CCMenuItemImage.item("startbutton.png", "startbutton.png",this, "startCallback");
		startbtn.setScale(0.8f * tilescalefactor);

		CCMenu startmenu = CCMenu.menu(startbtn); 
		startmenu.setPosition(CGPoint.make(0, 0));
		startbtn.setPosition(CGPoint.make(screenSize.width - startbtn.getContentSize().width/2*tilescalefactor, startbtn.getContentSize().height*tilescalefactor/2));

		pauseOverlay.addChild(startmenu, 3,1);

		gamepaused = true ;




		NUM_ROWS = 3 ;
		NUM_COLUMNS = 3 ;

		generateTiles();

		if( player == 1){
			setCurrentPlayer(State.PLAYER1) ;
		}else{
			setCurrentPlayer(State.PLAYER2) ;
		}





	}


	public void startCallback(Object sender) {
		//SoundEngine.sharedEngine().playEffect(appcontext, R.raw.cheer);

		gamepaused = false ; 
		thetime = 0 ;
		moves = 0 ;
		schedule("updateTimeLabel", 1.0f);

		selectTurn(getCurrentPlayer());

		if (getCurrentPlayer() == State.PLAYER2) {
			ComputerPlays() ;
		}
		if (getCurrentPlayer() == State.WIN) {
			setWinState(getWinner());
		}

		//Add Pause Sprite Button
		CCMenuItemImage pausebtn = CCMenuItemImage.item("pause.png", "pause.png",this, "pauseCallback");
		pausebtn.setScale(0.9f*tilescalefactor);
		CCMenu pausemenu = CCMenu.menu();
		pausemenu.addChild(pausebtn,1,1);
		pausemenu.setContentSize(pausebtn.getContentSize());
		//pausemenu.setScale(tilescalefactor);
		pausemenu.setPosition(CGPoint.make(screenSize.width - pausemenu.getContentSize().width*0.9f*tilescalefactor/2.0f , pausemenu.getContentSize().height*tilescalefactor/2));
		addChild(pausemenu, 100,PAUSE_MENU_TAG);

		CCColorLayer pauselayer = (CCColorLayer) getChildByTag(PAUSE_OVERLAY_TAG) ;
		pauselayer.runAction(CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f, screenSize.height + pauselayer.getContentSize().height*tilescalefactor)));
		pauselayer.removeAllChildren(true);
		pauselayer.removeSelf() ;
	}

	public void generateTiles(){
		//tileNumbers ;
		//tileNumbers ;
		tileIndex = 0 ;
		//Generate the random but solvable number series
		tileNumbers = Utility.byteArrayToIntArray(Utility.getRandomArray((NUM_ROWS * NUM_COLUMNS), false));			//Create 12 Boxes and arrange ont the screenMenu Items and add to layer
		int nextval ;

		CCNode tilesNode = CCNode.node();
		tilesNode.setTag(TILE_NODE_TAG);
		addChild(tilesNode);

		
		//TILE_SQUARE_SIZE = (int) ((screenSize.height  *tilescalefactor)/NUM_ROWS) ;
		int useablewidth = (int) (screenSize.width - movesLabel.getContentSize().width*tilescalefactor ) ;
		useableheight =  (int) (screenSize.height  - 30*tilescalefactor) ;
		
		TILE_SQUARE_SIZE = (int) Math.min((useableheight/NUM_ROWS) , (useablewidth/NUM_COLUMNS)) ;
		
		toppoint = (int) (useableheight - (TILE_SQUARE_SIZE / 2) + 15*tilescalefactor)   ;
		scalefactor  = TILE_SQUARE_SIZE / 150.0f ;

		topleft = (int) ((TILE_SQUARE_SIZE / 2) + 15*tilescalefactor) ;
		

		for (int j = toppoint ; j > toppoint - (TILE_SQUARE_SIZE * NUM_ROWS); j-= TILE_SQUARE_SIZE){
			for (int i = topleft ; i < (topleft - 5) + (TILE_SQUARE_SIZE * NUM_COLUMNS); i+= TILE_SQUARE_SIZE){

				if (tileIndex >= (NUM_ROWS * NUM_COLUMNS)) {
					break ;
				}
				nextval = tileIndex ;
				//Create a fontatlas, put it in an item, put it in a menu
				//Put the item in a Node and animate the Node.
				CCMenuItemImage eachItem= CCMenuItemImage.item("tictile.png", "tictile.png",this, "tileClickCallback");
				eachItem.setContentSize(eachItem.getContentSize().width  , eachItem.getContentSize().height);
				CCMenu menu = CCMenu.menu() ;
				menu.setContentSize(eachItem.getContentSize()) ;
				//Put the an item in Menu 
				menu.setPosition(0,0);
				menu.addChild(eachItem,1,1);


				CCNodeExt eachNode =  new  CCNodeExt();
				eachNode.setContentSize(eachItem.getContentSize());
				eachNode.setScale(scalefactor);
				eachNode.addChild(menu,1,1);
				eachNode.setPosition(i, j);
				eachNode.setNodeText(nextval + "");

				eachNode.setNodeNum(nextval);

				 
				tilesNode.addChild(eachNode,1,nextval);
				/*if( nextval != 0){

				}else {
					emptyPosition = CGPoint.ccp(i, j);
				}*/

				//Add each Node to a HashMap to note its location
				tileIndex++;
			}
		} 



	}

	public void updateTimeLabel(float dt) {
		thetime += 1;
		String string = CCFormatter.format("%02d:%02d", (int)(thetime /60) , (int)thetime % 60 );
		CCBitmapFontAtlas timerLabel = (CCBitmapFontAtlas) getChildByTag(TIMER_LABEL_TAG) ;
		timerLabel.setString(string);

	}

	public void handleWin(Object sender){
		gameover = true ;
		gamepaused = true ;

		WinCallback("");
	}



	public void pauseCallback(Object sender) {

		unschedule("updateTimeLabel");
		timehold  = thetime ;
		gamepaused = true ;

		CCMenu pausemenu = (CCMenu) getChildByTag(PAUSE_MENU_TAG);
		pausemenu.setVisible(false);

		CCColorLayer pauseOverlay = CCColorLayer.node(ccColor4B.ccc4(25, 25, 25, 255));
		pauseOverlay.setIsTouchEnabled(true);
		pauseOverlay.setOpacity(180);
		addChild(pauseOverlay,200,PAUSE_OVERLAY_TAG);




		//Add scroll to the Layer
		//Add Top Scroll
		CCSprite topscroll = CCSprite.sprite("darktranstop.png");
		float picscale = screenSize.width/topscroll.getContentSize().width ;
		topscroll.setScale(picscale);
		topscroll.setPosition(CGPoint.ccp(screenSize.width / 2.0f, screenSize.height + topscroll.getContentSize().height*tilescalefactor));
		pauseOverlay.addChild(topscroll,1,SCROLL_TOP_TAG);
		topscroll.runAction(CCSequence.actions(
				CCDelayTime.action(0.4f),
				CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f,screenSize.height - (topscroll.getContentSize().height * picscale)/2)), 
				CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f,screenSize.height - (topscroll.getContentSize().height * picscale)/2 + 20 * picscale )) 
				));


		CCBitmapFontAtlas gamepaused = CCBitmapFontAtlas.bitmapFontAtlas ("Game Paused!", "dalek.fnt");
		gamepaused.setColor(ccColor3B.ccc3(105, 75, 41));
		gamepaused.setScale( GidiGamesActivity.scalefactor + 0.4f * GidiGamesActivity.scalefactor);
		gamepaused.setAnchorPoint(1f,1f);
		gamepaused.setPosition(CGPoint.ccp(screenSize.width + gamepaused.getContentSize().width + 40 , screenSize.height -20 ));
		pauseOverlay.addChild(gamepaused,300,GAME_PUASES_LABEL_TAG);
		gamepaused.runAction(CCSequence.actions(
				CCDelayTime.action(0.5f),
				CCMoveTo.action(0.2f, CGPoint.make(screenSize.width - 20.0f * picscale, screenSize.height - 25 * picscale )) 
				));


		pauseOverlay.addChild(gamepaused,300,GAME_PUASES_LABEL_TAG);

		CCBitmapFontAtlas gamepausedmain = CCBitmapFontAtlas.bitmapFontAtlas ( "Game Paused.", "bionic.fnt");
		gamepausedmain.setAnchorPoint(CGPoint.ccp(0,1));
		//gamemoves.setColor(ccColor3B.ccc3(105, 75, 41));
		gamepausedmain.setScale(tilescalefactor);
		gamepausedmain.setAnchorPoint(0.5f,1f);

		gamepausedmain.setPosition(CGPoint.ccp(screenSize.width / 2.0f , screenSize.height/2.0f ));
		pauseOverlay.addChild(gamepausedmain,300,GAME_PUASES_LABEL_TAG);
		gamepausedmain.runAction(CCSequence.actions(
				CCDelayTime.action(0.1f),
				CCScaleBy.action(0.2f, 2.0f)
				));




		// Add Pause Menu Buttons
		CCMenuItemImage playbtn = CCMenuItemImage.item("play.png", "play.png",this, "playCallback");
		playbtn.setScale(0.9f * tilescalefactor);
		//CCMenuItemImage refreshbtn = CCMenuItemImage.item("refresh.png", "refresh.png",this, "refreshCallback");
		CCMenuItemImage menubtn = CCMenuItemImage.item("menu.png", "menu.png",this, "menuCallback");
		menubtn.setScale(0.9f*tilescalefactor);

		CCMenu resumemenu = CCMenu.menu(playbtn, menubtn); 
		resumemenu.setPosition(CGPoint.make(0, 0));
		menubtn.setPosition(CGPoint.make(screenSize.width - menubtn.getContentSize().width* tilescalefactor /2  , menubtn.getContentSize().height*tilescalefactor/2));
		//refreshbtn.setPosition(CGPoint.make(screenSize.width - refreshbtn.getContentSize().width - menubtn.getContentSize().width - 15, refreshbtn.getContentSize().height));
		playbtn.setPosition(CGPoint.make(screenSize.width -  playbtn.getContentSize().width*tilescalefactor - menubtn.getContentSize().width*tilescalefactor/2.0f -10*tilescalefactor , menubtn.getContentSize().height*tilescalefactor/2));

		//resumemenu.setPosition(CGPoint.make(screenSize.width - resumemenu.getContentSize().width - 50, resumemenu.getContentSize().height));
		pauseOverlay.addChild(resumemenu, 2,RESUME_MENU_TAG) ;


	}


	public void WinCallback(String toptext) {
		//schedule("updateTimeLabel");

		CCMenu pausemenu = (CCMenu) getChildByTag(PAUSE_MENU_TAG);
		pausemenu.setVisible(false);

		CCColorLayer pauseOverlay = CCColorLayer.node(ccColor4B.ccc4(25, 25, 25, 255));
		pauseOverlay.setOpacity(200);
		pauseOverlay.setIsTouchEnabled(true); 
		addChild(pauseOverlay,200,PAUSE_OVERLAY_TAG);

		if(toptext.equals(GAME_OUTPUT_YOU)){
			SoundEngine.sharedEngine().playEffect(appcontext, R.raw.cheer);
			//mDbHelper.createItem("tictactoe", "", String.valueOf(moves),  thetime);
			mDbHelper.open() ;
			mDbHelper.createScoreItem("tictactoe", "", "", moves, thetime);
			mDbHelper.close();
			emitter = new CCQuadParticleSystem(50);
			emitter.setScale(tilescalefactor);
			emitter.setPosition(CGPoint.ccp(screenSize.width, screenSize.height));
			pauseOverlay.addChild(emitter, 100);
			emitter.setTexture(CCTextureCache.sharedTextureCache().addImage("stars_grayscale.png"));

			// duration
			emitter.setDuration(CCParticleSystem.kCCParticleDurationInfinity);

			// Gravity Mode: gravity
			emitter.setGravity(CGPoint.zero());

			// Set "Gravity" mode (default one)
			emitter.setEmitterMode(CCParticleSystem.kCCParticleModeGravity);

			// Gravity Mode: speed of particles
			emitter.setSpeed(160);
			emitter.setSpeedVar(20);

			// Gravity Mode: radial
			emitter.setRadialAccel(-120);
			emitter.setRadialAccelVar(0);

			// Gravity Mode: tagential
			emitter.setTangentialAccel(30);
			emitter.setTangentialAccelVar(0);

			// angle
			emitter.setAngle(90);
			emitter.setAngleVar(360);

			// emitter position
			emitter.setPosition(CGPoint.ccp(160,240));
			emitter.setPosVar(CGPoint.zero());

			// life of particles
			emitter.setLife(4);
			emitter.setLifeVar(1);

			// spin of particles
			emitter.setStartSpin(0);
			emitter.setStartSpinVar(0);
			emitter.setEndSpin(0);
			emitter.setEndSpinVar(0);

			// color of particles
			ccColor4F startColor = new ccColor4F(0.5f, 0.5f, 0.5f, 1.0f);
			emitter.setStartColor(startColor);

			ccColor4F startColorVar = new ccColor4F(0.5f, 0.5f, 0.5f, 1.0f);
			emitter.setStartColorVar(startColorVar);

			ccColor4F endColor = new ccColor4F(0.1f, 0.1f, 0.1f, 0.2f);
			emitter.setEndColor(endColor);

			ccColor4F endColorVar = new ccColor4F(0.1f, 0.1f, 0.1f, 0.2f);	
			emitter.setEndColorVar(endColorVar);

			// size, in pixels
			emitter.setStartSize(80.0f);
			emitter.setStartSizeVar(40.0f);
			emitter.setEndSize(CCParticleSystem.kCCParticleStartSizeEqualToEndSize);

			// emits per second
			emitter.setEmissionRate( emitter.getTotalParticles()/emitter.getLife());

			// additive
			emitter.setBlendAdditive(true);
		}



		//Add scroll to the Layer
				//Add Top Scroll
				CCSprite topscroll = CCSprite.sprite("darktranstop.png");
				float picscale = screenSize.width/topscroll.getContentSize().width ;
				topscroll.setScale(picscale);
				topscroll.setPosition(CGPoint.ccp(screenSize.width / 2.0f, screenSize.height + topscroll.getContentSize().height));
				pauseOverlay.addChild(topscroll,1,SCROLL_TOP_TAG);
				topscroll.runAction(CCSequence.actions(
						CCDelayTime.action(0.4f),
						CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f,screenSize.height - (topscroll.getContentSize().height * picscale)/2)), 
						CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f,screenSize.height - (topscroll.getContentSize().height * picscale)/2 + 20 * picscale )) 
						));


		CCBitmapFontAtlas gamepaused = CCBitmapFontAtlas.bitmapFontAtlas (toptext, "dalek.fnt");
		gamepaused.setColor(ccColor3B.ccc3(105, 75, 41));
		gamepaused.setScale( GidiGamesActivity.scalefactor + 0.4f * GidiGamesActivity.scalefactor);
		gamepaused.setAnchorPoint(1f,1f);
		gamepaused.setPosition(CGPoint.ccp(screenSize.width + gamepaused.getContentSize().width + 40 , screenSize.height -20 ));
		pauseOverlay.addChild(gamepaused,300,GAME_PUASES_LABEL_TAG);
		gamepaused.runAction(CCSequence.actions(
				CCDelayTime.action(0.5f),
				CCMoveTo.action(0.2f, CGPoint.make(screenSize.width - 20.0f * picscale, screenSize.height - 25 * picscale )) 
				));

		pauseOverlay.addChild(gamepaused,300,GAME_PUASES_LABEL_TAG);
		

		CCBitmapFontAtlas gamemoves = CCBitmapFontAtlas.bitmapFontAtlas ( CCFormatter.format("%02d", moves ) + " Moves", "bionic.fnt");
		gamemoves.setAnchorPoint(CGPoint.ccp(0,1));
		gamemoves.setAnchorPoint(CGPoint.ccp(0,1));
		//gamemoves.setColor(ccColor3B.ccc3(105, 75, 41));
		gamemoves.setScale(tilescalefactor);
		gamemoves.setAnchorPoint(0.5f,1f);

		gamemoves.setPosition(CGPoint.ccp(screenSize.width / 2.0f , screenSize.height/2.0f ));
		pauseOverlay.addChild(gamemoves,300,GAME_PUASES_LABEL_TAG);
		gamemoves.runAction(CCSequence.actions(
				CCDelayTime.action(0.5f),
				CCScaleBy.action(0.2f, 2.0f)
				));
		
		
		CCBitmapFontAtlas instructionFontAtlas = CCBitmapFontAtlas.bitmapFontAtlas( "TAP Menu button below to continue!" , "bionic.fnt");
		instructionFontAtlas.setPosition(gamemoves.getPosition().x, -instructionFontAtlas.getContentSize().height*0.6f*tilescalefactor) ;
		instructionFontAtlas.setScale(0.6f*tilescalefactor) ;
		pauseOverlay.addChild(instructionFontAtlas,301);

		instructionFontAtlas.runAction(CCSequence.actions(
				CCDelayTime.action(0.5f), 
				CCMoveTo.action(0.5f, CGPoint.make(gamemoves.getPosition().x, gamemoves.getPosition().y - 10*tilescalefactor - gamemoves.getContentSize().height *tilescalefactor * 2.0f)) 
				));

		CCBitmapFontAtlas gametime = CCBitmapFontAtlas.bitmapFontAtlas (CCFormatter.format("%02d:%02d", (int)(thetime /60) , (int)thetime % 60 ), "bionic.fnt");
		gametime.setAnchorPoint(CGPoint.ccp(0,1));
		//gamemoves.setColor(ccColor3B.ccc3(105, 75, 41));
		gametime.setScale(tilescalefactor);

		gametime.setAnchorPoint(0.5f,1f);
		gametime.setPosition(CGPoint.ccp(screenSize.width / 2.0f , gamemoves.getPosition().y + gamemoves.getContentSize().height*tilescalefactor/2.0f + 10 ));
		pauseOverlay.addChild(gametime,301);


		// Add Pause Menu Buttons
		//CCMenuItemImage playbtn = CCMenuItemImage.item("play.png", "play.png",this, "playCallback");
		//CCMenuItemImage refreshbtn = CCMenuItemImage.item("refresh.png", "refresh.png",this, "refreshCallback");
		CCMenuItemImage menubtn = CCMenuItemImage.item("menu.png", "menu.png",this, "menuCallback");
		menubtn.setScale(0.9f*tilescalefactor);

		CCMenu resumemenu = CCMenu.menu(menubtn); 
		resumemenu.setPosition(CGPoint.make(0, 0));
		menubtn.setPosition(CGPoint.make(screenSize.width - menubtn.getContentSize().width*tilescalefactor/2.0f , menubtn.getContentSize().height*tilescalefactor/2.0f));
		//refreshbtn.setPosition(CGPoint.make(screenSize.width - refreshbtn.getContentSize().width - menubtn.getContentSize().width - 15, refreshbtn.getContentSize().height));
		//cplaybtn.setPosition(CGPoint.make(screenSize.width -  playbtn.getContentSize().width  - 20, playbtn.getContentSize().height));

		//resumemenu.setPosition(CGPoint.make(screenSize.width - resumemenu.getContentSize().width - 50, resumemenu.getContentSize().height));
		pauseOverlay.addChild(resumemenu, 2,RESUME_MENU_TAG) ;
	}


	public void removePauseMenu(Object theSprite) {
		CCColorLayer pauselayer = (CCColorLayer) getChildByTag(PAUSE_OVERLAY_TAG) ;
		CCMenu resumemenu = (CCMenu) pauselayer.getChildByTag(RESUME_MENU_TAG);
		resumemenu.setVisible(false);
		CCMenu pausemenu = (CCMenu) getChildByTag(PAUSE_MENU_TAG);
		pausemenu.setVisible(true);

		pauselayer.runAction(CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f, screenSize.height + pauselayer.getContentSize().height*tilescalefactor)));

		pauselayer.removeAllChildren(true);
		pauselayer.removeSelf() ;
	}
	public void playCallback(Object theSprite){
		gamepaused = false ;
		schedule("updateTimeLabel", 1.0f);

		CCColorLayer pauselayer = (CCColorLayer) getChildByTag(PAUSE_OVERLAY_TAG) ;

		CCBitmapFontAtlas gamepaused = (CCBitmapFontAtlas)  pauselayer.getChildByTag(GAME_PUASES_LABEL_TAG) ;
		gamepaused.runAction(CCMoveTo.action(0.3f, CGPoint.make(screenSize.width + gamepaused.getContentSize().width*tilescalefactor, gamepaused.getPosition().y)));


		CCSprite topscroll = (CCSprite) pauselayer .getChildByTag(SCROLL_TOP_TAG);
		topscroll.runAction(CCSequence.actions(
				CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f,screenSize.height - topscroll.getContentSize().height*tilescalefactor/2 - 20*tilescalefactor)),
				CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f, screenSize.height + topscroll.getContentSize().height*tilescalefactor)),
				CCCallFuncN.action(this, "removePauseMenu")
				));



	}

	public void loadPuzzleMenu(Object theSprite) {
		CCColorLayer pauselayer = (CCColorLayer) getChildByTag(PAUSE_OVERLAY_TAG) ;
		CCMenu resumemenu = (CCMenu) pauselayer.getChildByTag(RESUME_MENU_TAG);
		resumemenu.setVisible(false);
		CCMenu pausemenu = (CCMenu) getChildByTag(PAUSE_MENU_TAG);
		pausemenu.setVisible(true);

		pauselayer.runAction(CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f, screenSize.height + pauselayer.getContentSize().height*tilescalefactor)));

		pauselayer.removeAllChildren(true);
		pauselayer.removeSelf() ;
		CCDirector.sharedDirector().replaceScene(TicTacMenuLayer.scene());
	}
	public void menuCallback(Object theSprite){
		unschedule("updateTimeLabel");
		CCColorLayer pauselayer = (CCColorLayer) getChildByTag(PAUSE_OVERLAY_TAG) ;

		CCBitmapFontAtlas gamepaused = (CCBitmapFontAtlas)  pauselayer.getChildByTag(GAME_PUASES_LABEL_TAG) ;
		gamepaused.runAction(CCMoveTo.action(0.3f, CGPoint.make(screenSize.width + gamepaused.getContentSize().width*tilescalefactor, gamepaused.getPosition().y)));

		CCSprite topscroll = (CCSprite) pauselayer .getChildByTag(SCROLL_TOP_TAG);
		topscroll.runAction(CCSequence.actions(
				CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f,screenSize.height - topscroll.getContentSize().height*tilescalefactor/2 - 20*tilescalefactor)),
				CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f, screenSize.height + topscroll.getContentSize().height*tilescalefactor)),
				CCCallFuncN.action(this, "loadPuzzleMenu")
				));


	}




	/* 
	 * Handles Each tile Click Call back
	 * 
	 * */
	public void tileClickCallback(Object sender) {
		CCMenuItemImage item = (CCMenuItemImage) sender ;
		CCNodeExt thenode = (CCNodeExt) item.getParent().getParent() ;
		CCBitmapFontAtlas scorelabel = (CCBitmapFontAtlas) getChildByTag(SCORE_LABEL_TAG);
		CCBitmapFontAtlas moveslabel = (CCBitmapFontAtlas) getChildByTag(MOVES_LABEL_TAG);

		//CGPoint nodePosition = thenode.getPosition();

		if( !thenode.getNodeText().equals("locked") && !gameover && !gamepaused && boardplayable == true ){
			mData[thenode.getNodeNum()] = getCurrentPlayer() ;
			moves++ ;
			moveslabel.setString("Moves : " + CCFormatter.format("%03d", moves ));
			thenode.setNodeText("locked") ;
			scorelabel.setString( "Tile " + thenode.getNodeNum() + " Now Played "  );
			CCSprite lexis = CCSprite.sprite("lib_circle.png"); 
			lexis.setScale(thenode.getContentSize().height/lexis.getContentSize().height);
			thenode.addChild(lexis,2) ;

			if( !checkGameFinished(getCurrentPlayer())){
				finishTurn();
			}

		}else{

			//scorelabel.setString( "Tile " + thenode.getNodeText() + " Already Locked/Set "  );
		}


	}

	public void setCell(int cellIndex, State value){
		mData[cellIndex] = value;

		//CCNodeExt thenode = (CCNodeExt) item.getParent().getParent() ;
		CCNode tilesNode = (CCNode) getChildByTag(TILE_NODE_TAG) ;
		CCNodeExt thenode = (CCNodeExt) tilesNode.getChildByTag(cellIndex) ;
		CCBitmapFontAtlas scorelabel = (CCBitmapFontAtlas) getChildByTag(SCORE_LABEL_TAG);


		//CGPoint nodePosition = thenode.getPosition();

		if( !thenode.getNodeText().equals("locked")){
			thenode.setNodeText("locked") ;
			scorelabel.setString( "Tile " + thenode.getNodeText() + " Now Played "  );
			CCSprite lexis = CCSprite.sprite("lib_cross.png"); 
			lexis.setScale(thenode.getContentSize().height/lexis.getContentSize().height);
			thenode.addChild(lexis,4) ;
		}else{
			scorelabel.setString( "Tile " + thenode.getNodeText() + " Already Locked/Set "  );
		}


	}


	public void ComputerPlaysMain(Object theSprite){
		State[] data =  mData ; // mGameView.getData();
		if(checkPlayerThreat() == -1){
			// Pick a non-used cell at random. That's about all the AI you need for this game.

			int used = 0;

			while (used != 0x1F) {
				int index = mRnd.nextInt(9);
				if (((used >> index) & 1) == 0) {
					used |= 1 << index;
					if (data[index] == State.EMPTY) {

						// mData[index] = getCurrentPlayer();
						setCell(index, getCurrentPlayer());
						break;
					}
				}
			}

		}else{
			int index = checkPlayerThreat() ;
			if (data[index] == State.EMPTY) {
				//mData[index] = getCurrentPlayer();
				setCell(index, getCurrentPlayer());

			}

		}
		finishTurn(); 



	}
	public void ComputerPlays(){

		runAction(CCSequence.actions(
				CCDelayTime.action(COMPUTER_DELAY_MS),
				CCCallFuncN.action(this, "ComputerPlaysMain")
				));

	}


	private int checkPlayerThreat(){

		int position = -1 ;
		State[] data = mData;
		// check rows of opponent
		for (int j = 0, k = 0; j < 3; j++, k += 3) {
			if ((data[k] == State.PLAYER1 && data[k] == data[k+1] && data[k+2] == State.EMPTY)  ) {
				position = k + 2 ;
			} 

			if ((data[k+1] == State.PLAYER1 && data[k+1] == data[k+2] && data[k] == State.EMPTY)  ) {
				position = k ; 
			}

			if ( (data[k] == State.PLAYER1 && data[k] == data[k+2] && data[k+1] == State.EMPTY) ) {
				position = k + 1 ; 
			}

			if (data[k] == State.PLAYER2 && data[k] == data[k+1] && data[k+2] == State.EMPTY ){
				position  = k + 2 ;
				return position ;
			}
			if ((data[k+1] == State.PLAYER2 && data[k+1] == data[k+2] && data[k] == State.EMPTY)  ) {
				position = k ; 
				return position ;
			}

			if ( (data[k] == State.PLAYER2 && data[k] == data[k+2] && data[k+1] == State.EMPTY) ) {
				position = k + 1 ; 
				return position ;
			}
		}

		//check columns of opponent
		for (int i = 0; i < 3; i++) {
			if (data[i] == State.PLAYER1 && data[i] == data[i+3] && data[i+6] == State.EMPTY) {
				position = i + 6;
			}

			if (data[i+3] == State.PLAYER1 && data[i+3] == data[i+6] && data[i] == State.EMPTY) {
				position = i ;
			}
			if (data[i] == State.PLAYER1 && data[i] == data[i+6] && data[i+3] == State.EMPTY) {
				position = i + 3;
			}

			if (data[i] == State.PLAYER2 && data[i] == data[i+3] && data[i+6] == State.EMPTY) {
				position = i + 6;
				return position ;
			}

			if (data[i+3] == State.PLAYER2 && data[i+3] == data[i+6] && data[i] == State.EMPTY) {
				position = i ;
				return position ;
			}
			if (data[i] == State.PLAYER2 && data[i] == data[i+6] && data[i+3] == State.EMPTY) {
				position = i + 3;
				return position ;
			}
		}

		//Check diagonals
		if (data[0] == State.PLAYER1 && data[0] == data[4] && data[8] == State.EMPTY) {
			position = 8;
		}  if (data[2] == State.PLAYER1 && data[2] == data[4] && data[6] == State.EMPTY) {
			position = 6;
		}

		if (data[4] == State.PLAYER1 && data[4] == data[8] && data[0] == State.EMPTY) {
			position = 0;
		}   if (data[4] == State.PLAYER1 && data[4] == data[6] && data[2] == State.EMPTY) {
			position = 2;
		}

		if (data[0] == State.PLAYER1 && data[0] == data[8] && data[4] == State.EMPTY) {
			position = 4;
		}   if (data[2] == State.PLAYER1 && data[2] == data[6] && data[4] == State.EMPTY) {
			position = 4;
		}



		if (data[0] == State.PLAYER2 && data[0] == data[4] && data[8] == State.EMPTY) {
			position = 8;
			return position ;
		}  if (data[2] == State.PLAYER2 && data[2] == data[4] && data[6] == State.EMPTY) {
			position = 6;
			return position ;
		}

		if (data[4] == State.PLAYER2 && data[4] == data[8] && data[0] == State.EMPTY) {
			position = 0;
			return position ;
		}   if (data[4] == State.PLAYER2 && data[4] == data[6] && data[6] == State.EMPTY) {
			position = 2;return position ;
		}

		if (data[0] == State.PLAYER2 && data[0] == data[8] && data[4] == State.EMPTY) {
			position = 4;
			return position ;
		}   if (data[2] == State.PLAYER2 && data[2] == data[6] && data[4] == State.EMPTY) {
			position = 4;
			return position ;
		}
		return position ;
	}



	private State getOtherPlayer(State player) {
		return player == State.PLAYER1 ? State.PLAYER2 : State.PLAYER1;
	}

	private void finishTurn() {
		State player =  getCurrentPlayer() ; // mGameView.getCurrentPlayer();
		if (!checkGameFinished(player)) {
			player = selectTurn(getOtherPlayer(player));
			if (player == State.PLAYER2) {
				ComputerPlays();
				//mHandler.sendEmptyMessageDelayed(MSG_COMPUTER_TURN, COMPUTER_DELAY_MS);
			}
		}
	}

	public boolean checkGameFinished(State player) {
		State[] data = mData;
		boolean full = true;

		int col = -1;
		int row = -1;
		int diag = -1;

		// check rows
		for (int j = 0, k = 0; j < 3; j++, k += 3) {
			if (data[k] != State.EMPTY && data[k] == data[k+1] && data[k] == data[k+2]) {
				row = j;
			}
			if (full && (data[k] == State.EMPTY ||
					data[k+1] == State.EMPTY ||
					data[k+2] == State.EMPTY)) {
				full = false;
			}
		}

		// check columns
		for (int i = 0; i < 3; i++) {
			if (data[i] != State.EMPTY && data[i] == data[i+3] && data[i] == data[i+6]) {
				col = i;
			}
		}

		// check diagonals
		if (data[0] != State.EMPTY && data[0] == data[1+3] && data[0] == data[2+6]) {
			diag = 0;
		} else  if (data[2] != State.EMPTY && data[2] == data[1+3] && data[2] == data[0+6]) {
			diag = 1;
		}

		if (col != -1 || row != -1 || diag != -1) {
			setFinished(player, col, row, diag);
			return true;
		}

		// if we get here, there's no winner but the board is full.
		if (full) {
			setFinished(State.EMPTY, -1, -1, -1);
			return true;
		}
		return false;
	}


	private void setFinished(State player, int col, int row, int diagonal) {
		setCurrentPlayer(State.WIN);
		setWinState(player);
	}


	private void setWinState(State player) {
		gameover = true ;
		String finaltext ;

		if (player == State.EMPTY) {
			finaltext = GAME_OUTPUT_TIE ; 
		} else if (player == State.PLAYER1) {
			finaltext = GAME_OUTPUT_YOU;
		} else {
			finaltext = GAME_OUTPUT_COMPUTER;
		}
		WinCallback(finaltext) ;
	}

	//Select Turn
	private State selectTurn(State player) {
		setCurrentPlayer(player) ;
		/*mGameView.setCurrentPlayer(player);
				mButtonNext.setEnabled(false);
		 */CCBitmapFontAtlas scoreLabel = (CCBitmapFontAtlas) getChildByTag(SCORE_LABEL_TAG);
		 if (player == State.PLAYER1) {

			 scoreLabel.setString(PLAYER_1_TURN);
			 /*mInfoView.setText(R.string.player1_turn);
					mGameView.setEnabled(true);
			  */
			 boardplayable = true ;
		 } else if (player == State.PLAYER2) {
			 scoreLabel.setString(PLAYER_2_TURN);
			 /*mInfoView.setText(R.string.player2_turn);
					mGameView.setEnabled(false);*/
			 boardplayable = false ;
		 }

		 return player;
	}


	public enum State {
		UNKNOWN(-3),
		WIN(-2),
		EMPTY(0),
		PLAYER1(1),
		PLAYER2(2);

		private int mValue;

		private State(int value) {
			mValue = value;
		}

		public int getValue() {
			return mValue;
		}

		public static State fromInt(int i) {
			for (State s : values()) {
				if (s.getValue() == i) {
					return s;
				}
			}
			return EMPTY;
		}
	}

	public static State getCurrentPlayer() {
		return mCurrentPlayer;
	}

	public static void setCurrentPlayer(State player) {
		mCurrentPlayer = player;

	}

	public State getWinner() {
		return mWinner;
	}

	public void setWinner(State winner) {
		mWinner = winner;
	}
}





















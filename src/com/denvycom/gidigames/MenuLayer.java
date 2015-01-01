package com.denvycom.gidigames;


import java.util.ArrayList;
import org.cocos2d.actions.interval.CCDelayTime;
import org.cocos2d.actions.interval.CCFadeIn;
import org.cocos2d.actions.interval.CCFadeOut;
import org.cocos2d.actions.interval.CCMoveTo;
import org.cocos2d.actions.interval.CCSequence;
import org.cocos2d.config.ccMacros;
import org.cocos2d.events.CCTouchDispatcher;
import org.cocos2d.extensions.scroll.CCScrollView;
import org.cocos2d.extensions.scroll.CCTableView;
import org.cocos2d.extensions.scroll.CCTableViewBitMapFontCell;
import org.cocos2d.extensions.scroll.CCTableViewCell;
import org.cocos2d.extensions.scroll.CCTableViewDataSource;
import org.cocos2d.extensions.scroll.CCTableViewDelegate;
import org.cocos2d.extensions.scroll.CCTableViewSpriteCell;
import org.cocos2d.layers.CCColorLayer;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.opengl.CCBitmapFontAtlas;
import org.cocos2d.opengl.CCTextureAtlas;
import org.cocos2d.transitions.CCSlideInLTransition;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGRect;
import org.cocos2d.types.CGSize;
import org.cocos2d.types.ccColor3B;
import org.cocos2d.types.ccColor4B;
 
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.view.KeyEvent;
import android.view.MotionEvent;





public class MenuLayer extends CCColorLayer implements CCTableViewDelegate, CCTableViewDataSource {

	private static final int MAIN_WORD_NODE_LABEL_TAG = 1; 
	CCTextureAtlas textureAtlas;
	CCColorLayer backgroundLayer;
	CCScrollView scrollView;
	ArrayList<Object> contents;
	float tilescalefactor = 0.0f ;
	 
	CGPoint locationbegan = new CGPoint() ;
	
	   private CGSize cellSize_;

   	private CCTableView tableView_;
   	private ArrayList<String> elements_ = new ArrayList<String>();
	private ArrayList<String> elementssprites_ = new ArrayList<String>();


	public static CCScene scene()
	{
	    GidiGamesActivity.nextscene = "" ;
	    GidiGamesActivity.currentscene = "MenuLayer" ;
		CCScene scene = CCScene.node();
		CCLayer layer = new MenuLayer();
		scene.addChild(layer);

		return scene;
	}


	protected MenuLayer()
	{
		super(ccColor4B.ccc4(0, 0, 0, 0));	

		CGSize screenSize = CCDirector.sharedDirector().winSize();

		//this.setIsTouchEnabled(true);
         
	    tilescalefactor = (screenSize.height/370) ;


		//SoundEngine.sharedEngine().playSound(context, R.raw.jungle, true);

		//Add Background Sprite Image 
		CCSprite background = CCSprite.sprite("background.jpg");
		background.setScale(screenSize.width / background.getContentSize().width);
		background.setAnchorPoint(CGPoint.ccp(0f,1f)) ;
		background.setPosition(CGPoint.ccp(0, screenSize.height));
		background.setColor(ccColor3B.ccc3(230, 230, 230));
		addChild(background,1); 



		//Add Top Scroll
		CCSprite topscroll = CCSprite.sprite("darktranstop.png");
		float picscale = screenSize.width/topscroll.getContentSize().width ;
		topscroll.setScale(picscale);
		topscroll.setPosition(CGPoint.ccp(screenSize.width / 2.0f, screenSize.height + topscroll.getContentSize().height));
		addChild(topscroll,5);
		topscroll.runAction(CCSequence.actions(
				CCDelayTime.action(0.4f),
				CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f,screenSize.height - (topscroll.getContentSize().height * picscale)/2)), 
				CCMoveTo.action(0.2f, CGPoint.make(screenSize.width / 2.0f,screenSize.height - (topscroll.getContentSize().height * picscale)/2 + 20 * picscale )) 
				));


		CCSprite arrowright = CCSprite.sprite("arrowright.png");
		arrowright.setScale(tilescalefactor);
		arrowright.setPosition(CGPoint.ccp(screenSize.width - arrowright.getContentSize().width*tilescalefactor/2 - 9.5f*tilescalefactor, screenSize.height/2));
		addChild(arrowright,50);
		CCSprite arrowleft = CCSprite.sprite("arrowleft.png");
		arrowleft.setScale(tilescalefactor);
		arrowleft.setPosition(CGPoint.ccp( arrowleft.getContentSize().width*tilescalefactor/2 + 9.5f*tilescalefactor, screenSize.height/2));
		addChild(arrowleft,50);
  
		// Add Title
		CCBitmapFontAtlas selectgame = CCBitmapFontAtlas.bitmapFontAtlas ("GIDIGAMES - Select a TITLE!", "dalek.fnt");
		//selectgame.setAnchorPoint(CGPoint.ccp(0,1));
		selectgame.setColor(ccColor3B.ccc3(105, 75, 41));
		selectgame.setScale( GidiGamesActivity.scalefactor + 0.4f * GidiGamesActivity.scalefactor);
		selectgame.setAnchorPoint(1f,1f);
		selectgame.setPosition(CGPoint.ccp(screenSize.width + selectgame.getContentSize().width + 40 , screenSize.height -20 ));
		addChild(selectgame,6,MAIN_WORD_NODE_LABEL_TAG);
		selectgame.runAction(CCSequence.actions(
				CCDelayTime.action(0.5f),
				CCMoveTo.action(0.2f, CGPoint.make(screenSize.width - 20.0f * picscale, screenSize.height - 25 * picscale )) 
				));


		scrollView = CCScrollView.view(CGSize.zero());
		contents = new ArrayList<Object>();
 
		float scrollboxheight = (topscroll.getContentSize().height * picscale) - (30 * picscale) ;

		 
		cellSize_ = CGSize.make(280 * tilescalefactor, 172 * tilescalefactor);
	    setIsTouchEnabled(true);
	     
	    	        
	        elementssprites_.add("lexisbox.png"); 
			elementssprites_.add("puzzlebox.png"); 
			elementssprites_.add("tictacbox.png");
			
			elements_.add("LEXIS");
			elements_.add("PUZZLEMANIA");
			elements_.add("TIC TAC TOE");
	     
	    
//		CCLayerColor *clipping = CCLayerColor::layerWithColor(ccc4(255, 255, 255, 255));
//	    clipping.setPosition(ccp(50, 100));
//	    clipping.setContentSize(CGSizeMake(100, 300));
	//    
//	    addChild(clipping);
	     //tableView_.addChild(background);
 
	    tableView_ = CCTableView.view(this, CGSize.zero());
	    tableView_.setContentSize(CGSize.make(1500 , screenSize.height - scrollboxheight)); // You need to set contentSize to enable scrolling.
		
	    tableView_.tDelegate = this;
	    tableView_.dataSource = this;
	    tableView_.bounces = true ;
        //tableView_.setViewSize(CGSize.make(screenSize.width - (80*tilescalefactor) , screenSize.height - (50*tilescalefactor) - topscroll.getContentSize().height * picscale + 30 * picscale));
	    tableView_.setViewSize(CGSize.make(screenSize.width - (80*tilescalefactor) ,  172 * tilescalefactor));
	  //  tableView_.setPosition(CGPoint.ccp(0, screenSize.height/2)); 
	    tableView_.setPosition(CGPoint.ccp(40*tilescalefactor, (100 * tilescalefactor)  ));

	     
	    tableView_.direction = 1;
	    //tableView_.setDirection(SWScrollViewDirectionHorizontal);
	    //tableView_.setVerticalFillOrder(CCTableView.CCTableViewFillTopDown);

	    addChild(tableView_,18);
	    
	    //CCSprite *image = CCSprite::spriteWithFile("Icon.png");
	    //tableView_.addChild(image);
	    tableView_.reloadData();
		 


	}
	
	public void setPosition(CGPoint position)
	{
	    tableView_.setPosition(position);
	}

	public CGPoint getPosition()
	{
	    return tableView_.getPosition();
	}


	public void registerWithTouchDispatcher()
	{
	    CCTouchDispatcher.sharedDispatcher().addTargetedDelegate(this, 0, true);
	}

	public boolean containsTouchLocation(MotionEvent event)
	{
	    CGSize s = tableView_.viewSize;
	    CGRect r = CGRect.make(getPosition().x, getPosition().y, s.width, s.height);
	   // ccMacros.CCLOG("Dist Bdgan "," " + getPosition().x  + " - " + getPosition().y + " - table width - " + s.width + " height - " + s.height +  " Touched point " + convertTouchToNodeSpace(event).x + "-" + convertTouchToNodeSpace(event).y);
	    
	    ccMacros.CCLOG("Dist Bdgan "," " + convertTouchToNodeSpace(event) + CGRect.containsPoint(r, convertTouchToNodeSpace(event)) ) ;
	    return CGRect.containsPoint(r, convertTouchToNodeSpace(event));
	}

	@Override
	public boolean ccTouchesBegan(MotionEvent event) {
		
	 	//ccMacros.CCLOG("Dist Bdgan "," ------------------ " );
	    if (!containsTouchLocation(event)) return false;
	    return true;
	}
	
	 

	//SWTableViewDelegate Protocol
	public void tableCellTouched(CCTableView table, CCTableViewCell cell)
	{
	    //setPosition(ccp(getPosition().x, getPosition().y + cellSize_.height));
		ccMacros.CCLOG("Dist Bdgan "," ------------ " + cell.getPosition().x   + " " + cell.getPosition().x / cellSize_.width );
		int indexselected = (int) (cell.getPosition().x / cellSize_.width) ;
				
				if (indexselected == 0){
					GidiGamesActivity.clicksound() ;
					CCScene next = CCSlideInLTransition.transition(0.2f, LexisMenuLayer.scene());
				    CCDirector.sharedDirector().replaceScene( next );
				}else if(indexselected == 1){
					GidiGamesActivity.clicksound() ;
					CCScene next = CCSlideInLTransition.transition(0.2f, PuzzleMenuLayer.scene());
				    CCDirector.sharedDirector().replaceScene( next );
				}else if(indexselected == 2){
					GidiGamesActivity.clicksound() ;
					CCScene next = CCSlideInLTransition.transition(0.2f, TicTacMenuLayer.scene());
				    CCDirector.sharedDirector().replaceScene( next );
				}
	}

	//SWTableViewDataSource Protocol
	public CGSize cellSizeForTable(CCTableView table)
	{
	    return cellSize_;
	}

	public CCTableViewCell tableCellAtIndex(CCTableView table, int idx)
	{
		CCTableViewBitMapFontCell cell = (CCTableViewBitMapFontCell) table.dequeueBitmapCell();
		if (cell == null) {
			cell = new CCTableViewBitMapFontCell();
		}

		CCBitmapFontAtlas selectgame = CCBitmapFontAtlas.bitmapFontAtlas ( (String) elements_.get(idx), "dalek.fnt");
		CCSprite image = CCSprite.sprite((String) elementssprites_.get(idx));
		cell.setSprite(selectgame,image );
		cell.setScale( tilescalefactor);
		//cell.getSprite().setPosition(CGPoint.ccp((cellSize_.width - image.getContentSize().width) /2, (cellSize_.height - image.getContentSize().height) / 2));
		return cell;
	}

	public int numberOfCellsInTableView(CCTableView table)
	{
	    return elements_.size();
	}

	/*@Override
	public boolean ccKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			ccMacros.CCLOG("Dist Bdgan "," ------Back pressed ------ " );
			
		}
		return false;
	}*/

}



